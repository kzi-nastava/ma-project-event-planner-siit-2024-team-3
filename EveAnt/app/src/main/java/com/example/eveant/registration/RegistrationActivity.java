package com.example.eveant.registration;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import android.view.View;
import android.widget.TextView;

import com.example.eveant.R;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new UsernamePasswordFragment());
            transaction.commit();
            updateProgress(1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity becoming visible to user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity now in foreground and interactive");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity partially obscured, releasing temporary resources");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity no longer visible to user, releasing resources");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: Activity is restarting, performing refresh tasks if needed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity is being destroyed, performing final cleanup");
    }
    public void updateProgress(int completedStep) {
        // Group all the ellipses into an array
        TextView[] ellipses = new TextView[] {
                findViewById(R.id.ellipse_1),
                findViewById(R.id.ellipse_2),
                findViewById(R.id.ellipse_3),
                findViewById(R.id.ellipse_4),
                findViewById(R.id.ellipse_6),
                findViewById(R.id.ellipse_7)
        };

        // Group all the lines into an array
        View[] lines = new View[] {
                findViewById(R.id.line_46),
                findViewById(R.id.line_47),
                findViewById(R.id.line_48),
                findViewById(R.id.line_49),
                findViewById(R.id.line_50),
                findViewById(R.id.line_51),
                findViewById(R.id.line_52)
        };

        resetProgress(ellipses, lines);

        // Update progress dynamically
        for (int i = 0; i < completedStep; i++) {
            if (i < ellipses.length) {
                ellipses[i].setBackgroundResource(R.drawable.rounded_corners_8599e0);
                ellipses[i].setTextColor(Color.WHITE);
            }
            if (i < lines.length) {
                lines[i].setBackgroundResource(R.drawable.line_completed);
            }
        }

        // Manage visibility for first/second half
        manageVisibility(completedStep, ellipses, lines);
    }

    private void resetProgress(TextView[] ellipses, View[] lines) {
        for (TextView ellipse : ellipses) {
            ellipse.setBackgroundResource(0);
            ellipse.setTextColor(Color.GRAY);
        }
        for (View line : lines) {
            line.setBackgroundResource(R.drawable.line_black);
        }
    }

    private void manageVisibility(int completedStep, TextView[] ellipses, View[] lines) {
        boolean showFirstHalf = completedStep <= 4;

        // Set visibility for ellipses
        for (int i = 0; i < ellipses.length; i++) {
            ellipses[i].setVisibility(showFirstHalf && i < 4 ? View.VISIBLE : View.INVISIBLE);
        }

        // Set visibility for lines
        for (int i = 0; i < lines.length; i++) {
            lines[i].setVisibility(showFirstHalf && i < 4 ? View.VISIBLE : View.INVISIBLE);
        }

        // Manage second half visibility
        for (int i = 4; i < ellipses.length; i++) {
            ellipses[i].setVisibility(!showFirstHalf ? View.VISIBLE : View.INVISIBLE);
        }
        for (int i = 4; i < lines.length; i++) {
            lines[i].setVisibility(!showFirstHalf ? View.VISIBLE : View.INVISIBLE);
        }
    }
}