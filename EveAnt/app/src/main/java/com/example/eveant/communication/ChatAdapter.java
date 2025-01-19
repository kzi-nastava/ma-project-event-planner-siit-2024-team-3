package com.example.eveant.communication;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages;
    private User currentUserId;

    public ChatAdapter(List<Message> messages,User currentUserId) {
        this.messages = messages;
        this.currentUserId=currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSender().equals(1)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_send, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_recieved, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder za poslate poruke
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView timestampTextView;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.textViewMessage);
            timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
        }

        void bind(Message message) {
            messageTextView.setText(message.getContent());
            LocalDateTime timestamp = message.getTimestamp();

            DateTimeFormatter formatter = null;
            String formattedDate = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                formattedDate = timestamp.format(formatter);
            }


            timestampTextView.setText(formattedDate);
        }
    }

    // ViewHolder za primljene poruke
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView timestampTextView;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.textViewMessage);
            timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
        }

        void bind(Message message) {
            messageTextView.setText(message.getContent());

            DateTimeFormatter formatter = null;
            String formattedDate = null;
            LocalDateTime timestamp = message.getTimestamp();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                formattedDate = timestamp.format(formatter);
            }


            timestampTextView.setText(formattedDate);
        }
    }
}
