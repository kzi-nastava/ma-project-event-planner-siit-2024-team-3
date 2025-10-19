package com.example.eveant.admin.commentApproval;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.comment.Comment;
import com.example.eveant.comment.CommentService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCommentApprovalFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private AdminCommentAdapter adapter;
    private List<Comment> pendingComments = new ArrayList<>();

    private CommentService commentService;

    public static AdminCommentApprovalFragment newInstance() {
        return new AdminCommentApprovalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_comment_approval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentService = RetrofitClient.retrofit.create(CommentService.class);

        recyclerView = view.findViewById(R.id.commentsRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminCommentAdapter(pendingComments, this::approveComment, this::deleteComment);
        recyclerView.setAdapter(adapter);

        loadPendingComments();
    }

    private void loadPendingComments() {
        commentService.getPendingComments().enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingComments.clear();
                    pendingComments.addAll(response.body());
                    adapter.setComments(pendingComments);
                    updateEmptyState();
                } else {
                    Toast.makeText(getContext(), "Failed to load comments: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading comments: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void approveComment(int commentId) {
        commentService.approveComment(commentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    removeCommentFromList(commentId);
                    Toast.makeText(getContext(), "Comment approved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to approve comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error approving comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteComment(int commentId) {
        commentService.deleteComment(commentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    removeCommentFromList(commentId);
                    Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error deleting comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCommentFromList(int commentId) {
        for (int i = 0; i < pendingComments.size(); i++) {
            if (pendingComments.get(i).getId() == commentId) {
                pendingComments.remove(i);
                adapter.setComments(pendingComments);
                updateEmptyState();
                break;
            }
        }
    }

    private void updateEmptyState() {
        if (pendingComments.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}