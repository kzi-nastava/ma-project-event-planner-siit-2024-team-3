package com.example.eveant.admin.commentApproval;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.comment.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminCommentAdapter extends RecyclerView.Adapter<AdminCommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private final CommentActionListener approveListener;
    private final CommentActionListener deleteListener;

    public interface CommentActionListener {
        void onAction(int commentId);
    }

    public AdminCommentAdapter(List<Comment> comments, CommentActionListener approveListener, CommentActionListener deleteListener) {
        this.comments = comments;
        this.approveListener = approveListener;
        this.deleteListener = deleteListener;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameText;
        private TextView dateText;
        private TextView eventNameText;
        private TextView commentContentText;
        private Button approveButton;
        private Button deleteButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            dateText = itemView.findViewById(R.id.dateText);
            eventNameText = itemView.findViewById(R.id.eventNameText);
            commentContentText = itemView.findViewById(R.id.commentContentText);
            approveButton = itemView.findViewById(R.id.approveButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Comment comment) {
            // Set username
            String username = "Unknown User";
            if (comment.getProfile() != null && comment.getProfile().getUsername() != null) {
                username = comment.getProfile().getUsername();
            }
            usernameText.setText(username);

            // Set date
            String formattedDate = formatDate(comment.getCreatedAt());
            dateText.setText(formattedDate);

            // Set event name
            String eventName = "Unknown Event";
            if (comment.getEvent() != null) {
                eventName = comment.getEvent().getName();
            }
            eventNameText.setText("Event: " + eventName);

            // Set comment content
            commentContentText.setText(comment.getContent());

            // Set button listeners
            approveButton.setOnClickListener(v -> approveListener.onAction(comment.getId()));
            deleteButton.setOnClickListener(v -> deleteListener.onAction(comment.getId()));
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}