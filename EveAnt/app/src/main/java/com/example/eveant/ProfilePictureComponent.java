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
import androidx.core.content.ContextCompat;
import com.example.eveant.R;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.security.AuthManager;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePictureComponent extends RelativeLayout {

    private ImageView avatar;
    private Profile profile;
    private String currentUserEmail;
    private boolean isBlocked = false;

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

        // Initialize services
        userService = RetrofitClient.retrofit.create(UserService.class);

        // Get current user email using context
        AuthManager auth = AuthManager.getInstance(context);
        currentUserEmail = auth.getEmail();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Profile container click
        avatar.setOnClickListener(v -> {
            Log.d(TAG, "Avatar clicked");
            if (!isOwnProfile()) {
                showPopupMenu();
            }
        });
    }

    private void showPopupMenu() {
        // Inflate the popup menu layout
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.profile_popup_menu, null);

        Button btnBlock = popupView.findViewById(R.id.btn_block);
        Button btnReport = popupView.findViewById(R.id.btn_report);

        // Update block button text
        btnBlock.setText(isBlocked ? "Unblock" : "Block");

        // Create the popup window
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true // Focusable
        );

        // Set background and elevation
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.profile_menu_background));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20f);
        }

        // Set up button listeners
        btnBlock.setOnClickListener(v -> {
            toggleBlock();
            popupWindow.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            showReportDialog();
            popupWindow.dismiss();
        });

        // Show the popup window relative to avatar
        popupWindow.showAsDropDown(avatar);

        // Dismiss when touching outside
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
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

    private boolean isOwnProfile() {
        return profile != null && currentUserEmail != null && currentUserEmail.equals(profile.getEmail());
    }

    private void updateAppearance() {
        if (isOwnProfile()) {
            // Disable interactions for own profile
            avatar.setClickable(false);
            avatar.setFocusable(false);
            avatar.setBackgroundResource(R.drawable.own_profile_border);
        } else {
            avatar.setClickable(true);
            avatar.setFocusable(true);
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
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Failed to check block status: " + t.getMessage());
            }
        });
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
                Log.d(TAG, "Report response - Code: " + response.code() + ", Message: " + response.message());

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Report submitted for admin review", Toast.LENGTH_SHORT).show();
                } else {
                    // Try to get the error body for more details
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Report error body: " + errorBody);

                            // Check for specific error patterns
                            if (errorBody.contains("reporter") && errorBody.contains("reported")) {
                                Toast.makeText(getContext(), "Cannot report yourself", Toast.LENGTH_SHORT).show();
                            } else if (errorBody.contains("not found") || errorBody.contains("user")) {
                                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Report failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                            Toast.makeText(getContext(), "Report failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Report failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Report network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper class for report request
    public static class ReportRequest {
        @SerializedName("reporterEmail")
        private String reporterEmail;

        @SerializedName("reportedEmail")
        private String reportedEmail;

        @SerializedName("reason")
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