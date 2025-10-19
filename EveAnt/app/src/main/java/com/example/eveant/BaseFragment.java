package com.example.eveant;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eveant.notification.NotificationFragment;

public abstract class BaseFragment extends Fragment {

    /** Must return the Activity container id used by FragmentTransactions. */
    protected abstract @IdRes int getMainContainerId();

    /** Create a new Home fragment instance. */
    protected abstract @NonNull Fragment createHomeFragment();

    protected void setupBackBar(@NonNull View root) {
        View back = root.findViewById(R.id.back_button);
        if (back != null) back.setOnClickListener(v -> popOrGoHome());

        View home = root.findViewById(R.id.notification_button);
        if (home != null) home.setOnClickListener(v -> goHome());

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() { popOrGoHome(); }
                }
        );
    }

    protected void popOrGoHome() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            goHome();
        }
    }

    protected void goHome() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(getMainContainerId(), new NotificationFragment())
                .commit();
    }

}
