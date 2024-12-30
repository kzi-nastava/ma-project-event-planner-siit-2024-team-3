package com.example.eveant.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eveant.service.model.Service;

public class ServiceCreateViewModel extends ViewModel {
    private final MutableLiveData<Service> service = new MutableLiveData<>(new Service());

    public LiveData<Service> getService() {
        return service;
    }

    public void updateService(Service updatedService) {
        service.setValue(updatedService);
    }
}
