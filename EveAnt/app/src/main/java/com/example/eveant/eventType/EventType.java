package com.example.eveant.eventType;

import com.example.eveant.service.model.Category;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventType {
    private Integer id;
    private String name;
    private String description;

    @SerializedName("suggestedCategories")
    private List<Category> suggestedCategories;

    private Boolean active;
    private Boolean virtual;

    public EventType() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Category> getSuggestedCategories() { return suggestedCategories; }
    public void setSuggestedCategories(List<Category> suggestedCategories) { this.suggestedCategories = suggestedCategories; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getVirtual() { return virtual; }
    public void setVirtual(Boolean virtual) { this.virtual = virtual; }
}
