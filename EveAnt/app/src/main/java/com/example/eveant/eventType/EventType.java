package com.example.eveant.eventType;

import com.example.eveant.service.model.Category;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Type;
import java.util.List;

public class EventType implements Parcelable {
    private Integer id;                 // <-- will NOT be written/read from Parcel
    private String name;
    private String description;

    @SerializedName("suggestedCategories")
    private List<Category> suggestedCategories;

    private Boolean active;
    private Boolean virtual;

    public EventType() {}

    // ===== Parcelable impl (exclude id) =====
    protected EventType(Parcel in) {
        name = in.readString();
        description = in.readString();

        String catJson = in.readString();
        if (catJson != null) {
            Type t = new TypeToken<List<Category>>(){}.getType();
            suggestedCategories = new Gson().fromJson(catJson, t);
        }

        active = readNullableBoolean(in);
        virtual = readNullableBoolean(in);
        // id intentionally NOT restored -> remains null
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);

        // Serialize categories via JSON so Category needn't be Parcelable
        String catJson = (suggestedCategories != null)
                ? new Gson().toJson(suggestedCategories)
                : null;
        dest.writeString(catJson);

        writeNullableBoolean(dest, active);
        writeNullableBoolean(dest, virtual);
        // id intentionally NOT written
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<EventType> CREATOR = new Creator<EventType>() {
        @Override public EventType createFromParcel(Parcel in) { return new EventType(in); }
        @Override public EventType[] newArray(int size) { return new EventType[size]; }
    };

    private static void writeNullableBoolean(Parcel dest, Boolean b) {
        // -1=null, 0=false, 1=true
        int v = (b == null) ? -1 : (b ? 1 : 0);
        dest.writeInt(v);
    }

    private static Boolean readNullableBoolean(Parcel in) {
        int v = in.readInt();
        if (v == -1) return null;
        return v == 1;
    }

    // ===== Getters/Setters =====
    public Integer getId() { return id; }                 // will be null after parcel/unparcel
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Category> getSuggestedCategories() { return suggestedCategories; }
    public void setSuggestedCategories(List<Category> suggestedCategories) { this.suggestedCategories = suggestedCategories; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getVirtual() { return virtual; }
    public void setVirtual(Boolean virtual) { this.virtual = virtual; }
}
