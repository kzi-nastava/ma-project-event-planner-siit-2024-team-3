package com.example.eveant.service.model;

import com.example.eveant.eventType.EventType;

import java.util.Date;
import java.util.List;

public class Offer {
    private Integer id;
    private String provider;
    private Date lastModification;
    private String name;
    private Category category;
    private String description;
    private List<EventType> eventTypes;
    private Long price;
    private Integer discount;
    private Boolean visible;
    private OfferStatus status;
    private List<String> photos;

    public Offer() {
    }

    public Offer(Integer id, String provider, Date lastModification, String name, Category category,
                 String description, List<EventType> eventTypes, Long price, int discount,
                 Boolean visible, OfferStatus status, List<String> photos) {
        this.id = id;
        this.provider = provider;
        this.lastModification = lastModification;
        this.name = name;
        this.category = category;
        this.description = description;
        this.eventTypes = eventTypes;
        this.price = price;
        this.discount = discount;
        this.visible = visible;
        this.status = status;
        this.photos = photos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<EventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", category=" + category +
                '}';
    }
}
