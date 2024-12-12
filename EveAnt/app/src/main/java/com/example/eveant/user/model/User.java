package com.example.eveant.user.model;

import java.time.LocalDate;

public class User {
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

    public User(String firstName, String lastName, String dateOfBirth, Address address, String phoneNumber, String gender, Company company, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.company = company;
        this.role = role;
    }

    // Getters and Setters
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

