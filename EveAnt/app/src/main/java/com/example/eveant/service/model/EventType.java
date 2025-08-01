package com.example.eveant.service.model;

import java.io.Serializable;
import java.util.List;

public class EventType implements Serializable {
    private Long id;
    private String name;
    private String description;
    private List<Category> suggestedCategories;
    private boolean active;

    // Getteri i setteri
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<Category> getSuggestedCategories() { return suggestedCategories; }

    public void setSuggestedCategories(List<Category> suggestedCategories) { this.suggestedCategories = suggestedCategories; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
