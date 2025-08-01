package com.example.eveant.user.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class User implements Parcelable {
    private int id;
    private Profile profile;
    private String status;

    public User() {}

    public User(int id, Profile profile, String status) {
        this.id = id;
        this.profile = profile;
        this.status = status;
    }

    protected User(Parcel in) {
        id = in.readInt();
        profile = in.readParcelable(Profile.class.getClassLoader());
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(profile, flags);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
