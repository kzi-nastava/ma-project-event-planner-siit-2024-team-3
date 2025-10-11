package com.example.eveant.event;

import com.example.eveant.eventType.EventType;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Organizer;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
        public int id;
        public String name;
        public String description;
        public String date;
        public String organizer;
        public EventStatus status;
        public EventType eventType;
        public Address address;
        public Integer maxAttendance;
        public List<String> photos;

        public Event(String organizer, String name, String description, EventStatus status,
                     LocalDateTime date, int maxAttendance, Address address, List<String> photos, EventType eventType) {
    }

        public String getName() {
                return name;
        }

        public EventType getEventType() {
                return eventType;
        }

        public Address getAddress() {
                return address;
        }

        public String getDate() {
                return date;
        }

        public List<String> getPhotos() {
                return photos;
        }
}
