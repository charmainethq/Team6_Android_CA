package com.iss;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ScoreAdapter extends ArrayAdapter<Object> {
    private final Context context;
    protected Integer[] scores;
    public ScoreAdapter(Context context, Integer[] scores) {
        super(context, R.layout.record_list);
        this.context = context;
        this.scores = scores;
        addAll(new Object[scores.length]);
    }

    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.record_list, parent, false);
        }
        // set the image for ImageView
        TextView txtTop = view.findViewById(R.id.txtTop);
        txtTop.setText("TOP" + String.valueOf(1+pos));
        // set the text for TextView
        TextView txtTime = view.findViewById(R.id.txtTime);
        txtTime.setText(String.valueOf(scores[pos]) + "s");
        return view;
    }

}
