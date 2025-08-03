package com.example.eveant.service;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.EventType;
import com.example.eveant.service.model.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCreateViewModel extends ViewModel {
    private final MutableLiveData<Service> service = new MutableLiveData<>(new Service());

    public LiveData<Service> getService() {
        return service;
    }


    public void updateService(Service updatedService) {
        service.setValue(updatedService);
        Log.d(TAG, "updateService: "+service.getValue());
    }

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<EventType>> eventTypes = new MutableLiveData<>();

    private final MutableLiveData<List<EventType>> selectedEventTypes = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<EventType>> getSelectedEventTypes() {
        return selectedEventTypes;
    }

    public void updateSelectedEventTypes(List<EventType> selectedTypes) {
        selectedEventTypes.setValue(selectedTypes);
    }


    public LiveData<List<Category>> getCategoriesLiveData() {
        return categories;
    }

    public LiveData<List<EventType>> getEventTypesLiveData() {
        return eventTypes;
    }
    public void fetchCategories() {
        ServiceService serviceService = RetrofitClient.serviceService;
        serviceService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.setValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                categories.setValue(Collections.emptyList());
            }
        });
    }

    public void fetchEventTypes() {
        ServiceService serviceService = RetrofitClient.serviceService;
        serviceService.getEventTypes().enqueue(new Callback<List<EventType>>() {
            @Override
            public void onResponse(Call<List<EventType>> call, Response<List<EventType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<EventType>> call, Throwable t) {
                eventTypes.setValue(Collections.emptyList());
            }
        });
    }
}
