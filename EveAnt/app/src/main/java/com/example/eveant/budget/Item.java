package com.example.eveant.budget;

import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.Offer;

public class Item {
    private Long id;
    private String name;
    private Category category;
    private Long maxPrice;
    private Offer offer;

    // Default constructor
    public Item() {}

    // Parameterized constructor
    public Item(Long id, String name, Category category, Long maxPrice, Offer offer) {
        this.id = id;
        this.category = category;
        this.name=name;
        this.maxPrice = maxPrice;
        this.offer = offer;
    }

    // Getters and Setters
    public int getId() {
        return Math.toIntExact(id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
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

}
