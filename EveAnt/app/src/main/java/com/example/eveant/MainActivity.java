package com.example.eveant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.eveant.service.ServiceAdapter;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.model.ServiceMapper;
import com.example.eveant.websocket.NotificationHelper;
import com.example.eveant.websocket.NotificationWebSocketListener;
import com.example.eveant.websocket.WebSocketHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.ui.AppBarConfiguration;


public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private DrawerLayout drawerLayout;
    private AppBarConfiguration appBarConfiguration;


    private WebSocketHandler webSocketHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// Navigation setup
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

// Povezujemo bočni meni i donju navigaciju
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.budgetList, R.id.servicesViewFragment,
                R.id.categoryFragment, R.id.chatFragment)
                .setOpenableLayout(drawerLayout)
                .build();

// Povezujemo toolbar (za hamburger ikonu)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        webSocketHandler = new WebSocketHandler();
        webSocketHandler.connect(new NotificationWebSocketListener() {
            @Override
            public void showNotification(String message) {
                NotificationHelper.showNotification(MainActivity.this, "New Notification", message);
            }
        });
        final MutableLiveData<String> errorMessage = new MutableLiveData<>();
        final MutableLiveData<ArrayList<Service>> serviceLiveData = new MutableLiveData<>();

        Call<ArrayList<ServiceDTO>> call = RetrofitClient.serviceService.getAllServices();
        call.enqueue(new Callback<ArrayList<ServiceDTO>>() {
            @Override
            public void onResponse(Call<ArrayList<ServiceDTO>> call, Response<ArrayList<ServiceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceDTO> dtoList = response.body();
                    ArrayList<Service> services = new ArrayList<>();
                    for (ServiceDTO dto : dtoList) {
                        services.add(ServiceMapper.INSTANCE.toEntity(dto));
                        Log.d("MainActivity", "Service: " + dto.getName());
                    }
                    serviceLiveData.postValue(services);
                } else {
                    errorMessage.postValue("Failed to fetch products. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ServiceDTO>> call, Throwable t) {
                errorMessage.postValue("Failed to fetch products. Error: " + t.getMessage());
                Log.e("MainActivity", "Fetch error: ", t);
            }
        });


        /*-----------------------------------------------------------*/

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "USER");

        /*NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);*/
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketHandler.closeConnection();
    }
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

}
