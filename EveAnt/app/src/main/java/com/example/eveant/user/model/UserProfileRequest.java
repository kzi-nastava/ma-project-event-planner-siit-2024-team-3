package com.example.eveant.user.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserProfileRequest implements Parcelable{
    private User createUserDTO;
    private Profile createProfileDTO;

    protected UserProfileRequest(Parcel in) {
        createUserDTO = in.readParcelable(User.class.getClassLoader());
        createProfileDTO = in.readParcelable(Profile.class.getClassLoader());
    }
    public UserProfileRequest(){}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) createUserDTO, flags);
        dest.writeParcelable((Parcelable) createProfileDTO, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UserProfileRequest> CREATOR = new Creator<UserProfileRequest>() {
        @Override
        public UserProfileRequest createFromParcel(Parcel in) {
            return new UserProfileRequest(in);
        }

        @Override
        public UserProfileRequest[] newArray(int size) {
            return new UserProfileRequest[size];
        }
    };

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
