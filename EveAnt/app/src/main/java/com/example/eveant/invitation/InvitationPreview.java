package com.example.eveant.invitation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eveant.R;
import com.example.eveant.comment.CommentsSectionFragment;

public class InvitationPreview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use a test event ID for preview
        int testEventId = 1;

        CommentsSectionFragment commentsFragment = CommentsSectionFragment.newInstance(testEventId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, commentsFragment)
                .commit();
    }
}