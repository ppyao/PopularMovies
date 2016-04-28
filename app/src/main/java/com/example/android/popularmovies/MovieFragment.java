package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.popularmovies.MainActivity;
import com.example.android.popularmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates fetching the popular movies and displaying it as a {@link GridView} layout.
 */
public class MovieFragment extends Fragment {
    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private MovieThumbAdapter mThumbAdapter;

    public MovieFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchMovieTask fetchMovieTask = new FetchMovieTask();
            fetchMovieTask.execute("vote");  // other sort option: "popularity"
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create a MovieThumbAdapter from dummy thumbnails
        String[] dummyURLs = {
                "http://image.tmdb.org/t/p/w92//lfeaDfSv0kjiB3WW0hU3fdf8ZEV.jpg",
                "http://image.tmdb.org/t/p/w92//tprSZOUBYa6PBj63EI1IAZu91SS.jpg"
        };
        List<String> thumbURLs = new ArrayList<String>(Arrays.asList(dummyURLs));

        // Create a MovieThumbAdapter, which is a customized ArrayAdapter.
        // The MovieThumbAdapter will use data to populate the GridView it's attached to
        mThumbAdapter = new MovieThumbAdapter(getActivity(), thumbURLs);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridview.setAdapter(mThumbAdapter);

        // register a listener, so that when the item is clicked, the call back is invoked
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String thumbURL = mThumbAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, thumbURL);
                startActivity(intent);

                //Toast.makeText(getActivity(), "" + position + ": " + thumbURL,
                //        Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /**
         * Take the String representing the complete movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * The data we need to return is the String of the relative URL of the movies' thumbnails
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_POSTER_PATH = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);

            // The API response provides a relative path to a movie poster image.
            // Append a base path ahead of this relative path to build the complete url.
            // Fetch the image using Picasso by passing in the complete url.
            String baseURL = "http://image.tmdb.org/t/p/w185/";

            String[] resultStrs = new String[movieArray.length()];
            for(int i = 0; i < movieArray.length(); i++) {
                // Get the JSON object representing each movie
                JSONObject eachMovie = movieArray.getJSONObject(i);

                // moviePath is in a child array called "poster_path", which is 1 element long.
                String moviePath = eachMovie.getString(OWM_POSTER_PATH);
                resultStrs[i] = baseURL + moviePath;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Movie entry: " + s);
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no sort order, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            String sortByPopularity = "popularity.desc";
            String sortByVote = "ote_average.desc";
            String certCountry = "US";
            String cert = "R";
            String apiKey = "";

            try {
                // Construct the URL for the TMDb query
                // Possible parameters are avaiable at TMDb's forecast API page, at
                // https://www.themoviedb.org/documentation/api

                // sort order: most popular
                // URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=YOUR_API_KEY");
                // sort order: highest-rated
                // URL: /discover/movie/?certification_country=US&certification=R&sort_by=vote_average.desc&api_key=YOUR_API_KEY

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String CERT_COUNTRY_PARAM = "certification_country";
                final String CERT_PARAM = "certification";
                final String APPID_PARAM = "api_key";

                Uri.Builder uriBuilder = new Uri.Builder();
                if (params[0].equals("popularity")) {
                    uriBuilder = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(SORT_PARAM, sortByPopularity)
                            .appendQueryParameter(APPID_PARAM, apiKey);
                }
                else if (params[0].equals("vote")) {
                    uriBuilder = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(CERT_COUNTRY_PARAM, certCountry)
                            .appendQueryParameter(CERT_PARAM, cert)
                            .appendQueryParameter(SORT_PARAM, sortByVote)
                            .appendQueryParameter(APPID_PARAM, apiKey);
                }
                String builtUri = uriBuilder.build().toString();
                URL url = new URL(builtUri);
                Log.v(LOG_TAG, "Built URI " + builtUri);

                // Create the request to The Movie Database, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Popular movie JSON String: " + movieJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        // onPostExecute receive the return value of doInBackground as the input
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mThumbAdapter.clear();
                for(String movieThumbURL : result) {
                    mThumbAdapter.add(movieThumbURL);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}
