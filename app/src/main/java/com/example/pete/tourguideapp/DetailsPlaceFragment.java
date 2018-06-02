package com.example.pete.tourguideapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.pete.tourguideapp.MainActivity.findPlaceInDatabaseListByName;
import static com.example.pete.tourguideapp.MainActivity.getPlacesDatabase;
import static com.example.pete.tourguideapp.MainActivity.isCurrentLocation_inRangeOfAddress;
import static com.example.pete.tourguideapp.MainActivity.displayToast;
import static com.example.pete.tourguideapp.MainActivity.savePlaceStates;

public class DetailsPlaceFragment extends Fragment {

    private boolean showCheckedInIndicators = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View thisLayout = inflater.inflate(R.layout.fragment_details_place, container, false);

        // Variables passed from the PlaceListFragment
        String thisName = "";

        // Get the stored information
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            thisName = bundle.getString("name");
            showCheckedInIndicators = bundle.getBoolean("showCheckedInIndicators");
        }

        ArrayList<Place> places;
        // Prevent app crash if getActivity() is somehow null
        if (getActivity() != null) {
            places = getPlacesDatabase();
        } else {
            return thisLayout;
        }
        Place thisPlace = findPlaceInDatabaseListByName(places, thisName);

        // Name
        TextView nameTextView = thisLayout.findViewById(R.id.text_place_details_name);
        nameTextView.setText(thisPlace.getName());

        // Park Photo
        ImageView image_park_details_photo = thisLayout.findViewById(R.id.image_place_details_photo);
        image_park_details_photo.setImageResource(thisPlace.getPhotoResourceID(getActivity()));

        // Description
        TextView descriptionTextView = thisLayout.findViewById(R.id.text_place_details_description);
        descriptionTextView.setText(thisPlace.getDescription());

        // Address
        TextView addressTextView = thisLayout.findViewById(R.id.text_place_details_address);
        addressTextView.setText(thisPlace.getAddress());

        addressTextView.setOnClickListener(v -> {
            Uri googleMapsIntentUri = Uri.parse("geo:0,0?q=" + thisPlace.getAddress() + "?label=" + thisPlace.getName());
            Intent intent = new Intent(Intent.ACTION_VIEW, googleMapsIntentUri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        // Phone Number
        TextView phoneTextView = thisLayout.findViewById(R.id.text_place_details_phone);
        phoneTextView.setText(thisPlace.getPhoneNumber());

        phoneTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + thisPlace.getPhoneNumber()));
            startActivity(intent);
        });

        // Website
        // (Text will just be "Website" - no need to show the entire URL)
        TextView websiteTextView = thisLayout.findViewById(R.id.text_place_details_website);
        websiteTextView.setText(getResources().getString(R.string.website_text));

        websiteTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(thisPlace.getWebsite()));
            startActivity(intent);
        });

        // Hours
        TextView hoursTextView = thisLayout.findViewById(R.id.text_place_details_hours);
        hoursTextView.setText(thisPlace.getHours());

        // Check In Button
        Button checkInButton = thisLayout.findViewById(R.id.button_check_in);
        checkInButton.setOnClickListener(v -> {
            boolean isInRange = isCurrentLocation_inRangeOfAddress(getActivity(), thisPlace.getAddress());

            if (isInRange) {
                // Set value in database
                thisPlace.setCheckedIn(true);

                // TODO: Save the checked in states for the database
                // (Create a public static method in MainActivity that writes these states)
                Context context = getActivity().getApplicationContext();
                if (context != null) {
                    savePlaceStates(context);
                }

                // Show toast
                displayToast(getActivity(), "Check in complete.");

                // Update UI colors
                if (showCheckedInIndicators) {
                    setCheckedInIndicatorColor(thisLayout, thisPlace);
                }

                // Update text
                setCheckedInText(thisLayout, thisPlace);
            } else {
                displayToast(getActivity(), "You are not in range.");
            }

        });

        // Change the color of the border around the check in area if showCheckedInIndicators is true
        if (showCheckedInIndicators) {
            setCheckedInIndicatorColor(thisLayout, thisPlace);
        }

        // Set the text of the check_in_text_indicator TextView
        setCheckedInText(thisLayout, thisPlace);

        return thisLayout;
    }

    /*
    Sets the background of the linearLayout container of the checked in area of the UI
    to the color corresponding to whether thisPlace has been checked into or not
    */
    private void setCheckedInIndicatorColor(View thisLayout, Place thisPlace) {
        LinearLayout linearLayout = thisLayout.findViewById(R.id.layout_check_in_area);
        Drawable background = linearLayout.getBackground();

        // Store colors
        Resources resources = getResources();
        int checked_in_color = resources.getColor(R.color.colorCheckedInGreen);
        int not_checked_in_color = resources.getColor(R.color.colorNotCheckedInRed);

        if (thisPlace.hasCheckedIn()) {
            background.setColorFilter(checked_in_color, PorterDuff.Mode.SRC_IN);
        } else {
            background.setColorFilter(not_checked_in_color, PorterDuff.Mode.SRC_IN);
        }
    }

    /*
    Sets the check_in_text_indicator text to indicate whether thisPlace has been checked into
    */
    private void setCheckedInText(View thisLayout, Place thisPlace) {
        TextView check_in_text_indicator = thisLayout.findViewById(R.id.check_in_text_indicator);

        // Store texts
        Resources resources = getResources();
        String checked_in = resources.getString(R.string.checked_in_true);
        String not_checked_in = resources.getString(R.string.checked_in_false);

        if (thisPlace.hasCheckedIn()) {
            check_in_text_indicator.setText(checked_in);
        } else {
            check_in_text_indicator.setText(not_checked_in);
        }
    }

}
