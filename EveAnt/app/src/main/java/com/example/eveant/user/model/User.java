package com.example.eveant.user.model;

public class User {
    private Integer id;
    private Profile profile;
    private String status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // PROXY: firstName
    public String getFirstName() {
        if (this instanceof Provider) {
            return ((Provider) this).getFirstName();
        } else if (this instanceof Organizer) {
            return ((Organizer) this).getFirstName();
        }
        return "";
    }

    public void setFirstName(String firstName) {
        if (this instanceof Provider) {
            ((Provider) this).setFirstName(firstName);
        } else if (this instanceof Organizer) {
            ((Organizer) this).setFirstName(firstName);
        }
    }

    // PROXY: lastName
    public String getLastName() {
        if (this instanceof Provider) {
            return ((Provider) this).getLastName();
        } else if (this instanceof Organizer) {
            return ((Organizer) this).getLastName();
        }
        return "";
    }

    public void setLastName(String lastName) {
        if (this instanceof Provider) {
            ((Provider) this).setLastName(lastName);
        } else if (this instanceof Organizer) {
            ((Organizer) this).setLastName(lastName);
        }
    }

    // PROXY: dateOfBirth
    public String getDateOfBirth() {
        if (this instanceof Provider) {
            return ((Provider) this).getDateOfBirth();
        } else if (this instanceof Organizer) {
            return ((Organizer) this).getDateOfBirth();
        }
        return "";
    }

    public void setDateOfBirth(String dateOfBirth) {
        if (this instanceof Provider) {
            ((Provider) this).setDateOfBirth(dateOfBirth);
        } else if (this instanceof Organizer) {
            ((Organizer) this).setDateOfBirth(dateOfBirth);
        }
    }

    // PROXY: phoneNumber
    public String getPhoneNumber() {
        if (this instanceof Provider) {
            return ((Provider) this).getPhoneNumber();
        } else if (this instanceof Organizer) {
            return ((Organizer) this).getPhoneNumber();
        }
        return "";
    }

    public void setPhoneNumber(String phoneNumber) {
        if (this instanceof Provider) {
            ((Provider) this).setPhoneNumber(phoneNumber);
        } else if (this instanceof Organizer) {
            ((Organizer) this).setPhoneNumber(phoneNumber);
        }
    }

    // PROXY: address
    public Address getAddress() {
        if (this instanceof Provider) {
            return ((Provider) this).getAddress();
        } else if (this instanceof Organizer) {
            return ((Organizer) this).getAddress();
        }
        return new Address();
    }

    public void setAddress(Address address) {
        if (this instanceof Provider) {
            ((Provider) this).setAddress(address);
        } else if (this instanceof Organizer) {
            ((Organizer) this).setAddress(address);
        }
    }

    // PROXY: username (iz Profile)
    public String getUsername() {
        return profile != null ? profile.getUsername() : "";
    }

    public void setUsername(String username) {
        if (profile == null) profile = new Profile();
        profile.setUsername(username);
    }

    // PROXY: email (iz Profile)
    public String getEmail() {
        return profile != null ? profile.getEmail() : "";
    }

    public void setEmail(String email) {
        if (profile == null) profile = new Profile();
        profile.setEmail(email);
    }
    public void setGender(String gender) {
        if (this instanceof Provider) {
            ((Provider) this).setGender(gender);
        } else if (this instanceof Organizer) {
            ((Organizer) this).setGender(gender);
        }
    }
    public String getGender() {
        if (this instanceof Provider) {
            return ((Provider) this).getGender();
        } else if (this instanceof Organizer) {
            return ((Organizer) this).getGender();
        }
        return "";
    }

}
