package com.example.pete.tourguideapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    //region Constants, Instance Variables, and Static Variables

    // Constants
    private static final String FRAGMENT_TAG_MAIN_FRAGMENT = "mainFragmentTag";
    private static final String FRAGMENT_TAG_PLACE_LIST = "placeListTag";
    private static final String FRAGMENT_TAG_PLACE_GALLERY = "placeGalleryTag";
    private static final String FRAGMENT_TAG_PARK_DETAILS = "parkDetailsTag";
    private static final String FRAGMENT_TAG_LANDMARK_DETAILS = "landmarkDetailsTag";
    private static final String FRAGMENT_TAG_RESTAURANT_DETAILS = "restaurantDetailsTag";
    private static final String FRAGMENT_TAG_VENUE_DETAILS = "venueDetailsTag";
    private static final String FRAGMENT_TAG_CHECK_IN_PROGRESS = "checkInProgressTag";

    // How often the location is updated
    private static final int MS_IN_SECOND = 1000;
    private static final long UPDATE_INTERVAL = 10 * MS_IN_SECOND;  // 10 seconds
    private static final long FASTEST_INTERVAL = 2 * MS_IN_SECOND; // 2 seconds

    // This request code's value doesn't matter, so long as it's < 65535. See the documentation for ActivityCompat.requestPermissions()
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 333;
    // See the table in the project notes for the equivalent imperial distance
    private static final double LAT_LON_CHECK_IN_RANGE = .001;

    private static final String PLACE_STATES_FILENAME = "tourGuidePlaceStates";

    // Places database
    private static ArrayList<Place> places;

    // App settings / references
    private static boolean showPlacesAsList = false;
    private static String lastSelectedPlaceType = "";
    private static Menu mMenu;

    // Location variables (precise location)
    private GoogleApiClient googleApiClient;
    private static Location location;

    //endregion Constants and Instance Variables

    //region Project Notes

    // + convert drawable resources to multiple screen densities
    // see: http://nsimage.brosteins.com/
    // I created my own tool for this: AndroidDensities.exe

    // TODO: finish adding check-in feature
    // - Keep track of check-in date

    /* Latitude / Longitude accuracy reference
    Six decimal places
    .000001	4 inches
    Five decimal places
    .00001	3.6 feet
    Four decimal places
    .0001	36 feet
    Three decimal places
    .001	360 feet
    Two decimal places
    .01	3600 feet
    0.7 miles
    One decimal places
    .1	36,000 feet
    6.9 miles */

    //endregion Project Notes

    //region Overrides and Core Functionality

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the action bar title
        setTitle(getResources().getString(R.string.app_name));

        // Load the main_fragment layout
        loadMainFragment();

        // Set up the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Add a click listener to the navigation header (return to Main Fragment)
        View headerView = navigationView.getHeaderView(0);
        LinearLayout navHeaderLayout = headerView.findViewById(R.id.nav_header_linear_layout);
        navHeaderLayout.setOnClickListener(v -> {
            loadMainFragment();

            // Close the Nav Drawer if it is open
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        // Set up location-based services (precise location)
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Load the places database
        places = loadPlacesDatabase(this);

        // Load the checked in states for the database
        loadPlaceStates();

        // Debug
        Log.d("MainActivity", "onCreate");
    }

    /*
    Back presses are caught by MainActivity, not by its Fragments
    The stack is MainFragment > PlaceListFragment > DetailsPlaceFragment
    Or MainFragment > CheckInProgressFragment > PlaceListFragment > DetailsPlaceFragment
    */
    @Override
    public void onBackPressed() {
        // Get the list of Fragments
        List frags = getSupportFragmentManager().getFragments();
        if (frags != null && frags.size() > 0) {
            // Get the current, topmost Fragment
            // (Seems like there's only ever one fragment in this list)
            Fragment fragment = (Fragment) frags.get(frags.size() - 1);
            if (fragment != null && fragment.getTag() != null) {
                switch (fragment.getTag()) {
                    case FRAGMENT_TAG_MAIN_FRAGMENT:
                        // Let the system handle Back presses from main fragment
                        super.onBackPressed();
                        break;
                    case FRAGMENT_TAG_PLACE_LIST:
                        PlaceListFragment placeListFragment = (PlaceListFragment) fragment;
                        if (placeListFragment.showCheckedInIndicators) {
                            // Go back to Check In Progress Fragment
                            loadCheckInProgressFragment();
                        } else {
                            // Go back to Main Fragment
                            loadMainFragment();
                        }
                        break;
                    case FRAGMENT_TAG_PLACE_GALLERY:
                        PlaceGalleryFragment placeGalleryFragment = (PlaceGalleryFragment) fragment;
                        if (placeGalleryFragment.showCheckedInIndicators) {
                            // Go back to Check In Progress Fragment
                            loadCheckInProgressFragment();
                        } else {
                            // Go back to Main Fragment
                            loadMainFragment();
                        }
                        break;
                    case FRAGMENT_TAG_PARK_DETAILS:
                        DetailsViewPagerFragment detailsViewPagerFragment1 = (DetailsViewPagerFragment) fragment;
                        // Go back to Place List (parks)
                        loadPlaceListOrGallery(getSupportFragmentManager(), getResources().getString(R.string.parks_text), detailsViewPagerFragment1.showCheckedInIndicators);
                        break;
                    case FRAGMENT_TAG_LANDMARK_DETAILS:
                        DetailsViewPagerFragment detailsViewPagerFragment2 = (DetailsViewPagerFragment) fragment;
                        // Go back to Place List (landmarks)
                        loadPlaceListOrGallery(getSupportFragmentManager(), getResources().getString(R.string.landmarks_text), detailsViewPagerFragment2.showCheckedInIndicators);
                        break;
                    case FRAGMENT_TAG_RESTAURANT_DETAILS:
                        DetailsViewPagerFragment detailsViewPagerFragment3 = (DetailsViewPagerFragment) fragment;
                        // Go back to Place List (restaurants)
                        loadPlaceListOrGallery(getSupportFragmentManager(), getResources().getString(R.string.restaurants_text), detailsViewPagerFragment3.showCheckedInIndicators);
                        break;
                    case FRAGMENT_TAG_VENUE_DETAILS:
                        DetailsViewPagerFragment detailsViewPagerFragment4 = (DetailsViewPagerFragment) fragment;
                        // Go back to Place List (venues)
                        loadPlaceListOrGallery(getSupportFragmentManager(), getResources().getString(R.string.venues_text), detailsViewPagerFragment4.showCheckedInIndicators);
                        break;
                    case FRAGMENT_TAG_CHECK_IN_PROGRESS:
                        // Go back to Main Fragment
                        loadMainFragment();
                        break;
                    default:
                        Log.d("MainActivity", "onBackPressed() unknown fragment tag: " + fragment.getTag());
                }
            }
        }

        // Back button closes the Nav Drawer if it is open
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action_bar_sub, menu);

        // Store a reference to the current action bar menu
        mMenu = menu;

        return true;
    }

    /*
    Hide or show the Action Bar Submenu
    */
    private static void hideActionBarSubmenu(boolean hideMenu) {
        if (mMenu == null) {
            return;
        }

        // Hide or show the menu. Uses mMenu as a reference to the current menu.
        if (hideMenu) {
            mMenu.findItem(R.id.action_list).setVisible(false);
            mMenu.findItem(R.id.action_gallery).setVisible(false);
        } else {
            mMenu.findItem(R.id.action_list).setVisible(true);
            mMenu.findItem(R.id.action_gallery).setVisible(true);
        }
    }

    /*
    Handle action bar sub-menu item clicks
    Refresh the current Place List or Place Gallery
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_list) {
            // view Places List as list
            showPlacesAsList = true;
        } else if (id == R.id.action_gallery) {
            // view Places List as gallery
            showPlacesAsList = false;
        }

        // Refresh the current listView, if a listView is the current fragment
        List frags = getSupportFragmentManager().getFragments();
        if (frags != null && frags.size() > 0) {
            // Get the current, topmost Fragment
            // (Seems like there's only ever one fragment in this list)
            Fragment fragment = (Fragment) frags.get(frags.size() - 1);
            if (fragment != null && fragment.getTag() != null) {
                switch (fragment.getTag()) {
                    case FRAGMENT_TAG_PLACE_LIST:
                        if (!lastSelectedPlaceType.equals("")) {
                            PlaceListFragment placeListFragment = (PlaceListFragment) fragment;
                            loadPlaceListOrGallery(getSupportFragmentManager(), lastSelectedPlaceType, placeListFragment.showCheckedInIndicators);
                        }
                        break;
                    case FRAGMENT_TAG_PLACE_GALLERY:
                        if (!lastSelectedPlaceType.equals("")) {
                            PlaceGalleryFragment placeGalleryFragment = (PlaceGalleryFragment) fragment;
                            loadPlaceListOrGallery(getSupportFragmentManager(), lastSelectedPlaceType, placeGalleryFragment.showCheckedInIndicators);
                        }
                        break;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_check_in_progress) {
            loadCheckInProgressFragment();
        } else {
            loadPlaceListOrGallery(getSupportFragmentManager(), item.getTitle().toString(), false);
        }

        // Close the Navigation Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Set the action bar title
        setTitle(item.getTitle());

        return true;
    }

    /*
    Return the places database.
    This returns a static list of Places, not a newly created one as loadPlacesDatabase would.
    This is done so that changes to Place.checkedIn persist.
    */
    public static ArrayList<Place> getPlacesDatabase() {
        return places;
    }

    /*
    This retrieves the database of Songs which is stored in JSON format as /res/raw/database.json
    The database is based on an Excel spreadsheet converted to CSV
    The resulting CSV file was converted to JSON using this tool:
    http://www.convertcsv.com/csv-to-json.htm

    This should only be called when the application loads.
    If a Fragment needs the places database, use getPlacesDatabase() instead.
    */
    private static ArrayList<Place> loadPlacesDatabase(Context context) {
        ArrayList<Place> result = new ArrayList<>();

        // Get the JSON resource as a String
        InputStream inputStream = context.getResources().openRawResource(R.raw.database);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

        // Parse the JSON string
        JSONArray array;
        try {
            array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);

                String type = row.getString("Type");
                String name = row.getString("Name");
                String description = row.getString("Description");
                String address = row.getString("Address");
                String phoneNumber = row.getString("Phone Number");
                String website = row.getString("Website");
                String hours = row.getString("Hours");

                // Convert type from String to Place.PlaceTypes
                Place.PlaceTypes thisType = getPlaceType_fromString(context, type);

                Place place = new Place(thisType, name, description, address, phoneNumber, website, hours);

                // Use the builder method corresponding to this Place's type
                switch (place.getType()) {
                    case PARK:
                        place.makePark(row.getDouble("Acres"), row.getString("Trail Length"));
                        break;
                    case LANDMARK:
                        place.makeLandmark(row.getString("History"));
                        break;
                    case RESTAURANT:
                        place.makeRestaurant(row.getString("Type of Food"));
                        break;
                    case VENUE:
                        place.makeVenue(row.getString("Upcoming Shows"));
                        break;
                }

                result.add(place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /*
    Assuming the Places database has already been loaded by getPlacesDatabase(),
    return a specific Place given its name
    (Note that Place names are assumed to be unique)
    */
    public static Place findPlaceInDatabaseListByName(ArrayList<Place> places, String placeName) {
        Place result = null;

        for (Place p : places) {
            if (p.getName().equals(placeName)) {
                result = p;
                break;
            }
        }

        return result;
    }

    /*
    Get a Place.PlaceTypes value from a given String
    The place types are stored in the JSON database as Strings
    This is called from getPlacesDatabase()
    */
    public static Place.PlaceTypes getPlaceType_fromString(Context context, String type) {
        Place.PlaceTypes thisType = null;

        // Get String resource Place categories (will match thisPlaceType)
        String parks_text = context.getResources().getString(R.string.parks_text);
        String landmarks_text = context.getResources().getString(R.string.landmarks_text);
        String restaurants_text = context.getResources().getString(R.string.restaurants_text);
        String venues_text = context.getResources().getString(R.string.venues_text);

        // Identify the Place Type (place category)
        // Can't use a switch statement here because String Resources are not Constants for some reason
        if (type.equals(parks_text)) {
            thisType = Place.PlaceTypes.PARK;
        } else if (type.equals(landmarks_text)) {
            thisType = Place.PlaceTypes.LANDMARK;
        } else if (type.equals(restaurants_text)) {
            thisType = Place.PlaceTypes.RESTAURANT;
        } else if (type.equals(venues_text)) {
            thisType = Place.PlaceTypes.VENUE;
        } else {
            Log.d("getPlaceType_fromString", "Unrecognized place type String in getPlaceType_fromString: " + type);
        }

        return thisType;
    }

    // Load main_fragment into MainActivity's main_content
    private void loadMainFragment() {
        Fragment fragment = new MainFragment();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, FRAGMENT_TAG_MAIN_FRAGMENT).commit();

        // Set the action bar title
        setTitle(getResources().getString(R.string.app_name));

        // Don't show the action bar submenu in Main Fragment
        hideActionBarSubmenu(true);
    }

    // Load check_in_progress_fragment into MainActivity's main_content
    private void loadCheckInProgressFragment() {
        Fragment fragment = new CheckInProgressFragment();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, FRAGMENT_TAG_CHECK_IN_PROGRESS).commit();

        // Set the action bar title
        setTitle(getResources().getString(R.string.check_in_menu_title));

        // Don't show the action bar submenu in Check In Progress Fragment
        hideActionBarSubmenu(true);
    }

    // Load Place List or Gallery. The boolean showPlacesAsList decides which.
    public static void loadPlaceListOrGallery(android.support.v4.app.FragmentManager fragmentManager, String type, boolean showCheckedInIndicators) {
        Fragment fragment;

        // Put extra data into the fragment
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putBoolean("showCheckedInIndicators", showCheckedInIndicators);

        if (showPlacesAsList) {
            fragment = new PlaceListFragment();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, FRAGMENT_TAG_PLACE_LIST).commit();
        } else {
            fragment = new PlaceGalleryFragment();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, FRAGMENT_TAG_PLACE_GALLERY).commit();
        }

        lastSelectedPlaceType = type;

        hideActionBarSubmenu(false);
    }

    public static void loadPlaceDetailsFragment(android.support.v4.app.FragmentManager fragmentManager, Place thisPlace, boolean showCheckedInIndicators) {
        Fragment fragment;

        // Select the fragment tag corresponding to this Place's type
        String thisFragmentTag = "";
        switch (thisPlace.getType()) {
            case PARK:
                thisFragmentTag = FRAGMENT_TAG_PARK_DETAILS;
                break;
            case LANDMARK:
                thisFragmentTag = FRAGMENT_TAG_LANDMARK_DETAILS;
                break;
            case RESTAURANT:
                thisFragmentTag = FRAGMENT_TAG_RESTAURANT_DETAILS;
                break;
            case VENUE:
                thisFragmentTag = FRAGMENT_TAG_VENUE_DETAILS;
                break;
        }

        fragment = new DetailsViewPagerFragment();

        // Put extra data into the fragment
        // This data will be acted on by the details fragment class
        Bundle bundle1 = new Bundle();
        bundle1.putString("name", thisPlace.getName());
        bundle1.putBoolean("showCheckedInIndicators", showCheckedInIndicators);
        fragment.setArguments(bundle1);

        // Insert details fragment into main_content's content_frame
        // Tag the fragment so that it can be identified later
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, thisFragmentTag).commit();

        // Hide the action bar menu for details layout
        hideActionBarSubmenu(true);
    }

    // Display Toast notification
    public static void displayToast(Context context, String textToShow) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, textToShow, duration);
        toast.show();
    }

    //endregion Overrides and Core Functionality

    //region Location

    /*
    The basic methods required for obtaining device location and permissions were adapted from:
    https://github.com/googlesamples/android-play-location
    https://demonuts.com/current-gps-location/
    https://developer.android.com/training/permissions/requesting#perm-request
    */

    /*
    Test the location service.
    Displays latitude & longitude if the test was successful
    */
    public void testLocation(View v) {
        // Display whether current location is within check-in range of an address
        displayToast(this, String.valueOf(isCurrentLocation_inRangeOfAddress(this, "1400 W Millbrook Rd, Raleigh, NC 27612")));
        displayToast(this, String.valueOf(isCurrentLocation_inRangeOfAddress(this, "2805 Orkney Pl, Raleigh, NC 27604")));

        // Display current location
        if (location == null) {
            Log.d("testLocation", "Location was null. Requesting that location updates start.");
            startLocationUpdates();
            return;
        }

        String latitude = "Latitude";
        String longitude = "Longitude";

        latitude = String.format(Locale.ENGLISH, "%s: %f",
                latitude,
                location.getLatitude());

        longitude = String.format(Locale.ENGLISH, "%s: %f",
                longitude,
                location.getLongitude());

        displayToast(this, latitude + "\n" + longitude);
    }

    /*
    Gets Latitude and Longitude coordinates from an address via Google Play Services Maps
    Adapted from https://stackoverflow.com/a/27834110
    */
    private static LatLng getLocationFromAddress(Activity activity, String address) {
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList;
        LatLng latLng = null;

        try {
            // May throw an IOException
            addressList = geocoder.getFromLocationName(address, 5);
            if (addressList == null) {
                return null;
            }

            Address location = addressList.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return latLng;
    }

    /*
    Determines whether the current device location is within range of an address
    In range is defined by the constant LAT_LON_CHECK_IN_RANGE
    */
    public static boolean isCurrentLocation_inRangeOfAddress(Activity activity, String address) {
        boolean result = false;

        LatLng addressLatLng = getLocationFromAddress(activity, address);

        if (location == null || addressLatLng == null) {
            return false;
        }

        double myLat = location.getLatitude();
        double myLon = location.getLongitude();

        double addressLat = addressLatLng.latitude;
        double addressLon = addressLatLng.longitude;

        double latDifference = Math.abs(addressLat - myLat);
        double lonDifference = Math.abs(addressLon - myLon);

        // Must be within LAT_LON_CHECK_IN_RANGE of Lat/Lon to check in
        if (latDifference <= LAT_LON_CHECK_IN_RANGE && lonDifference <= LAT_LON_CHECK_IN_RANGE) {
            result = true;
        }

        return result;
    }

    /*
    Start location updates and get the last known GPS location
    MissingPermission is suppressed because the permission check is done in a reusable method which Lint doesn't understand.
    */
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        // Request permissions if we don't have them. Return if the permissions were not already granted.
        if (!requestPermissions()) {
            return;
        }

        // Request location updates
        startLocationUpdates();

        // Get the last GPS location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            startLocationUpdates();
        }
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d("onConnected", "Current location: " + String.valueOf(latitude) + ", " + String.valueOf(longitude));
        } else {
            Log.d("onConnected", "Location was null. If you are running on an emulator, you must first click Extended Controls > Location > Send");
        }
    }

    /*
    Start (request) location updates
    MissingPermission is suppressed because the permission check is done in a reusable method which Lint doesn't understand.
    */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Create the location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        // Request permissions if we don't have them. Return if the permissions were not already granted.
        if (!requestPermissions()) {
            Log.d("startLocationUpdates", "Permissions were not previously granted. Exiting startLocationUpdates().");
            return;
        }

        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("onConnectionSuspended", "Connection Suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("onConnectionFailed", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        MainActivity.location = location;
    }

    /*
    Requests FINE and COARSE location permissions if they haven't been granted
    Returns a boolean value indicating whether they were already granted before calling this method
    */
    private boolean requestPermissions() {
        boolean result = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);

            result = false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);

            result = false;
        }

        return result;
    }

    //endregion Location methods

    //region Saving and Loading

    /*
    Translates the Places database to a list of PlaceStates
    A PlaceState is the place name (key) and check in status boolean (value)
    This list is elsewhere saved to a local file for check-in state persistence
    */
    private static ArrayList<PlaceState> getPlaceStateList() {
        ArrayList<PlaceState> result = new ArrayList<>();

        for (Place p : getPlacesDatabase()) {
            result.add(new PlaceState(p.getName(), p.hasCheckedIn()));
        }

        return result;
    }

    /*
    Write the check-in states of the Places database to a local file
    */
    public static void savePlaceStates(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(PLACE_STATES_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(getPlaceStateList());
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Load the previous check-in states from a local file and apply it to the Places database
    Places must first have been loaded from the JSON file
    */
    private void loadPlaceStates() {
        try {
            ArrayList<PlaceState> placeStates;
            FileInputStream fis = getApplicationContext().openFileInput(PLACE_STATES_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            // There's no reason to complain about an unchecked cast here because
            // we only reach this statement if data has been written to this object by this app
            // and we know that the written data is always of the expected type
            //noinspection unchecked
            placeStates = (ArrayList<PlaceState>) is.readObject();
            is.close();
            fis.close();

            // Copy placeStates data to Places database
            for (PlaceState p : placeStates) {
                setPlaceState(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
    Apply the given PlaceState to its matching entry in Places database
    */
    private void setPlaceState(PlaceState state) {
        boolean foundPlace = false;

        for (Place p : places) {
            if (p.getName().equals(state.getName())) {
                p.setCheckedIn(state.isCheckedIn());
                foundPlace = true;
            }
        }

        if (!foundPlace) {
            Log.d("setPlaceState", "Could not find place with name: " + state.getName());
        }
    }

    //endregion Saving and Loading

}