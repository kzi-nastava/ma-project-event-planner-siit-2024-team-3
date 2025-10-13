package com.example.eveant.event.agenda;

import com.example.eveant.user.model.Address;

public class Activity {
    public Integer id;
    public int event;              // eventId
    public String name;
    public String description;
    public String startTime;       // ISO-8601
    public String endTime;         // ISO-8601
    public String color;           // "#RRGGBB"
    public Address address;
}

class ActivityDTO {
    public int event;              // eventId
    public String name;
    public String description;
    public String startTime;       // ISO-8601
    public String endTime;         // ISO-8601
    public String color;           // "#RRGGBB"
    public Address address;
}


