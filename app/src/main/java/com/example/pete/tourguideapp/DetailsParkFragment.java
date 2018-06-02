package com.example.pete.tourguideapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.pete.tourguideapp.MainActivity.getPlacesDatabase;
import static com.example.pete.tourguideapp.MainActivity.findPlaceInDatabaseListByName;

public class DetailsParkFragment extends Fragment {

    public DetailsParkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisLayout = inflater.inflate(R.layout.fragment_details_park, container, false);

        // Variables passed from the PlaceListFragment
        String thisName = "";

        // Get the stored information
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            thisName = bundle.getString("name");
        }

        if (getActivity() == null) {
            return thisLayout;
        }

        ArrayList<Place> places = getPlacesDatabase();
        Place thisPlace = findPlaceInDatabaseListByName(places, thisName);

        // Set View properties for the fragment_details_park layout:

        // Acres
        TextView acresTextView = thisLayout.findViewById(R.id.text_park_details_acres);
        acresTextView.setText(getResources().getString(R.string.acres_text, thisPlace.getAcres()));

        // Trail Length
        TextView trailTextView = thisLayout.findViewById(R.id.text_park_details_trail);
        String trailsText = getResources().getString(R.string.trail_length_text, thisPlace.getTrailLength());

        // Do not add "trail length" text if thisPlace.trailLength does not contain numbers.
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(trailsText);
        if (!matcher.lookingAt()) {
            trailsText = thisPlace.getTrailLength();
        }

        trailTextView.setText(trailsText);

        return thisLayout;
    }

}
