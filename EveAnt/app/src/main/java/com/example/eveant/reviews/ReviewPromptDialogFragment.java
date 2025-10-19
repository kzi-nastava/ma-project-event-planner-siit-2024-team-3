package com.example.eveant.reviews;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewPromptDialogFragment extends DialogFragment {

    public interface Listener {
        void onReviewSubmitted(int eventId, int rating);
        void onReviewDismissed();
    }

    private Listener listener;
    private String username;
    private Event targetEvent;

    public static ReviewPromptDialogFragment newInstance(String username) {
        ReviewPromptDialogFragment f = new ReviewPromptDialogFragment();
        Bundle b = new Bundle();
        b.putString("username", username);
        f.setArguments(b);
        return f;
    }

    public void setListener(Listener l) { this.listener = l; }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        username = getArguments() != null ? getArguments().getString("username") : null;

        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_review_prompt_dialog, null);

        ImageView ivCover = content.findViewById(R.id.ivCover);
        TextView tvTitle  = content.findViewById(R.id.tvTitle);
        TextView tvWhen   = content.findViewById(R.id.tvWhen);
        RatingBar rating  = content.findViewById(R.id.ratingBar);
        EditText etNote   = content.findViewById(R.id.etComment);
        ProgressBar prog  = content.findViewById(R.id.progress);
        View btnSubmit    = content.findViewById(R.id.btnSubmit);
        View btnLater     = content.findViewById(R.id.btnLater);
        View body         = content.findViewById(R.id.body);

        // initial states
        body.setVisibility(View.INVISIBLE);
        prog.setVisibility(View.VISIBLE);

        // fetch pending events
        RetrofitClient.reviewService.getEventsToReview(username).enqueue(new Callback<List<Event>>() {
            @Override public void onResponse(Call<List<Event>> call, Response<List<Event>> resp) {
                prog.setVisibility(View.GONE);
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    targetEvent = resp.body().get(0); // show the first pending event now
                    body.setVisibility(View.VISIBLE);

                    // Title / time
                    String name = targetEvent != null ? targetEvent.name : null;
                    String date = targetEvent != null ? targetEvent.date : null;
                    tvTitle.setText(!TextUtils.isEmpty(name) ? name : ("Event #" + (targetEvent != null ? targetEvent.id : 0)));
                    tvWhen.setText(!TextUtils.isEmpty(date) ? date : "");

                    // ----- COVER (same behavior as Public/My Events) -----
                    String cover = null;
                    try {
                        if (targetEvent != null && targetEvent.photos != null && !targetEvent.photos.isEmpty()) {
                            cover = targetEvent.photos.get(0);
                        }
                    } catch (Exception ignored) {}

                    if (!TextUtils.isEmpty(cover)) {
                        if (cover.startsWith("http://") || cover.startsWith("https://")) {
                            // Absolute URL
                            Glide.with(ivCover)
                                    .load(cover)
                                    .placeholder(R.drawable.rounded_corners_image_blue)
                                    .error(R.drawable.rounded_corners_image_blue)
                                    .into(ivCover);

                        } else if (isLikelyBase64(cover)) {
                            // Base64 (with or without data URI prefix)
                            try {
                                String b64 = stripDataUriPrefix(cover);
                                byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (bmp != null) {
                                    ivCover.setImageBitmap(bmp);
                                } else {
                                    ivCover.setImageResource(R.drawable.rounded_corners_image_blue);
                                }
                            } catch (Exception e) {
                                ivCover.setImageResource(R.drawable.rounded_corners_image_blue);
                            }

                        } else {
                            // Relative/unknown → placeholder (no BASE_URL)
                            ivCover.setImageResource(R.drawable.rounded_corners_image_blue);
                        }
                    } else {
                        // No photo → placeholder
                        ivCover.setImageResource(R.drawable.rounded_corners_image_blue);
                    }

                } else {
                    dismissAllowingStateLoss(); // nothing to review
                }
            }
            @Override public void onFailure(Call<List<Event>> call, Throwable t) {
                dismissAllowingStateLoss();
            }
        });

        // submit
        btnSubmit.setOnClickListener(v -> {
            if (targetEvent == null) return;
            int stars = Math.max(1, Math.round(rating.getRating())); // enforce 1..5
            String comment = etNote.getText() != null ? etNote.getText().toString().trim() : null;

            CreateReview bodyDto = new CreateReview(
                    username,
                    targetEvent.id, // eventId
                    null,           // offerId
                    stars,
                    comment
            );

            v.setEnabled(false);
            RetrofitClient.reviewService.createReview(bodyDto).enqueue(new Callback<CreateReview.Created>() {
                @Override public void onResponse(Call<CreateReview.Created> call, Response<CreateReview.Created> resp) {
                    if (resp.isSuccessful()) {
                        Toast.makeText(requireContext(), "Thanks for your review!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onReviewSubmitted(targetEvent.id, stars);
                        dismissAllowingStateLoss();
                    } else if (resp.code() == 409) {
                        Toast.makeText(requireContext(), "You already reviewed this event.", Toast.LENGTH_SHORT).show();
                        dismissAllowingStateLoss();
                    } else {
                        Toast.makeText(requireContext(), "Could not submit review ("+resp.code()+")", Toast.LENGTH_SHORT).show();
                        v.setEnabled(true);
                    }
                }
                @Override public void onFailure(Call<CreateReview.Created> call, Throwable t) {
                    Toast.makeText(requireContext(), "Network error while submitting", Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                }
            });
        });

        // later
        btnLater.setOnClickListener(v -> {
            if (listener != null) listener.onReviewDismissed();
            dismissAllowingStateLoss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(content)
                .setCancelable(true)
                .create();
    }

    // ==== helpers (same logic as adapters) ====
    private static boolean isLikelyBase64(String s) {
        if (TextUtils.isEmpty(s)) return false;
        if (s.startsWith("data:image")) return true;
        if (s.length() < 100) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ok = (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9') ||
                    c == '+' || c == '/' || c == '=' || c == '\n' || c == '\r';
            if (!ok) return false;
        }
        return true;
    }

    private static String stripDataUriPrefix(String s) {
        int idx = s != null ? s.indexOf("base64,") : -1;
        return (idx >= 0) ? s.substring(idx + "base64,".length()) : (s != null ? s : "");
    }
}
