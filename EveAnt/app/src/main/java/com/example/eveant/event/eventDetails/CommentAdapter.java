package com.example.eveant.event.eventDetails;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.R;
import java.util.ArrayList;
import java.util.List;


class Comment {
    public final String author, text, when;
    public Comment(String a, String t, String w) { author=a; text=t; when=w; }
}

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
    private final List<Comment> items = new ArrayList<>();
    public CommentAdapter(List<Comment> seed) { if (seed!=null) items.addAll(seed); }
    static class VH extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvText, tvWhen;
        VH(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvText   = itemView.findViewById(R.id.tvText);
            tvWhen   = itemView.findViewById(R.id.tvWhen);
        }
    }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) { return new VH(p); }
    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Comment c = items.get(i);
        h.tvAuthor.setText(c.author);
        h.tvText.setText(c.text);
        h.tvWhen.setText(c.when);
    }
    @Override public int getItemCount() { return items.size(); }
    public void addFirst(Comment c){ items.add(0,c); notifyItemInserted(0); }
}
