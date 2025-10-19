package com.example.eveant.service.model;

public class OfferDTO {
    private Integer id;
    private String name;
    private Double price;
    private Double discount;
    private String type; // "service" or "product"

    public OfferDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
