package com.example.eveant.user.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Address implements Parcelable {
    private String country;
    private String city;
    private String street;
    private String houseNumber;
    private String postalNumber;

    // Constructors
    public Address() {}

    public Address(String country, String city, String street, String houseNumber, String postalNumber) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalNumber = postalNumber;
    }
    protected Address(Parcel in) {
        country = in.readString();
        city = in.readString();
        street = in.readString();
        houseNumber = in.readString();
        postalNumber = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(country);
        dest.writeString(city);
        dest.writeString(street);
        dest.writeString(houseNumber);
        dest.writeString(postalNumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
    // Getters and Setters
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

    public String getPostalNumber() { return postalNumber; }
    public void setPostalNumber(String postalNumber) { this.postalNumber = postalNumber; }
}
