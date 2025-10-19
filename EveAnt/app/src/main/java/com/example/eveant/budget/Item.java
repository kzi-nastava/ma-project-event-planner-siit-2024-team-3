package com.example.eveant.budget;

import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.OfferDTO;

import java.io.Serializable;

public class Item  implements Serializable {
    private Integer id;
    private String name;
    private Category category;
    private Double maxPrice;
    private OfferDTO offer;

    public Item() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public OfferDTO getOffer() { return offer; }
    public void setOffer(OfferDTO offer) { this.offer = offer; }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + (category != null ? category.getName() : "null") +
                ", maxPrice=" + maxPrice +
                ", offer=" + (offer != null ? offer.getName() : "null") +
                '}';
    }
}
