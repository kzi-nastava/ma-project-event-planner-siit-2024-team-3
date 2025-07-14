package com.example.eveant.budget;

import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.Offer;

public class ItemDTO {
    private String name;
    private Category category;
    private Long maxPrice;
    private Offer offer;

    // Default constructor
    public ItemDTO() {}

    // Parameterized constructor
    public ItemDTO(String name, Category category, Long maxPrice, Offer offer) {
        this.category = category;
        this.name=name;
        this.maxPrice = maxPrice;
        this.offer = offer;
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
