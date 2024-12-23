package com.example.eveant.service;

public class Service {
    private String name;
    private int category;
    private double price;
    private boolean isAvailable;
    private String imageUrl;

    // Constructor, getters, and setters
    public Service(String name, int category, double price, boolean isAvailable, String imageUrl) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public int getCategory() { return category; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return isAvailable; }
    public String getImageUrl() { return imageUrl; }
}
