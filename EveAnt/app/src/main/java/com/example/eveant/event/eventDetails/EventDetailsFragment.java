package com.example.eveant.event.eventDetails;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.BaseFragment;
import com.example.eveant.HomeFragment;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.event.eventDetails.utils.Ui;
import com.example.eveant.event.invitations.Invitation;
import com.example.eveant.event.invitations.InviteRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Orchestrator only; delegates logic to controllers/helpers. */
public class EventDetailsFragment extends BaseFragment {
    @Override protected int getMainContainerId() { return R.id.home_container; } // your Activity container id
    @NonNull @Override protected Fragment createHomeFragment() { return new HomeFragment(); }

    private static final String ARG_EVENT_ID = "eventId";

    private int eventId = -1;
    private Event currentEvent;

    // UI refs we need directly
    private Button btnJoin, btnDownloadPdf, btnPost;
    private EditText etComment;
    private LinearLayout commentBox;
    private RecyclerView rvActivities, rvComments;

    // Controllers
    private JoinController joinController;
    private ActivitiesController activitiesController;
    private ReviewsController reviewsController;
    private MapController mapController;
    private PdfExporter pdfExporter;

    public static EventDetailsFragment newInstance(int eventId) {
        Bundle b = new Bundle();
        b.putInt(ARG_EVENT_ID, eventId);
        EventDetailsFragment f = new EventDetailsFragment();
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        if (getArguments()!=null) eventId = getArguments().getInt(ARG_EVENT_ID, -1);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_event_details, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        setupBackBar(v);
        // 0) background
        v.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        // 1) Back navigator (header + system)

        // 2) Pull core refs we need locally
        btnJoin       = v.findViewById(R.id.btnJoin);
        btnDownloadPdf= v.findViewById(R.id.btnDownloadPdf);
//        commentBox    = v.findViewById(R.id.commentBox);
        etComment     = v.findViewById(R.id.etComment);
        btnPost       = v.findViewById(R.id.btnPost);

        rvActivities  = v.findViewById(R.id.rvActivities);
        rvComments    = v.findViewById(R.id.rvComments);

        if (rvActivities != null) {
            rvActivities.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
        if (rvComments != null) {
            rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // 3) Controllers
        joinController       = new JoinController(requireContext(), btnJoin, commentBox);
        activitiesController = new ActivitiesController(rvActivities);
        reviewsController    = new ReviewsController(rvComments);
        mapController        = new MapController(v); // finds mapView inside
        pdfExporter          = new PdfExporter(requireContext(), v);

        // 4) UI events
        if (btnJoin != null) btnJoin.setOnClickListener(x -> joinController.handleJoinClick(currentEvent, () -> toggleJoin()));
        if (btnDownloadPdf != null) btnDownloadPdf.setOnClickListener(x -> {
            if (currentEvent == null) {
                Ui.toast(requireContext(), "Event not loaded yet");
            } else {
                pdfExporter.export(currentEvent, activitiesController.getCurrentActivities());
            }
        });
        if (btnPost != null) {
            btnPost.setOnClickListener(x -> {
                String txt = etComment != null ? String.valueOf(etComment.getText()).trim() : "";
                if (TextUtils.isEmpty(txt)) {
                    if (etComment != null) etComment.setError("Required");
                    return;
                }
                if (etComment != null) { etComment.setError(null); etComment.setText(""); }
                reviewsController.addLocalComment("You", txt, "Just now"); // or send to backend if needed
                Ui.toast(requireContext(), "Comment posted");
            });
        }

        // 5) Data
        if (eventId > 0) {
            fetchEvent();
            fetchActivities();
            reviewsController.fetchReviews(eventId); // load all reviews
        }
    }

    /* ---------------------------- Network orchestration ---------------------------- */

    private void fetchEvent() {
        RetrofitClient.eventService.getEventById(eventId).enqueue(new Callback<Event>() {
            @Override public void onResponse(Call<Event> c, Response<Event> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body()==null) { Ui.toast(requireContext(),"Load event failed: "+r.code()); return; }
                currentEvent = r.body();

                // Bind top header fields + photo + map
                EventUiBinder.bindHeader(requireContext(), getView(), currentEvent, mapController::geocodeAndPlace);

                // Join UI (over/not over; public visibility; and joined state)
                joinController.applyEvent(currentEvent);
                if (currentEvent.getStatus() == com.example.eveant.event.EventStatus.PUBLIC) {
                    checkIfUserHasJoined(); // calls joinController.refreshUi()
                }
            }
            @Override public void onFailure(Call<Event> c, Throwable t) { if (isAdded()) Ui.toast(requireContext(),"Error: "+t.getMessage()); }
        });
    }

    private void fetchActivities() {
        RetrofitClient.activityService.getByEventId(eventId).enqueue(new Callback<List<Activity>>() {
            @Override public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body()==null) { Ui.toast(requireContext(),"Load agenda failed: "+r.code()); return; }
                activitiesController.submit(r.body());
            }
            @Override public void onFailure(Call<List<Activity>> c, Throwable t) { if (isAdded()) Ui.toast(requireContext(),"Error: "+t.getMessage()); }
        });
    }

    private void checkIfUserHasJoined() {
        String email = com.example.eveant.user.security.AuthManager
                .getInstance(requireContext())
                .getEmail();

        RetrofitClient.invitationEventService.getInvitations(eventId).enqueue(new Callback<List<Invitation>>() {
            @Override public void onResponse(Call<List<Invitation>> c, Response<List<Invitation>> r) {
                if (!isAdded()) return;
                boolean joined = false;
                if (r.isSuccessful() && r.body() != null) {
                    for (Invitation inv : r.body()) {
                        if (email.equalsIgnoreCase(inv.email)) { joined = true; break; }
                    }
                }
                joinController.setJoined(joined);
            }
            @Override public void onFailure(Call<List<Invitation>> c, Throwable t) { if (!isAdded()) return; joinController.refreshUi(); }
        });
    }

    /* ---------------------------- Join/Leave actions ---------------------------- */

    private void toggleJoin() {
        if (joinController.isJoined()) leaveEvent(); else joinEvent();
    }

    private void joinEvent() {
        joinController.setLoading(true);
        String email = com.example.eveant.user.security.AuthManager
                .getInstance(requireContext())
                .getEmail();

        RetrofitClient.invitationEventService
                .sendInvitation(eventId, new InviteRequest(email, "", eventId))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (!isAdded()) return;
                        joinController.setLoading(false);
                        if (r.isSuccessful()) {
                            joinController.setJoined(true);
                            Ui.toast(requireContext(), "Joined");
                        } else {
                            Ui.toast(requireContext(), "Join failed: " + r.code());
                        }
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        if (!isAdded()) return;
                        joinController.setLoading(false);
                        Ui.toast(requireContext(), "Network error: " + t.getMessage());
                    }
                });
    }

    private void leaveEvent() {
        joinController.setLoading(true);
        String email = com.example.eveant.user.security.AuthManager
                .getInstance(requireContext())
                .getEmail();

        RetrofitClient.invitationEventService
                .declineInvitation(eventId, java.net.URLEncoder.encode(email))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (!isAdded()) return;
                        joinController.setLoading(false);
                        if (r.isSuccessful()) {
                            joinController.setJoined(false);
                            Ui.toast(requireContext(), "Left event");
                        } else {
                            Ui.toast(requireContext(), "Leave failed: " + r.code());
                        }
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        if (!isAdded()) return;
                        joinController.setLoading(false);
                        Ui.toast(requireContext(), "Network error: " + t.getMessage());
                    }
                });
    }
}
