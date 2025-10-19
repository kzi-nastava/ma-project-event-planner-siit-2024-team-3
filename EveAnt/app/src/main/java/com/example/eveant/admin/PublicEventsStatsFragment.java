// PublicEventsStatsFragment.java
package com.example.eveant.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.BaseFragment;
import com.example.eveant.HomeFragment;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.admin.adminReports.AdminReportsFragment;
import com.example.eveant.admin.commentApproval.AdminCommentApprovalFragment;
import com.example.eveant.event.Event;
import java.util.*;
import java.util.stream.Collectors;
import retrofit2.*;

public class PublicEventsStatsFragment extends BaseFragment {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvTotalPublic, tvByCity;
    private PublicEventAdapter adapter;
    private Button btnAdminReports;
    private Button btnAllAttendance, btnAllReviews, btnAdminComments;

    @Override protected int getMainContainerId() { return R.id.home_container; }
    @NonNull @Override protected Fragment createHomeFragment() { return new HomeFragment(); }

    private List<Event> lastLoaded = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_public_events_stats, container, false);
        rv = v.findViewById(R.id.rvEvents);
        progress = v.findViewById(R.id.progress);
        tvTotalPublic = v.findViewById(R.id.tvTotalPublic);
        tvByCity = v.findViewById(R.id.tvByCity);
        btnAllAttendance = v.findViewById(R.id.btnAllAttendance);
        btnAllReviews = v.findViewById(R.id.btnAllReviews);
        btnAdminComments = v.findViewById(R.id.btnAdminComments);
        btnAdminReports = v.findViewById(R.id.btnAdminReports);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PublicEventAdapter();
        rv.setAdapter(adapter);

        btnAllAttendance.setOnClickListener(view -> {
            int[] ids = lastLoaded.stream().mapToInt(Event::getId).toArray();
            String[] names = lastLoaded.stream().map(e -> e.getName() == null ? ("#" + e.getId()) : e.getName()).toArray(String[]::new);
            startActivity(GraphsActivity.intentForAll(requireContext(), ids, names, GraphsActivity.TYPE_ATTENDANCE));
        });

        btnAllReviews.setOnClickListener(view -> {
            int[] ids = lastLoaded.stream().mapToInt(Event::getId).toArray();
            String[] names = lastLoaded.stream().map(e -> e.getName() == null ? ("#" + e.getId()) : e.getName()).toArray(String[]::new);
            startActivity(GraphsActivity.intentForAll(requireContext(), ids, names, GraphsActivity.TYPE_REVIEWS));
        });

        // Add click listener for admin comments button
        btnAdminComments.setOnClickListener(view -> {
            AdminCommentApprovalFragment adminCommentFragment = AdminCommentApprovalFragment.newInstance();

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(getId(), adminCommentFragment) // Use the current fragment's container
                    .addToBackStack("admin_comments")
                    .commit();
        });





        btnAdminReports.setOnClickListener(view -> {
            AdminReportsFragment adminReportsFragment = AdminReportsFragment.newInstance();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(getId(), adminReportsFragment)
                    .addToBackStack("admin_reports")
                    .commit();
        });
        loadData();
        return v;
    }

    private void loadData() {
        showLoading(true);
        Call<List<Event>> call = RetrofitClient.eventService.searchEvents(
                null, null, "PUBLIC", null, null, null, null, "name", "asc"
        );
        call.enqueue(new Callback<List<Event>>() {
            @Override public void onResponse(Call<List<Event>> call, Response<List<Event>> resp) {
                showLoading(false);
                List<Event> items = (resp.isSuccessful() && resp.body() != null) ? resp.body() : Collections.emptyList();
                lastLoaded = items;
                adapter.submit(items);
                updateStats(items);
            }
            @Override public void onFailure(Call<List<Event>> call, Throwable t) {
                showLoading(false);
                lastLoaded = Collections.emptyList();
                adapter.submit(Collections.emptyList());
                tvTotalPublic.setText("Public events: 0");
                tvByCity.setText("");
            }
        });
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        setupBackBar(v);
    }

    private void updateStats(List<Event> items) {
        tvTotalPublic.setText("Public events: " + (items == null ? 0 : items.size()));
        Map<String, Long> byCity = new LinkedHashMap<>();
        if (items != null) {
            for (Event e : items) {
                String city = null;
                try {
                    Object addr = e.getClass().getMethod("getAddress").invoke(e);
                    if (addr != null) {
                        try { city = (String) addr.getClass().getMethod("getCity").invoke(addr); }
                        catch (Exception ignored) { city = addr.toString(); }
                    }
                } catch (Exception ignored) {}
                if (TextUtils.isEmpty(city)) city = "—";
                byCity.put(city, byCity.getOrDefault(city, 0L) + 1);
            }
        }
        String cityText = byCity.entrySet().stream()
                .sorted((a,b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("   "));
        tvByCity.setText(cityText);
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        rv.setAlpha(show ? 0.3f : 1f);
    }
}