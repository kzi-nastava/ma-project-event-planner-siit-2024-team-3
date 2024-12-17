package com.example.eveant.user.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;

public class User implements Parcelable {
    private int id;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private Address address;
    private String phoneNumber;
    private String gender;
    private Company company;
    private String role;
    // Constructors
    public User() {}

    public User(int id, String firstName, String lastName, String dateOfBirth, Address address, String phoneNumber, String gender, Company company, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.company = company;
        this.role = role;
        this.id = id;
    }
    protected User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        dateOfBirth = in.readString();
        address = in.readParcelable(Address.class.getClassLoader());
        phoneNumber = in.readString();
        gender = in.readString();
        company = in.readParcelable(Company.class.getClassLoader());
        role = in.readString();
        id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(dateOfBirth);
        dest.writeParcelable(address, flags);
        dest.writeString(phoneNumber);
        dest.writeString(gender);
        dest.writeParcelable(company, flags);
        dest.writeString(role);
        dest.writeInt(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    // Getters and Setters
    public int getId(){return id; }

    public void setId(int id){this.id = id;}
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Company getCompany(){
        return company;
    }

    public void setCompany (Company company){
        this.company = company;
    }
    public String getRole(){
        return role;
    }
    public void setRole (String role){
        this.role = role;
    }
}

