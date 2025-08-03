package com.example.eveant.service.model;


import java.io.Serializable;
import java.util.List;

public class EventTypeDTO implements Serializable {
    private String name;
    private String description;
    private List<CategoryDTO> suggestedCategories;
    private Boolean active;
    private Boolean virtual;

    public EventTypeDTO() { super(); }

    public EventTypeDTO(String name) {
        this.name = name;
    }

    // get/set
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<CategoryDTO> getSuggestedCategories() { return suggestedCategories; }
    public void setSuggestedCategories(List<CategoryDTO> suggestedCategories) { this.suggestedCategories = suggestedCategories; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getVirtual() { return virtual; }
    public void setVirtual(Boolean virtual) { this.virtual = virtual; }
}
