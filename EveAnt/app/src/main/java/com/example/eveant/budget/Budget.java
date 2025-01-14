package com.example.eveant.budget;

import java.util.List;

public class Budget {
    private int id;
    private Event event;
    private List<Item> items;
    private Long totalBudget;

    public Budget() {}

    public Budget(int id, Event event, List<Item> items, Long totalBudget) {
        this.id = id;
        this.event = event;
        this.items = items;
        this.totalBudget = totalBudget;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Long getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(Long totalBudget) {
        this.totalBudget = totalBudget;
    }
}
