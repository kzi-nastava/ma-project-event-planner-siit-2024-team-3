package com.example.eveant.event;

import com.example.eveant.eventType.EventType;
import com.example.eveant.user.model.Address;

import java.util.ArrayList;
import java.util.List;

// Use this as the body type for your update call
public class EventUpdateDTO {
    public String name;
    public String description;
    public String date;                 // e.g. "yyyy-MM-dd'T'HH:mm:ss"
    public EventStatus status;
    public EventType eventType;         // <-- remove this line if your backend forbids changing type
    public Address address;
    public Integer maxAttendance;
    public List<String> photos;

    public static EventUpdateDTO from(Event e) {
        EventUpdateDTO dto = new EventUpdateDTO();
        dto.name          = e.name;
        dto.description   = e.description;
        dto.date          = e.date;
        dto.status        = e.status;
        dto.address       = e.address;
        dto.maxAttendance = e.maxAttendance;
        dto.photos        = (e.photos == null) ? null : new ArrayList<>(e.photos);
        return dto;
    }
}
