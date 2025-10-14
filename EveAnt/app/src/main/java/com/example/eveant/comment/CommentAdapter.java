// CommentAdapter.java
package com.example.eveant.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.ProfilePictureComponent;
import com.example.eveant.R;
import com.example.eveant.user.model.Profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM d, y, h:mm a", Locale.getDefault());

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ProfilePictureComponent avatar;
        private TextView username;
        private TextView timestamp;
        private TextView content;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.comment_avatar);
            username = itemView.findViewById(R.id.comment_username);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
            content = itemView.findViewById(R.id.comment_content);
        }

        public void bind(Comment comment) {
            Profile profile = comment.getProfile();
            if (profile != null) {
                // Set profile picture
                avatar.setProfile(profile);
                // You might want to set actual profile picture URL here
                // avatar.setProfilePicture(profile.getImageUrl());

                // Set username
                username.setText(profile.getUsername() != null ? profile.getUsername() : "Anonymous");
            }

            // Set content
            content.setText(comment.getContent());

            // Format timestamp
            if (comment.getCreatedAt() != null) {
                try {
                    Date date = apiDateFormat.parse(comment.getCreatedAt());
                    String formattedDate = displayDateFormat.format(date);
                    timestamp.setText(formattedDate);
                } catch (ParseException e) {
                    timestamp.setText(comment.getCreatedAt());
                }
            }
        }
    }
}