package com.example.eveant.invitation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eveant.R;

public class InvitationPreview extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_preview);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new InvitationFragment())
                .commit();
    }
}
