package com.example.eveant.user.model;

public class Address {
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
