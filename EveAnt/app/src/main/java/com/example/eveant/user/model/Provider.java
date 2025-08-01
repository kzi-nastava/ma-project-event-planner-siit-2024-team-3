package com.example.eveant.user.model;

import android.os.Parcel;

public class Provider extends User {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private Address address;
    private String phoneNumber;
    private String gender;
    private Company company;

    public Provider() {}

    public Provider(int id, Profile profile, String status,
                    String firstName, String lastName, String dateOfBirth,
                    Address address, String phoneNumber, String gender,
                    Company company) {
        super(id, profile, status);
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.company = company;
    }

    protected Provider(Parcel in) {
        super(in);
        firstName = in.readString();
        lastName = in.readString();
        dateOfBirth = in.readString();
        address = in.readParcelable(Address.class.getClassLoader());
        phoneNumber = in.readString();
        gender = in.readString();
        company = in.readParcelable(Company.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(dateOfBirth);
        dest.writeParcelable(address, flags);
        dest.writeString(phoneNumber);
        dest.writeString(gender);
        dest.writeParcelable(company, flags);
    }

    public static final Creator<Provider> CREATOR = new Creator<Provider>() {
        @Override
        public Provider createFromParcel(Parcel in) {
            return new Provider(in);
        }

        @Override
        public Provider[] newArray(int size) {
            return new Provider[size];
        }
    };

    // Getters & Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
}
