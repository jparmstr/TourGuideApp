package com.example.pete.tourguideapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.pete.tourguideapp.MainActivity.getPlaceType_fromString;
import static com.example.pete.tourguideapp.MainActivity.getPlacesDatabase;
import static com.example.pete.tourguideapp.MainActivity.loadPlaceDetailsFragment;

public class PlaceGalleryFragment extends Fragment {

    // TODO: This sub is a little redundant with PlaceListFragment
    // I could probably merge the PlaceList_ and PlaceGallery_ classes

    private ArrayList<Place> places;
    public boolean showCheckedInIndicators = false;

    public PlaceGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_place_gallery, container, false);

        // Get information sent in bundle
        String placeTypeString = "";
        showCheckedInIndicators = false;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            placeTypeString = bundle.getString("type");
            showCheckedInIndicators = bundle.getBoolean("showCheckedInIndicators");
        }

        // Handle potential error if the bundle causes placeTypeString to be null
        if (placeTypeString == null) {
            placeTypeString = "";
        }

        if (getActivity() == null) {
            return rootView;
        }

        // Convert placeTypeString to Places.PlaceType
        Place.PlaceTypes placeType = getPlaceType_fromString(getActivity(), placeTypeString);

        // Get the places database (ArrayList of Places)
        places = getPlacesDatabase();

        // Use placeType to filter the database
        places = filterPlaceListByType(places, placeType);

        // Create an adapter to populate the grid_view_place_gallery
        PlaceGalleryAdapter placeGalleryAdapter = new PlaceGalleryAdapter(getActivity(), places, showCheckedInIndicators);
        GridView placesGridView = rootView.findViewById(R.id.grid_view_place_gallery);
        placesGridView.setAdapter(placeGalleryAdapter);

        // Add click listeners for grid view items
        placesGridView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the Place associated with the clicked item
            // (Look up the Place by its name, which is displayed in the item_list_item layout)
            TextView textViewName = view.findViewById(R.id.gallery_item_place_name);
            String thisPlaceName = textViewName.getText().toString();
            Place thisPlace = getPlaceByName(thisPlaceName);

            loadPlaceDetailsFragment(getActivity().getSupportFragmentManager(), thisPlace, showCheckedInIndicators);
        });

        return rootView;
    }

    private ArrayList<Place> filterPlaceListByType(ArrayList<Place> list, Place.PlaceTypes placeType) {
        ArrayList<Place> result = new ArrayList<>();

        for (Place p : list) {
            if (p.getType() == placeType) {
                result.add(p);
            }
        }

        return result;
    }

    private Place getPlaceByName(String placeName) {
        Place result = null;

        for (Place p : places) {
            if (p.getName().equals(placeName)) {
                result = p;
            }
        }

        return result;
    }

}
