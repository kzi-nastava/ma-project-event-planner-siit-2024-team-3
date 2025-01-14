package com.example.eveant.budget;

import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.Offer;

public class Item {
    private Long id;
    private Budget budget;
    private Category category;
    private Long maxPrice;
    private Offer offer;
    private Long price;

    // Default constructor
    public Item() {}

    // Parameterized constructor
    public Item(Long id, Budget budget, Category category, Long maxPrice, Offer offer, Long price) {
        this.id = id;
        this.budget = budget;
        this.category = category;
        this.maxPrice = maxPrice;
        this.offer = offer;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}
