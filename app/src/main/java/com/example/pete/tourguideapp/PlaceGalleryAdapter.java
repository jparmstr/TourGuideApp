package com.example.pete.tourguideapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class PlaceGalleryAdapter extends ArrayAdapter<Place> {

    private Activity mContext;
    private boolean showCheckedInIndicators;

    PlaceGalleryAdapter(Activity context, ArrayList<Place> places, boolean showCheckedInIndicators) {
        super(context, 0, places);

        this.mContext = context;

        this.showCheckedInIndicators = showCheckedInIndicators;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View galleryItemView = convertView;

        // Create a new instance of the layout if the View is not being recycled
        if (galleryItemView == null) {
            galleryItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_gallery_item, parent, false);
        }

        // Get the Place located at this position in the list
        Place currentPlace = getItem(position);

        if (currentPlace == null) {
            return galleryItemView;
        }

        // Set View properties for the item_gallery_item layout:

        // Place Name TextView
        TextView nameTextView = galleryItemView.findViewById(R.id.gallery_item_place_name);
        nameTextView.setText(currentPlace.getName());

        // Place photo
        ImageView galleryImageView = galleryItemView.findViewById(R.id.gallery_item_thumbnail_imageView);
        int thisPhotoResId = currentPlace.getPhotoResourceID(mContext);
        galleryImageView.setImageResource(thisPhotoResId);

        // Set checked in indicators if showCheckedInIndicators is true
        if (showCheckedInIndicators) {
            // Checked in indicator reference
            ImageView checkedInImageView = galleryItemView.findViewById(R.id.gallery_item_checked_in_indicator);
            setCheckedInIndicators(currentPlace, checkedInImageView);
        }

        return galleryItemView;
    }

    /*
    If showCheckedInIndicators is true, this will show the checked in indicators over the place images
    It also looks at the currentPlace to decide between checked_in or not_checked_in drawables
    */
    private void setCheckedInIndicators(Place currentPlace, ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);

        int checkedInDrawable = R.drawable.ic_checked_in_large;
        int notCheckedInDrawable = R.drawable.ic_not_checked_in_large;

        if (currentPlace.hasCheckedIn()) {
            imageView.setImageResource(checkedInDrawable);
        } else {
            imageView.setImageResource(notCheckedInDrawable);
        }
    }
}
