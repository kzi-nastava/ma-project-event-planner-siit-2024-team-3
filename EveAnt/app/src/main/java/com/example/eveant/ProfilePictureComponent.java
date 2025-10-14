package com.example.eveant;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.example.eveant.R;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.security.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePictureComponent extends RelativeLayout {

    private ImageView avatar;
    private LinearLayout profileMenu;
    private Button btnBlock, btnReport;
    private RelativeLayout profileContainer;

    private Profile profile;
    private String currentUserEmail;
    private boolean isBlocked = false;
    private boolean menuOpen = false;

    private UserService userService;
    private static final String TAG = "ProfilePictureComponent";

    public ProfilePictureComponent(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ProfilePictureComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProfilePictureComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.profile_picture, this, true);

        // Initialize views
        avatar = findViewById(R.id.avatar);
        profileMenu = findViewById(R.id.profile_menu);
        btnBlock = findViewById(R.id.btn_block);
        btnReport = findViewById(R.id.btn_report);
        profileContainer = findViewById(R.id.profile_container);

        // Initialize services
        userService = RetrofitClient.retrofit.create(UserService.class);

        // Get current user email using context
        AuthManager auth = AuthManager.getInstance(context);
        currentUserEmail = auth.getEmail();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Profile container click
        profileContainer.setOnClickListener(v -> {
            if (!isOwnProfile()) {
                toggleMenu();
            }
        });

        // Block button
        btnBlock.setOnClickListener(v -> {
            toggleBlock();
            profileMenu.setVisibility(View.GONE);
            menuOpen = false;
        });

        // Report button
        btnReport.setOnClickListener(v -> {
            showReportDialog();
            profileMenu.setVisibility(View.GONE);
            menuOpen = false;
        });

        // Close menu when clicking outside
        setOnClickListener(v -> {
            if (menuOpen) {
                profileMenu.setVisibility(View.GONE);
                menuOpen = false;
            }
        });
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        updateAppearance();
        checkIfBlocked();
    }

    public void setProfilePicture(String imageUrl) {
        // Use your preferred image loading library here (Glide, Picasso, etc.)
        // For example with Glide:
        // Glide.with(getContext()).load(imageUrl).into(avatar);

        // For now, using a placeholder
        avatar.setImageResource(R.drawable.ic_person);
    }

    private void toggleMenu() {
        menuOpen = !menuOpen;
        profileMenu.setVisibility(menuOpen ? View.VISIBLE : View.GONE);
    }

    private boolean isOwnProfile() {
        return profile != null && currentUserEmail != null && currentUserEmail.equals(profile.getEmail());
    }

    private void updateAppearance() {
        if (isOwnProfile()) {
            // Disable interactions for own profile
            profileContainer.setClickable(false);
            profileContainer.setFocusable(false);
            avatar.setBackgroundResource(R.drawable.own_profile_border);
        } else {
            profileContainer.setClickable(true);
            profileContainer.setFocusable(true);
            avatar.setBackground(null);
        }
    }

    private void checkIfBlocked() {
        if (isOwnProfile() || currentUserEmail == null || profile == null) return;

        Call<Boolean> call = userService.isUserBlocked(currentUserEmail, profile.getEmail());
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isBlocked = response.body();
                    updateBlockButtonText();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Failed to check block status: " + t.getMessage());
            }
        });
    }

    private void updateBlockButtonText() {
        btnBlock.setText(isBlocked ? "Unblock" : "Block");
    }

    private void toggleBlock() {
        if (currentUserEmail == null || profile == null) {
            Toast.makeText(getContext(), "Invalid user info", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBlocked) {
            // Unblock user
            Call<Void> call = userService.unblockUser(currentUserEmail, profile.getEmail());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isBlocked = false;
                        updateBlockButtonText();
                        Toast.makeText(getContext(), "User unblocked", Toast.LENGTH_SHORT).show();
                        // Send notification if needed
                        // webSocketService.sendNotification(profile.getEmail() + " has been unblocked!");
                    } else {
                        Toast.makeText(getContext(), "Unblock failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Failed to unblock user: " + t.getMessage());
                    Toast.makeText(getContext(), "Unblock failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Block user
            Call<Void> call = userService.blockUser(currentUserEmail, profile.getEmail());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isBlocked = true;
                        updateBlockButtonText();
                        Toast.makeText(getContext(), "User blocked", Toast.LENGTH_SHORT).show();
                        // Send notification if needed
                        // webSocketService.sendNotification(profile.getEmail() + " has been blocked!");
                    } else {
                        Toast.makeText(getContext(), "Block failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Failed to block user: " + t.getMessage());
                    Toast.makeText(getContext(), "Block failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Report User");

        final EditText input = new EditText(getContext());
        input.setHint("Enter reason for reporting...");
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (!reason.isEmpty()) {
                submitReport(reason);
            } else {
                Toast.makeText(getContext(), "Please enter a reason", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void submitReport(String reason) {
        if (currentUserEmail == null || profile == null) {
            Toast.makeText(getContext(), "Invalid user info", Toast.LENGTH_SHORT).show();
            return;
        }

        ReportRequest reportRequest = new ReportRequest(currentUserEmail, profile.getEmail(), reason);
        Call<Void> call = userService.submitReport(reportRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Report submitted for admin review", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Report failed: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper class for report request
    public static class ReportRequest {
        private String reporterEmail;
        private String reportedEmail;
        private String reason;

        public ReportRequest(String reporterEmail, String reportedEmail, String reason) {
            this.reporterEmail = reporterEmail;
            this.reportedEmail = reportedEmail;
            this.reason = reason;
        }

        // Getters and setters
        public String getReporterEmail() { return reporterEmail; }
        public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

        public String getReportedEmail() { return reportedEmail; }
        public void setReportedEmail(String reportedEmail) { this.reportedEmail = reportedEmail; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}