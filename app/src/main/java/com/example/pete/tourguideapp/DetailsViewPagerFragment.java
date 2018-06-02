package com.example.pete.tourguideapp;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static com.example.pete.tourguideapp.MainActivity.findPlaceInDatabaseListByName;
import static com.example.pete.tourguideapp.MainActivity.getPlacesDatabase;
import static com.example.pete.tourguideapp.Place.PlaceTypes.LANDMARK;
import static com.example.pete.tourguideapp.Place.PlaceTypes.PARK;
import static com.example.pete.tourguideapp.Place.PlaceTypes.RESTAURANT;
import static com.example.pete.tourguideapp.Place.PlaceTypes.VENUE;

// fragment_details_view_pager layout which will hold fragment_details_place and fragment_details_park (for example)
public class DetailsViewPagerFragment extends Fragment {

    public boolean showCheckedInIndicators = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View thisLayout = inflater.inflate(R.layout.fragment_details_view_pager, container, false);

        // Variables passed from the PlaceListFragment
        String thisName = "";

        // Get the stored information
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            thisName = bundle.getString("name");
            showCheckedInIndicators = bundle.getBoolean("showCheckedInIndicators");
        }

        if (getActivity() == null) {
            return thisLayout;
        }

        ArrayList<Place> places = getPlacesDatabase();
        Place thisPlace = findPlaceInDatabaseListByName(places, thisName);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = thisLayout.findViewById(R.id.view_pager);

        // Create an adapter that knows which fragment should be shown on each page
        // getChildFragmentManager() is important here. The DetailsFragmentPagerAdapter could not be reused with getActivity().getSupportFragmentManager()
        DetailsFragmentPagerAdapter adapter = new DetailsFragmentPagerAdapter(getActivity(), getChildFragmentManager(), thisPlace.getType(), thisPlace.getName(), showCheckedInIndicators);
        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = thisLayout.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // Change the background color of the Tab Layout tabs depending on place category
        int color = -1;
        Resources resources = getActivity().getResources();

        switch (thisPlace.getType()) {
            case PARK:
                color = resources.getColor(R.color.colorParks);
                break;
            case LANDMARK:
                color = resources.getColor(R.color.colorLandmarks);
                break;
            case RESTAURANT:
                color = resources.getColor(R.color.colorRestaurants);
                break;
            case VENUE:
                color = resources.getColor(R.color.colorVenues);
                break;
        }

        tabLayout.setBackgroundColor(color);

        return thisLayout;
    }
}
