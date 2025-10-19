package com.example.eveant.invitation;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    private List<Invitation> invitations = new ArrayList<>();
    private InvitationService invitationService;

    public InvitationAdapter(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvitationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationAdapter.ViewHolder holder, int position) {
        Invitation invitation = invitations.get(position);
        holder.email.setText(invitation.getEmail());

        updateButtonColors(holder, invitation);

        holder.acceptBtn.setOnClickListener(v -> {
            // Optimistically update UI
            invitation.setAccepted(true);
            invitation.setDeclined(false);
            updateButtonColors(holder, invitation);

            // Call API
            invitationService.updateInvitationStatus(invitation.getId(), new InvitationStatusRequest(true))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                // revert if API fails
                                invitation.setAccepted(false);
                                updateButtonColors(holder, invitation);
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            invitation.setAccepted(false);
                            updateButtonColors(holder, invitation);
                        }
                    });
        });

        holder.declineBtn.setOnClickListener(v -> {
            invitation.setAccepted(false);
            invitation.setDeclined(true);
            updateButtonColors(holder, invitation);

            invitationService.updateInvitationStatus(invitation.getId(), new InvitationStatusRequest(false))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!response.isSuccessful()) {
                                invitation.setDeclined(false);
                                updateButtonColors(holder, invitation);
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            invitation.setDeclined(false);
                            updateButtonColors(holder, invitation);
                        }
                    });
        });
    }

    private void updateButtonColors(ViewHolder holder, Invitation invitation) {
        if (invitation.isAccepted()) {
            holder.acceptBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#27AE60"))); // green
            holder.declineBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#d9e1ec"))); // grey
            holder.acceptBtn.setEnabled(false);
            holder.declineBtn.setEnabled(false);
        } else if (invitation.isDeclined()) {
            holder.acceptBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#d9e1ec"))); // grey
            holder.declineBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E74C3C"))); // red
            holder.acceptBtn.setEnabled(false);
            holder.declineBtn.setEnabled(false);
        } else {
            holder.acceptBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#27AE60"))); // green
            holder.declineBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E74C3C"))); // red
            holder.acceptBtn.setEnabled(true);
            holder.declineBtn.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView email;
        ImageButton acceptBtn, declineBtn;

        ViewHolder(View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.guest_email);
            acceptBtn = itemView.findViewById(R.id.button_accept);
            declineBtn = itemView.findViewById(R.id.button_decline);
        }
    }
}
