package com.example.pete.tourguideapp;

import java.io.Serializable;

/*
This is a simplified version of the Place class
Which contains only the place name (key) and check in status boolean (value)
When the app saves user data, the state of the Places database is copied to a new list of this type
When the app loads user data, a list of this type is loaded.
The state(s) are applied to the Places database, whose static data is loaded from a .JSON file.
*/
class PlaceState implements Serializable {

    private String name;
    private boolean checkedIn;

    PlaceState(String name, boolean checkedIn) {
        setName(name);
        setCheckedIn(checkedIn);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    private void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }


}
