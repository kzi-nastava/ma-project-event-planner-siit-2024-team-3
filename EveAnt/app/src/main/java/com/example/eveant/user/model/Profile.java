package com.example.eveant.user.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {
    private String username;
    private String password;
    private String email;

    // Constructors
    public Profile() {}

    public Profile(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
    }
    protected Profile(Parcel in) {
        username = in.readString();
        password = in.readString();
        email = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(email);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    }

