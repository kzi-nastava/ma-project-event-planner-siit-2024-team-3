package com.example.eveant.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.user.security.AuthManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private Button muteButton;
    private TextView emptyText;

    private boolean isMuted = false;
    private AuthManager authManager;
    private String userEmail;

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize AuthManager and get user email
        authManager = AuthManager.getInstance(requireContext());
        userEmail = authManager.getEmail();

        recyclerView = view.findViewById(R.id.notificationsRecycler);
        muteButton = view.findViewById(R.id.muteButton);
        emptyText = view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notification -> markAsRead(notification.getId()));
        recyclerView.setAdapter(adapter);

        loadNotifications();
        checkMuteStatus();

        muteButton.setOnClickListener(v -> toggleMute());
    }

    private void loadNotifications() {
        if (userEmail == null) {
            Log.e("Notif", "User email is null");
            return;
        }

        RetrofitClient.notificationService.getAllNotifications(userEmail)
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Notification> notifications = response.body();
                            if (notifications.isEmpty()) {
                                emptyText.setVisibility(TextView.VISIBLE);
                                recyclerView.setVisibility(RecyclerView.GONE);
                            } else {
                                emptyText.setVisibility(TextView.GONE);
                                recyclerView.setVisibility(RecyclerView.VISIBLE);
                                adapter.setNotifications(notifications);
                            }
                        } else {
                            Log.e("Notif", "Failed to load notifications: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        Log.e("Notif", "Error loading notifications", t);
                    }
                });
    }

    private void markAsRead(int id) {
        RetrofitClient.notificationService.markAsRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.removeNotification(id);
                } else {
                    Log.e("Notif", "Failed to mark notification as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Notif", "Error marking notification as read", t);
            }
        });
    }

    private void checkMuteStatus() {
        if (userEmail == null) return;

        RetrofitClient.notificationService.getMuteStatus(userEmail)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            isMuted = response.body();
                            updateMuteButtonText();
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.e("Notif", "Error checking mute status", t);
                    }
                });
    }

    private void toggleMute() {
        if (userEmail == null) return;

        RetrofitClient.notificationService.toggleMute(userEmail)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            isMuted = !isMuted;
                            updateMuteButtonText();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Notif", "Error toggling mute", t);
                    }
                });
    }

    private void updateMuteButtonText() {
        muteButton.setText(isMuted ? "Unmute Notifications" : "Mute Notifications");
    }
}