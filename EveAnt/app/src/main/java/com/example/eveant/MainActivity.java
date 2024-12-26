package com.example.eveant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.eveant.service.model.ApiService;
import com.example.eveant.service.model.Service;
import com.example.eveant.user.UserClientUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final MutableLiveData<String> errorMessage = new MutableLiveData<>();
        final MutableLiveData<ArrayList<Service>> serviceLiveData = new MutableLiveData<>();
        Call<ArrayList<Service>> call = RetrofitClient.apiService.getAllServices();
        call.enqueue(new Callback<ArrayList<Service>>() {
            @Override
            public void onResponse(Call<ArrayList<Service>> call, Response<ArrayList<Service>> response) {
                if (response.isSuccessful()) {
                    serviceLiveData.postValue(response.body());
                    List<Service> services = response.body();
                    for (Service service : services) {
                        Log.d("MainActivity", "Service: " + service.getName());
                    }
                } else {
                    errorMessage.postValue("Failed to fetch products. Code: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<ArrayList<Service>> call, Throwable t) {
                errorMessage.postValue("Failed to fetch products. Error: " + t.getMessage());
                Log.e("MainActivity", "Fetch error: ", t);
            }

        });


        /*-----------------------------------------------------------*/

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "USER");

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (role.equals("ORGANIZER")) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.organizer_menu);
        } else if (role.equals("PROVIDER")) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.provider_menu);
        } else if (role.equals("ADMIN")) {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.admin_menu);
        } else {
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        }
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    public NavController getNavController() {
        return navController;
    }
}
