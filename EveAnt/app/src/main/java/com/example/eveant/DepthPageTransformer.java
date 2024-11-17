package com.example.eveant;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        if (position < -1) { // Page is off-screen to the left.
            page.setAlpha(0);
        } else if (position <= 1) { // Page is visible.
            page.setAlpha(1);
            page.setPivotX(position < 0 ? page.getWidth() : 0);
            page.setPivotY(page.getHeight() / 2);
            page.setRotationY(-90 * position);
        } else { // Page is off-screen to the right.
            page.setAlpha(0);
        }
    }
}
