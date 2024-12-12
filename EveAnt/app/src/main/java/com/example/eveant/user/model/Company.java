package com.example.eveant.user.model;

import java.util.List;

public class Company {
    private String name;
    private String email;
    private Address address;
    private String contact;
    private String description;
    private List<String> photos;

    // Constructors
    public Company() {}

    public Company(String name, String email, Address address, String contact, String description, List<String> photos) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.contact = contact;
        this.description = description;
        this.photos = photos;
    }

    // Getters and Setters
    public String getCompanyName() { return name; }
    public void setCompanyName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
}
