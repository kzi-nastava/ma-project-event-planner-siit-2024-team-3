package com.example.eveant.user.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Company implements Parcelable {
    private String companyName;
    private String email;
    private Address address;
    private String contact;
    private String description;
    private List<String> photos;

    // Constructors
    public Company() {}

    public Company(String companyName, String email, Address address, String contact, String description, List<String> photos) {
        this.companyName = companyName;
        this.email = email;
        this.address = address;
        this.contact = contact;
        this.description = description;
        this.photos = photos;
    }
    protected Company(Parcel in) {
        companyName = in.readString();
        email = in.readString();
        address = in.readParcelable(Address.class.getClassLoader());
        contact = in.readString();
        description = in.readString();
        photos = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(companyName);
        dest.writeString(email);
        dest.writeParcelable(address, flags);
        dest.writeString(contact);
        dest.writeString(description);
        dest.writeStringList(photos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };


    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String name) { this.companyName = name; }

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
