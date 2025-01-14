package com.example.eveant.budget;

public class ItemDTO {
    private int category;
    private Long maxPrice;
    private int offer;
    private Long price;

    public ItemDTO(int id, long priceMax, int offer, long price) {
        this.category=id;
        this.maxPrice=priceMax;
        this.offer=offer;
        this.price=price;

    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public Long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getOffer() {
        return offer;
    }

    public void setOffer(int offer) {
        this.offer = offer;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}
