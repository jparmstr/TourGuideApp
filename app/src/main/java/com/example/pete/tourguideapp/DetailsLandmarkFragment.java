package com.example.pete.tourguideapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.pete.tourguideapp.MainActivity.getPlacesDatabase;
import static com.example.pete.tourguideapp.MainActivity.findPlaceInDatabaseListByName;

public class DetailsLandmarkFragment extends Fragment {

    public DetailsLandmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisLayout = inflater.inflate(R.layout.fragment_details_landmark, container, false);

        // Variables passed from the PlaceListFragment
        String thisName = "";

        // Get the stored information
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            thisName = bundle.getString("name");
        }

        ArrayList<Place> places = getPlacesDatabase();
        Place thisPlace = findPlaceInDatabaseListByName(places, thisName);

        // Set View properties for the fragment_details_landmark layout:

        // History
        TextView historyTextView = thisLayout.findViewById(R.id.text_landmark_details_history);
        historyTextView.setText(thisPlace.getHistory());

        return thisLayout;
    }

}
