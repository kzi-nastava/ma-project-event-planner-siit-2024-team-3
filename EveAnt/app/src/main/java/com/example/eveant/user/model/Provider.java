package com.example.eveant.user.model;

public class Provider extends User {
    private Company company;

    public Provider() {
        super();
        this.company = new Company(); // avoid null pointer
    }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
}
