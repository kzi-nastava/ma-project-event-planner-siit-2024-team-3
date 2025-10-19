package com.example.eveant.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onMarkAsRead(Notification notification);
    }

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    public void removeNotification(int id) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId() == id) {
                notifications.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.messageText.setText(notification.getMessage());
        holder.markAsReadButton.setOnClickListener(v -> listener.onMarkAsRead(notification));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        Button markAsReadButton;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.notificationMessage);
            markAsReadButton = itemView.findViewById(R.id.markAsReadButton);
        }
    }
}
