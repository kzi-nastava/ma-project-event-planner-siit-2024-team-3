package com.example.eveant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewPager = findViewById(R.id.home_search_rectangle_layout);

        // Add your image resource IDs to the list
        List<SlideItem> slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30 NOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "11 DEC"));
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30 NOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "11 DEC"));

        ImagePagerAdapter adapter = new ImagePagerAdapter(this, slideItems);
        viewPager.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == adapter.getItemCount()) {
                    currentPage = 0; // Reset to first page.
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000); // Slide every 3 seconds.
            }
        };

        // Start auto-slide
        handler.postDelayed(runnable, 3000);

        // Optional: Stop auto-slide on user interaction
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position + 1;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Prevent memory leaks.
    }
}