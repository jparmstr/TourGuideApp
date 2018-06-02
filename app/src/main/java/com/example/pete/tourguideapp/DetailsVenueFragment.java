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

public class DetailsVenueFragment extends Fragment {

    public DetailsVenueFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisLayout = inflater.inflate(R.layout.fragment_details_venue, container, false);

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

        // Set View properties for the fragment_details_venue layout:

        // Upcoming Shows
        TextView upcomingTextView = thisLayout.findViewById(R.id.text_venue_details_upcoming);
        upcomingTextView.setText(thisPlace.getUpcomingShows());

        // TODO: Create a class + adapter + JSON database for upcoming shows

        return thisLayout;
    }

}
