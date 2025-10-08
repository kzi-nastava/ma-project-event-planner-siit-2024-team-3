package com.example.eveant.event;

import java.util.List;

class GetEventDTO {
    public int id;
    public String name;
    public String description;
    public String date;        // ISO-8601
    public String organizer;   // username
    public String status;      // EventStatus
    public EventTypeDTO eventType;
    public AddressDTO address;
    public Integer maxAttendance;
    public List<String> photos;
}

class CreatedEventDTO {
    public int id;
    public String organizer;
}

class UpdatedEventDTO {
    public int id;
    public String name;
    public String description;
    public String date;
    public Integer maxAttendance;
    public AddressDTO address;
    public EventTypeDTO eventType;
    public List<String> photos;
}

class CreateEventDTO {
    public String name;
    public String description;
    public String date;           // yyyy-MM-dd or ISO
    public Integer maxAttendance;
    public AddressDTO address;
    public String organizer;      // username
    public String eventType;      // name or DTO (server resolves)
    public List<String> photos;
}

class AddressDTO {
    public String street;
    public String city;
    public String postalNumber;
    public String country;
    public String houseNumber;
}

class EventTypeDTO {
    public Integer id;
    public String name;
    public String description;
    public Boolean active;
}

class GetInvitationDTO {
    public int id;
    public String email;
    public boolean accepted;
}

class CreatedInvitationDTO {
    public int id;
    public String email;
    public boolean accepted;
    public int eventId;
}

class CreateInvitationDTO {
    public String email;
    public String message;
}