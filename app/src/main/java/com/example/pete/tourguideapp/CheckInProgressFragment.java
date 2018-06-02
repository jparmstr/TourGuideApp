package com.example.pete.tourguideapp;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.pete.tourguideapp.MainActivity.getPlacesDatabase;
import static com.example.pete.tourguideapp.MainActivity.loadPlaceListOrGallery;

public class CheckInProgressFragment extends Fragment {

    // View References
    private TextView parksTextView;
    private ProgressBar parksProgressBar;
    private TextView landmarksTextView;
    private ProgressBar landmarksProgressBar;
    private TextView restaurantsTextView;
    private ProgressBar restaurantsProgressBar;
    private TextView venuesTextView;
    private ProgressBar venuesProgressBar;

    public CheckInProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_check_in_progress, container, false);

        // Get view references
        getViewReferences(rootView);

        // Set theme colors for progress bars:
        setProgressBarColors();

        if (getActivity() == null) {
            return rootView;
        }

        // Get Places database
        ArrayList<Place> places = getPlacesDatabase();

        // Set check in progress texts and progress bar values
        setTextsAndProgress(places);

        // Set on click handlers (go to places list or gallery)
        setClickHandlers();

        return rootView;
    }

    /*
    Get references to the Views of fragment_check_in_progress
    Which will be stored in private instance variables and used by methods of this class
    */
    private void getViewReferences(View rootView) {
        parksTextView = rootView.findViewById(R.id.text_view_check_in_progress_parks);
        parksProgressBar = rootView.findViewById(R.id.progress_bar_parks);

        landmarksTextView = rootView.findViewById(R.id.text_view_check_in_progress_landmarks);
        landmarksProgressBar = rootView.findViewById(R.id.progress_bar_landmarks);

        restaurantsTextView = rootView.findViewById(R.id.text_view_check_in_progress_restaurants);
        restaurantsProgressBar = rootView.findViewById(R.id.progress_bar_restaurants);

        venuesTextView = rootView.findViewById(R.id.text_view_check_in_progress_venues);
        venuesProgressBar = rootView.findViewById(R.id.progress_bar_venues);
    }

    /*
    Set color filters on the progress bars matching place category colors defined in res/colors
    */
    private void setProgressBarColors() {
        // Store colors
        Resources resources = getResources();
        int parks_color = resources.getColor(R.color.colorParks);
        int landmarks_color = resources.getColor(R.color.colorLandmarks);
        int restaurants_color = resources.getColor(R.color.colorRestaurants);
        int venues_color = resources.getColor(R.color.colorVenues);

        // Parks progress bar
        parksProgressBar.getProgressDrawable().setColorFilter(parks_color, PorterDuff.Mode.SRC_IN);

        // Landmarks progress bar
        landmarksProgressBar.getProgressDrawable().setColorFilter(landmarks_color, PorterDuff.Mode.SRC_IN);

        // Restaurants progress bar
        restaurantsProgressBar.getProgressDrawable().setColorFilter(restaurants_color, PorterDuff.Mode.SRC_IN);

        // Venues progress bar
        venuesProgressBar.getProgressDrawable().setColorFilter(venues_color, PorterDuff.Mode.SRC_IN);
    }

    /*
    Set the TextView texts for the Check In Progress Fragment
    Uses String resources to set text such as "Parks: 1 of 10"
    Also set the progress bar progress and max values
    */
    private void setTextsAndProgress(ArrayList<Place> places) {
        // Parks
        double parksCount = getCheckedInPlaceCount(places, Place.PlaceTypes.PARK);
        double parksTotal = getTotalPlaceCount(places, Place.PlaceTypes.PARK);

        parksTextView.setText(getResources().getString(R.string.check_in_progress_parks_text,
                getCheckedInPlaceCount(places, Place.PlaceTypes.PARK),
                parksTotal));

        parksProgressBar.setProgress((int) parksCount);
        parksProgressBar.setMax((int) parksTotal);

        // Landmarks
        double landmarksCount = getCheckedInPlaceCount(places, Place.PlaceTypes.LANDMARK);
        double landmarksTotal = getTotalPlaceCount(places, Place.PlaceTypes.LANDMARK);

        landmarksTextView.setText(getResources().getString(R.string.check_in_progress_landmarks_text,
                landmarksCount,
                landmarksTotal));

        landmarksProgressBar.setProgress((int) landmarksCount);
        landmarksProgressBar.setMax((int) landmarksTotal);

        // Restaurants
        double restaurantsCount = getCheckedInPlaceCount(places, Place.PlaceTypes.RESTAURANT);
        double restaurantsTotal = getTotalPlaceCount(places, Place.PlaceTypes.RESTAURANT);

        restaurantsTextView.setText(getResources().getString(R.string.check_in_progress_restaurants_text,
                restaurantsCount,
                restaurantsTotal));

        restaurantsProgressBar.setProgress((int) restaurantsCount);
        restaurantsProgressBar.setMax((int) restaurantsTotal);

        // Venues
        double venuesCount = getCheckedInPlaceCount(places, Place.PlaceTypes.VENUE);
        double venuesTotal = getTotalPlaceCount(places, Place.PlaceTypes.VENUE);

        venuesTextView.setText(getResources().getString(R.string.check_in_progress_venues_text,
                venuesCount,
                venuesTotal));

        venuesProgressBar.setProgress((int) venuesCount);
        venuesProgressBar.setMax((int) venuesTotal);
    }

    /*
    Return the total number of Places of the given type
    */
    private double getTotalPlaceCount(ArrayList<Place> places, Place.PlaceTypes type) {
        double result = 0;

        for (Place p : places) {
            if (p.getType() == type) {
                result++;
            }
        }

        return result;
    }

    /*
    Return the number of places of the given type that have been checked into
    */
    private double getCheckedInPlaceCount(ArrayList<Place> places, Place.PlaceTypes type) {
        double result = 0;

        for (Place p : places) {
            if (p.getType() == type) {
                if (p.hasCheckedIn()) {
                    result++;
                }
            }
        }

        return result;
    }

    /*
    Set click handlers on UI elements
    Will use MainActivity.loadPlaceListOrGallery
    */
    private void setClickHandlers() {
        if (getActivity() == null) {
            return;
        }

        // Fragment manager that will be passed to all On Click Listeners
        android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        
        // Category texts
        String parks_text = getResources().getString(R.string.parks_text);
        String landmarks_text = getResources().getString(R.string.landmarks_text);
        String restaurants_text = getResources().getString(R.string.restaurants_text);
        String venues_text = getResources().getString(R.string.venues_text);

        parksTextView.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, parks_text, true));
        parksProgressBar.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, parks_text, true));

        landmarksTextView.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, landmarks_text, true));
        landmarksProgressBar.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, landmarks_text, true));

        restaurantsTextView.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, restaurants_text, true));
        restaurantsProgressBar.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, restaurants_text, true));

        venuesTextView.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, venues_text, true));
        venuesProgressBar.setOnClickListener(v -> loadPlaceListOrGallery(fragmentManager, venues_text, true));
    }

}
