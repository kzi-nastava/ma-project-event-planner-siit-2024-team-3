package com.example.eveant;

public class SlideItem {
    private final int imageResId;
    private final String title;
    private final String location;
    private final String time;
    private final String date;

    public SlideItem(int imageResId, String title, String location, String time, String date) {
        this.imageResId = imageResId;
        this.title = title;
        this.location = location;
        this.time = time;
        this.date = date;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
