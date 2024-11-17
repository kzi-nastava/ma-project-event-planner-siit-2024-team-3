package com.example.eveant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.home_search_rectangle_layout);

        // Add your image resource IDs to the list
        List<SlideItem> slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30\nNOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "23\nDEC"));
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30\nNOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "23\nDEC"));

        ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), slideItems);
        viewPager.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == adapter.getItemCount()) {
                    currentPage = 0; // Reset to first page
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000); // Slide every 3 seconds
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
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable); // Prevent memory leaks
    }
}
