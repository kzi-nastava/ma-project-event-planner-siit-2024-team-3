package com.example.eveant.service.model;


import java.io.Serializable;

public class CategoryDTO implements Serializable {
    private String name;
    private String description;
    private String createdBy;
    private CategoryStatus status;

    public CategoryDTO() { super(); }

    public CategoryDTO(String name) {
        this.name = name;
    }

    // get/set
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public CategoryStatus getStatus() { return status; }
    public void setStatus(CategoryStatus status) { this.status = status; }
}
