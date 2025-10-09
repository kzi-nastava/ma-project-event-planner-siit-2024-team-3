package com.example.eveant.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    //private Organizer organizer;
    private String name;
    private String description;
    private EventStatus status;
    private LocalDateTime date;
    private int maxAttendance;
    //private ArrayList<User> guestList;
    private List<String> photos;
    private int clicks;

    public Event() {}

}

