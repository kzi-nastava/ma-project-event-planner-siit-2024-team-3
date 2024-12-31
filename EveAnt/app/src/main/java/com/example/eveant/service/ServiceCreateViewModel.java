package com.example.eveant.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.Category;
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
    }



    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();

    public LiveData<List<Category>> getCategoriesLiveData() {
        return categories;
    }
    public void fetchCategories() {
        ApiService apiService = RetrofitClient.apiService;
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
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



}
