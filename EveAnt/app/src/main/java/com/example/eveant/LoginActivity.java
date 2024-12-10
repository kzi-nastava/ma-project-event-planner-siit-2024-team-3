package com.example.eveant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eveant.R;
import com.example.eveant.registration.RegistrationActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.login);
        Button navigateRegistrationButton = findViewById(R.id.goToRegister);

        // Navigate to the Registration screen
        navigateRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Example of authentication logic (replace with your actual logic)
                String username = ((EditText) findViewById(R.id.username)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();

                // Mock user login check (replace with API call or database query)
                User loggedInUser = authenticateUser(username, password);
                if (loggedInUser != null) {
                    // Save user role in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("role", loggedInUser.getRole());
                    editor.apply();

                    // Navigate to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close LoginActivity
                } else {
                    // Show error message
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Mock authentication method (replace with actual implementation)
    private User authenticateUser(String username, String password) {
        // Replace this with your actual authentication logic
        if ("admin".equals(username) && "password".equals(password)) {
            return new User("admin", "admin");
        } else if ("organizer".equals(username) && "password".equals(password)) {
            return new User("organizer", "organizer");
        } else if ("provider".equals(username) && "password".equals(password)) {
            return new User("provider", "provider");
        }
        return null;
    }

    // Mock User class (replace with your actual User model)
    class User {
        private String username;
        private String role;

        User(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getRole() {
            return role;
        }
    }
}
