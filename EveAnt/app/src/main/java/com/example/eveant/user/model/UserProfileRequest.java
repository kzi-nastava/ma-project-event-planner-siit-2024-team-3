package com.example.eveant.user.model;

public class UserProfileRequest {
    private User createUserDTO;
    private Profile createProfileDTO;

    // Constructors
    public UserProfileRequest() {}

    public UserProfileRequest(User createUserDTO, Profile createProfileDTO) {
        this.createUserDTO = createUserDTO;
        this.createProfileDTO = createProfileDTO;
    }

    // Getters and Setters
    public User getCreateUserDTO() {
        return createUserDTO;
    }

    public void setCreateUserDTO(User createUserDTO) {
        this.createUserDTO = createUserDTO;
    }

    public Profile getCreateProfileDTO() {
        return createProfileDTO;
    }

    public void setCreateProfileDTO(Profile createProfileDTO) {
        this.createProfileDTO = createProfileDTO;
    }
}
