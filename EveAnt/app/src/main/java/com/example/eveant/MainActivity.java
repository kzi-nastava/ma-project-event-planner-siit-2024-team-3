package com.example.eveant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.model.ServiceMapper;
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

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        String role = getIntent().getStringExtra("role");
        navController = ((NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment)).getNavController();



        /*-----------------------------------------------------------*/

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        role = sharedPreferences.getString("role", Constants.ROLE_USER);

        // Dinamičko podešavanje donjeg menija po ulozi
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.getMenu().clear();

        switch (role) {
            case Constants.ROLE_ADMIN:
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin);
                break;
            case Constants.ROLE_PROVIDER:
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_pup);
                break;
            case Constants.ROLE_ORGANIZER:
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_organizer);
                break;
            default:
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
                break;
        }
        Log.d("MainActivity", "Korisnik ima ulogu: " + role);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

    }


    public NavController getNavController() {
        return navController;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void setupBottomNavigationForPUP() {
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_pup);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void setupBottomNavigationForOrganizer() {
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_organizer);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void setupBottomNavigationForAdmin() {
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_admin);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

}
