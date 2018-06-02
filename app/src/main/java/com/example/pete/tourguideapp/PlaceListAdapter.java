package com.example.pete.tourguideapp;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class PlaceListAdapter extends ArrayAdapter<Place> {

    private Activity mContext;
    private Place.PlaceTypes placeType;
    private boolean showCheckedInIndicators;

    PlaceListAdapter(Activity context, ArrayList<Place> places, Place.PlaceTypes placeType, boolean showCheckedInIndicators) {
        super(context, 0, places);

        this.mContext = context;

        this.placeType = placeType;

        this.showCheckedInIndicators = showCheckedInIndicators;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        // Create a new instance of the layout if the View is not being recycled
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_item, parent, false);
        }

        // Get the Place located at this position in the list
        Place currentPlace = getItem(position);

        if (currentPlace == null) {
            return listItemView;
        }

        // Set View properties for the item_list_item layout:

        // Place Name TextView
        TextView nameTextView = listItemView.findViewById(R.id.text_list_item_name);
        nameTextView.setText(currentPlace.getName());

        // Category - specific layout modifications
        // (such as showing a custom icon or changing colors)
        int color = -1;
        int drawable = -1;
        Resources resources = mContext.getResources();

        switch (placeType) {
            case PARK:
                drawable = R.drawable.ic_park;
                color = resources.getColor(R.color.colorParks);
                break;
            case LANDMARK:
                drawable = R.drawable.ic_landmark;
                color = resources.getColor(R.color.colorLandmarks);
                break;
            case RESTAURANT:
                drawable = R.drawable.ic_restaurant;
                color = resources.getColor(R.color.colorRestaurants);
                break;
            case VENUE:
                drawable = R.drawable.ic_venue;
                color = resources.getColor(R.color.colorVenues);
                break;
        }

        // Which UI elements to change the color of
        nameTextView.setBackgroundColor(color);

        ImageView imageView = listItemView.findViewById(R.id.image_list_item_icon);
        imageView.setImageResource(drawable);

        // Set checked in indicators if showCheckedInIndicators is true
        if (showCheckedInIndicators) {
            setCheckedInIndicators(currentPlace, imageView);
        }

        return listItemView;
    }

    /*
    If showCheckedInIndicators is true, this will show the checked in indicators instead of the place icons
    It also looks at the currentPlace to decide between checked_in or not_checked_in drawables
    */
    private void setCheckedInIndicators(Place currentPlace, ImageView imageView) {
        int checkedInDrawable = R.drawable.ic_checked_in_large;
        int notCheckedInDrawable = R.drawable.ic_not_checked_in_large;

        if (currentPlace.hasCheckedIn()) {
            imageView.setImageResource(checkedInDrawable);
        } else {
            imageView.setImageResource(notCheckedInDrawable);
        }
    }
}
