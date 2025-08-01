package com.example.eveant.user.model;

import android.os.Parcel;

public class Admin extends User {
    private String firstName;
    private String lastName;

    public Admin() {}

    public Admin(int id, Profile profile, String status,
                 String firstName, String lastName) {
        super(id, profile, status);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    protected Admin(Parcel in) {
        super(in);
        firstName = in.readString();
        lastName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(firstName);
        dest.writeString(lastName);
    }

    public static final Creator<Admin> CREATOR = new Creator<Admin>() {
        @Override
        public Admin createFromParcel(Parcel in) {
            return new Admin(in);
        }

        @Override
        public Admin[] newArray(int size) {
            return new Admin[size];
        }
    };

    // Getters & Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
