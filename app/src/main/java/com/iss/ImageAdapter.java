package com.iss;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> imagePaths;
    private int numCards = 20;
    private ArrayList<ImageView> imageViews = new ArrayList<>();

    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
        } else {
            imageView = (ImageView) convertView;
        }

        String imagePath = imagePaths.get(position);
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            Picasso.get().load(file).into(imageView);
        } else {
            // Handle null or empty imagePath
            imageView.setImageDrawable(null); // Clear the imageView
        }

        imageView.setCropToPadding(false); // normalize padding
        imageView.setBackgroundResource(0); // Remove the border

        return imageView;
    }



    public ArrayList<ImageView> getImageViews() {
        return imageViews;
    }

    public void updateData(ArrayList<String> newImagePaths) {
        this.imagePaths = newImagePaths;
        this.imageViews.clear();
        notifyDataSetChanged();
    }



}
