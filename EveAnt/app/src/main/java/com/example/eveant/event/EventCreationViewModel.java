package com.example.eveant.event;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eveant.eventType.EventType;

public class EventCreationViewModel extends ViewModel {
    private final MutableLiveData<EventType> selectedType = new MutableLiveData<>();
    private final MutableLiveData<Integer> eventId = new MutableLiveData<>();
    private final MutableLiveData<java.time.LocalDate> eventDate = new MutableLiveData<>(); // NEW
    private final MutableLiveData<Boolean> isPublic = new MutableLiveData<>(false);

    public void setSelectedType(EventType et) { selectedType.setValue(et); }
    public LiveData<EventType> getSelectedType() { return selectedType; }

    public void setEventId(Integer id) { eventId.setValue(id); }
    public LiveData<Integer> getEventId() { return eventId; }
    public void setEventDate(java.time.LocalDate d) { eventDate.setValue(d); }   // NEW
    public LiveData<java.time.LocalDate> getEventDate() { return eventDate; }
    public void setIsPublic(boolean value) { isPublic.setValue(value); }
    public LiveData<Boolean> getIsPublic() { return isPublic; }
}
