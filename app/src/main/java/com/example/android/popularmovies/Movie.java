package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yao on 5/2/2016.
 */
public class Movie implements Parcelable {
    String movieThumbURL;
    String movieTitle;
    String movieDate;
    String movieVote;
    String movieOverview;

    public Movie(String movieThumbURL, String movieTitle, String movieDate, String movieVote, String movieOverview)
    {
        this.movieThumbURL = movieThumbURL;
        this.movieTitle = movieTitle;
        this.movieDate = movieDate;
        this.movieVote = movieVote;
        this.movieOverview = movieOverview;
    }

    private Movie(Parcel in){
        movieThumbURL = in.readString();
        movieTitle = in.readString();
        movieDate = in.readString();
        movieVote = in.readString();
        movieOverview = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() { return movieTitle + "--" + movieDate; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieThumbURL);
        parcel.writeString(movieTitle);
        parcel.writeString(movieDate);
        parcel.writeString(movieVote);
        parcel.writeString(movieOverview);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };
}
