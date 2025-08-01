package com.example.eveant.user.model;

import android.os.Parcel;

public class Organizer extends User {
    private String organizationName;
    private String phoneNumber;

    public Organizer() {}

    public Organizer(int id, Profile profile, String status,
                     String organizationName, String phoneNumber) {
        super(id, profile, status);
        this.organizationName = organizationName;
        this.phoneNumber = phoneNumber;
    }

    protected Organizer(Parcel in) {
        super(in);
        organizationName = in.readString();
        phoneNumber = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(organizationName);
        dest.writeString(phoneNumber);
    }

    public static final Creator<Organizer> CREATOR = new Creator<Organizer>() {
        @Override
        public Organizer createFromParcel(Parcel in) {
            return new Organizer(in);
        }

        @Override
        public Organizer[] newArray(int size) {
            return new Organizer[size];
        }
    };

    // Getters & Setters
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
