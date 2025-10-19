// CommentsSectionFragment.java
package com.example.eveant.comment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.user.security.AuthManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CommentsSectionFragment extends Fragment {

    private Integer eventId;
    private CommentService commentService;
    private AuthManager authManager;

    private EditText commentInput;
    private Button postCommentBtn;
    private TextView commentConfirmation;
    private TextView emptyComments;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;

    private List<Comment> comments = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    public static CommentsSectionFragment newInstance(Integer eventId) {
        CommentsSectionFragment fragment = new CommentsSectionFragment();
        Bundle args = new Bundle();
        args.putInt("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getInt("eventId");
        }

        commentService = RetrofitClient.retrofit.create(CommentService.class);
        authManager = AuthManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comments_section, container, false);
        initViews(view);
        setupRecyclerView();
        loadApprovedComments();
        return view;
    }

    private void initViews(View view) {
        commentInput = view.findViewById(R.id.comment_input);
        postCommentBtn = view.findViewById(R.id.post_comment_btn);
        commentConfirmation = view.findViewById(R.id.comment_confirmation);
        emptyComments = view.findViewById(R.id.empty_comments);
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);

        postCommentBtn.setOnClickListener(v -> addComment());
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void loadApprovedComments() {
        if (eventId == null) {
            System.out.println("DEBUG: Event ID is null");
            return;
        }

        System.out.println("DEBUG: Loading comments for event: " + eventId);

        Call<List<Comment>> call = commentService.getApprovedComments(eventId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                System.out.println("DEBUG: Response received - Code: " + response.code() + ", Successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("DEBUG: Loaded " + response.body().size() + " comments");
                    comments.clear();
                    comments.addAll(response.body());
                    commentAdapter.setComments(comments);
                    updateEmptyState();
                } else {
                    System.out.println("DEBUG: Response not successful - Code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load comments: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                System.out.println("DEBUG: Network failure: " + t.getMessage());
                t.printStackTrace(); // This will show the full stack trace
                Toast.makeText(getContext(), "Error loading comments: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment() {
        String commentText = commentInput.getText().toString().trim();

        if (!authManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please log in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (commentText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = authManager.getEmail();
        CommentService.CommentRequest commentRequest = new CommentService.CommentRequest(
                commentText, eventId, userEmail
        );

        Call<Comment> call = commentService.addComment(commentRequest);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.isSuccessful()) {
                    commentInput.setText("");
                    showConfirmationMessage("Your comment has been sent to the admin for approval.");

                    // Optionally refresh comments to show the new one if approved immediately
                    // loadApprovedComments();
                } else {
                    Toast.makeText(getContext(), "Failed to add comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Toast.makeText(getContext(), "Error adding comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationMessage(String message) {
        commentConfirmation.setText(message);
        commentConfirmation.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            commentConfirmation.setVisibility(View.GONE);
        }, 5000);
    }

    private void updateEmptyState() {
        if (comments.isEmpty()) {
            emptyComments.setVisibility(View.VISIBLE);
            commentsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyComments.setVisibility(View.GONE);
            commentsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}