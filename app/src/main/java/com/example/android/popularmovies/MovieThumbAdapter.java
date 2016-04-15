package com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by yao on 4/14/2016.
 */
public class MovieThumbAdapter extends ArrayAdapter<String> {


    public MovieThumbAdapter(Context context, List<String> mThumbIds) {
        super(context,0, mThumbIds);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(getContext());
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(getContext()).load(getItem(position)).into(imageView);
        //imageView.setImageResource(getItem(position));
        return imageView;
    }

}
