package com.example.eveant.event.eventDetails;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.RetrofitClient;
import com.example.eveant.comment.Comment;
import com.example.eveant.reviews.Review;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ReviewsController {

    private final RecyclerView rv;
    private final ReviewAdapter adapter = new ReviewAdapter();

    ReviewsController(RecyclerView rv) {
        this.rv = rv;
        if (this.rv != null) this.rv.setAdapter(adapter);
    }

    void fetchReviews(int eventId) {
        if (rv == null) return;
        RetrofitClient.reviewService.getEventReviews(eventId).enqueue(new Callback<List<Review>>() {
            @Override public void onResponse(Call<List<Review>> c, Response<List<Review>> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    // silent; or show a toast if you want
                    return;
                }
                adapter.replaceAll(r.body());
            }
            @Override public void onFailure(Call<List<Review>> c, Throwable t) { /* optionally toast */ }
        });
    }
    void setApiComments(List<Comment> comments) {
        if (rv == null || comments == null) return;

        // Convert Comment objects to Review objects
        List<Review> reviews = new ArrayList<>();
        for (Comment comment : comments) {
            // Extract author name from profile
            String authorName = "Anonymous";
            if (comment.getProfile() != null && comment.getProfile().getUsername() != null) {
                authorName = comment.getProfile().getUsername();
            }

            // Use createdAt as the date
            String date = comment.getCreatedAt();

            Review review = new Review(
                    authorName,
                    comment.getContent(),
                    date,
                    0  // default rating since comments don't have ratings
            );
            reviews.add(review);
        }
        adapter.replaceAll(reviews);
    }
    /** For local echo after posting comment (if you keep comments inline). */
    void addLocalComment(String author, String text, String when) {
        Review local = new Review(author, text, when, 0); // adapt to your constructor
        List<Review> data = new ArrayList<>(adapter.getItems());
        data.add(0, local);
        adapter.replaceAll(data);
    }
}
