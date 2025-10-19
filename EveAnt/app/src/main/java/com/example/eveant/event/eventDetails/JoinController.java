package com.example.eveant.event.eventDetails;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.eveant.event.Event;
import com.example.eveant.event.eventDetails.utils.Ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class JoinController {
    private final Context ctx;
    private final Button btnJoin;
    private final LinearLayout commentBox;

    private boolean joined = false;
    private @Nullable Event event;
    private boolean loading = false;

    JoinController(Context ctx, Button btnJoin, LinearLayout commentBox) {
        this.ctx = ctx; this.btnJoin = btnJoin; this.commentBox = commentBox;
    }

    void applyEvent(@Nullable Event e) {
        this.event = e;
        refreshUi();
    }

    void setJoined(boolean v) {
        joined = v;
        refreshUi();
    }

    boolean isJoined() { return joined; }

    void setLoading(boolean v) {
        loading = v;
        if (btnJoin != null) btnJoin.setEnabled(!v);
    }

    void refreshUi() {
        if (btnJoin == null) return;

        boolean over = isEventOver(event != null ? event.getDate() : null);
        if (over) {
            btnJoin.setEnabled(false);
            btnJoin.setText("This event is over");
            btnJoin.setAlpha(0.6f);
            if (commentBox != null) commentBox.setVisibility(View.GONE);
            return;
        }
        btnJoin.setEnabled(!loading);
        btnJoin.setAlpha(1f);
        btnJoin.setText(joined ? "Leave event" : "Join event");
        if (commentBox != null) commentBox.setVisibility(View.VISIBLE);
    }

    void handleJoinClick(@Nullable Event e, Runnable toggleAction) {
        if (isEventOver(e != null ? e.getDate() : null)) {
            Ui.toast(ctx, "This event is over.");
            return;
        }
        toggleAction.run();
    }

    private boolean isEventOver(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) return false;
        try {
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            String date = (t > 0) ? iso.substring(0, t) : iso;
            String time = (t > 0 && iso.length() >= t + 5) ? iso.substring(t + 1, t + 6) : "23:59";
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date eventDt = in.parse(date + " " + time);
            return eventDt != null && eventDt.before(new Date());
        } catch (Exception ignored) { return false; }
    }
}
