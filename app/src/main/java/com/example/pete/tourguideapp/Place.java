package com.example.pete.tourguideapp;

import android.content.Context;

/*
 * This is the class for all Places
 * The Constructor accepts common Place attributes
 * There are Builder methods for the specific Place categories (Park, Landmark, etc)
 * */
public class Place {

    public enum PlaceTypes {PARK, RESTAURANT, LANDMARK, VENUE}

    // Common Place attributes
    private PlaceTypes type;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private String website;
    private String hours;

    // Park
    private double acres;
    private String trailLength;

    // Landmark
    private String history;

    // Restaurant
    private String typeOfFood;

    // Venue
    private String upcomingShows;

    // Check in
    private boolean checkedIn;

    Place(PlaceTypes type, String name, String description, String address, String phoneNumber, String website, String hours) {
        setType(type);
        setName(name);
        setDescription(description);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setWebsite(website);
        setHours(hours);
    }

    //region getters and setters for common Place attributes

    /*
     * Each Place has a Photo
     * */
    public int getPhotoResourceID(Context context) {
        String name_with_underscores = name.toLowerCase().replace(" ", "_");
        // Conform to the android resource naming conventions of only a-z, 0-9, and underscores
        name_with_underscores = name_with_underscores.replaceAll("[^a-z0-9_]", "");
        return context.getResources().getIdentifier(type.toString().toLowerCase() + "_" + name_with_underscores + "_photo", "drawable", context.getPackageName());
    }

    public PlaceTypes getType() {
        return type;
    }

    private void setType(PlaceTypes type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    private void setWebsite(String website) {
        this.website = website;
    }

    public String getHours() {
        return hours;
    }

    private void setHours(String hours) {
        this.hours = hours;
    }

    //endregion

    //region getters and setters for category-specific Place attributes

    public double getAcres() {
        return acres;
    }

    private void setAcres(double acres) {
        this.acres = acres;
    }

    public String getTrailLength() {
        return trailLength;
    }

    private void setTrailLength(String trailLength) {
        this.trailLength = trailLength;
    }

    public String getHistory() {
        return history;
    }

    private void setHistory(String history) {
        this.history = history;
    }

    public String getTypeOfFood() {
        return typeOfFood;
    }

    private void setTypeOfFood(String typeOfFood) {
        this.typeOfFood = typeOfFood;
    }

    public String getUpcomingShows() {
        return upcomingShows;
    }

    private void setUpcomingShows(String upcomingShows) {
        this.upcomingShows = upcomingShows;
    }

    //endregion

    //region builders for category-specific Place attributes

    public void makePark(double acres, String trailLength) {
        setAcres(acres);
        setTrailLength(trailLength);
    }

    public void makeLandmark(String history) {
        setHistory(history);
    }

    public void makeRestaurant(String typeOfFood) {
        setTypeOfFood(typeOfFood);
    }

    public void makeVenue(String upcomingShows) {
        setUpcomingShows(upcomingShows);
    }

    //endregion

    public boolean hasCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
