package com.example.eveant.event.eventDetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.R;
import com.example.eveant.comment.Comment;
import com.example.eveant.user.model.Profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
    private final List<Comment> items = new ArrayList<>();
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM d, y, h:mm a", Locale.getDefault());

    public CommentAdapter(List<Comment> seed) {
        if (seed != null) items.addAll(seed);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvText, tvWhen;

        VH(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvText   = itemView.findViewById(R.id.tvText);
            tvWhen   = itemView.findViewById(R.id.tvWhen);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Comment comment = items.get(position);

        // Set author name
        Profile profile = comment.getProfile();
        if (profile != null) {
            String username = profile.getUsername();
            holder.tvAuthor.setText(username != null ? username : "Anonymous");
        } else {
            holder.tvAuthor.setText("Anonymous");
        }

        // Set comment text
        holder.tvText.setText(comment.getContent());

        // Format and set timestamp
        if (comment.getCreatedAt() != null) {
            try {
                Date date = apiDateFormat.parse(comment.getCreatedAt());
                String formattedDate = displayDateFormat.format(date);
                holder.tvWhen.setText(formattedDate);
            } catch (ParseException e) {
                // Fallback to raw date string if parsing fails
                holder.tvWhen.setText(comment.getCreatedAt());
            }
        } else {
            holder.tvWhen.setText("Unknown date");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setComments(List<Comment> comments) {
        items.clear();
        if (comments != null) {
            items.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void addFirst(Comment comment) {
        items.add(0, comment);
        notifyItemInserted(0);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }
}