package com.example.eveant.service.model;

import java.io.Serializable;

public class Category implements Serializable {
    private int id;
    private String name;
    private String description;
    private String createdBy;
    private CategoryStatus status;

    public Category() {
    }

    public Category(String name){
        this.name = name;
        /*this.createdBy = createdBy;*/
        this.status = CategoryStatus.SUGGESTED;
    }

    public Category(int id, String name, String description, String createdBy, CategoryStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.status = status;
    }

    // Geteri i seteri
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public void setStatus(CategoryStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", status=" + status +
                '}';
    }
}
