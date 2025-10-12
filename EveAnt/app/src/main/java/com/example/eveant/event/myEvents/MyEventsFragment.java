package com.example.eveant.event.myEvents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.EventStatus;
import com.example.eveant.eventType.EventType;
import com.example.eveant.eventType.EventTypeService;
import com.example.eveant.user.security.AuthManager;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyEventsFragment extends Fragment {

    private TextView tvDateFilter;
    private Spinner spEventType;
    private Button btnApply, btnClear;
    private RecyclerView rvEvents;
    private ProgressBar progress;
    private TextView tvEmpty;

    private MyEventsAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<EventType> allTypes = new ArrayList<>();

    private Integer selectedTypeId = null;
    private String selectedDateIso = null; // yyyy-MM-dd

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tvDateFilter = v.findViewById(R.id.tvDateFilter);
        spEventType   = v.findViewById(R.id.spEventType);
        btnApply      = v.findViewById(R.id.btnApply);
        btnClear      = v.findViewById(R.id.btnClear);
        rvEvents      = v.findViewById(R.id.rvEvents);
        progress      = v.findViewById(R.id.progress);
        tvEmpty       = v.findViewById(R.id.tvEmpty);

        // Grid like web: 2 cards per row (phones portrait)
        rvEvents.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new MyEventsAdapter(new MyEventsAdapter.Listener() {

            @Override public void onEdit(Event e)  { showUpdateDialog(e); }
            @Override public void onDelete(Event e){ confirmDeleteDialog(e); }
            @Override public void onOpen(Event e) {
                Fragment details = com.example.eveant.event.eventDetails.EventDetailsFragment.newInstance(e.getId());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.nav_host_fragment, details)
                        .addToBackStack("event_details")
                        .commit();
            }
        });
        rvEvents.setAdapter(adapter);

        tvDateFilter.setOnClickListener(v1 -> openDatePicker());
        btnApply.setOnClickListener(v12 -> applyFilters());
        btnClear.setOnClickListener(v13 -> clearFilters());

        fetchEventTypes();  // fills spinner
        loadEvents();       // loads user's events
    }

    private void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    // show as mm/dd/yyyy, keep ISO internally
                    tvDateFilter.setText(String.format(Locale.US, "%02d/%02d/%04d", m + 1, d, y));
                    selectedDateIso = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void applyFilters() {
        filterAndShow();
    }

    private void clearFilters() {
        selectedDateIso = null;
        tvDateFilter.setText("mm/dd/yyyy");
        spEventType.setSelection(0);
        selectedTypeId = null;
        filterAndShow();
    }

    private void filterAndShow() {
        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            boolean ok = true;

            if (selectedTypeId != null) {
                Integer tId = (e.getEventType() != null) ? e.getEventType().getId() : null;
                ok &= (tId != null && tId.equals(selectedTypeId));
            }
            if (selectedDateIso != null) {
                // compare by yyyy-MM-dd prefix
                String iso = safe(e.getDate()); // assumes ISO "yyyy-MM-ddTHH:mm:ss" or "yyyy-MM-dd"
                ok &= iso.startsWith(selectedDateIso);
            }
            if (ok) filtered.add(e);
        }
        adapter.submit(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void fetchEventTypes() {
        // Populate spinner: "All event types" + types from backend
        final List<String> names = new ArrayList<>();
        names.add("All event types");

        EventTypeService svc = RetrofitClient.eventTypeService;
        svc.getAllActivated().enqueue(new Callback<List<EventType>>() {
            @Override public void onResponse(Call<List<EventType>> call, Response<List<EventType>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    bindSpinner(names);
                    return;
                }
                allTypes.clear();
                allTypes.addAll(resp.body());
                for (EventType t : allTypes) names.add(nonNull(t.getName()));
                bindSpinner(names);
            }
            @Override public void onFailure(Call<List<EventType>> call, Throwable t) {
                bindSpinner(names);
            }
        });
    }

    private void bindSpinner(List<String> names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, names);
        spEventType.setAdapter(adapter);
        spEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedTypeId = null;
                } else {
                    EventType t = allTypes.get(position - 1);
                    selectedTypeId = t.getId();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadEvents() {
        showLoading(true);
        AuthManager auth = AuthManager.getInstance(requireContext());
        final String organizerEmail = auth.getEmail();
        RetrofitClient.eventService.getAllByOrganizer(organizerEmail).enqueue(new Callback<List<Event>>() {
            @Override public void onResponse(Call<List<Event>> call, Response<List<Event>> resp) {
                showLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    allEvents.clear();
                    adapter.submit(allEvents);
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                allEvents.clear();
                allEvents.addAll(resp.body());
                filterAndShow();
            }
            @Override public void onFailure(Call<List<Event>> call, Throwable t) {
                showLoading(false);
                allEvents.clear();
                adapter.submit(allEvents);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String nonNull(String s) { return s == null ? "" : s; }

    private void confirmDeleteDialog(Event e) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete event?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> doDelete(e))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doDelete(Event e) {
        showLoading(true);
        RetrofitClient.eventService.deleteEvent(e.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> resp) {
                showLoading(false);
                if (!resp.isSuccessful()) {
                    Toast.makeText(requireContext(), "Delete failed: " + resp.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                // remove locally
                int idx = -1;
                for (int i = 0; i < allEvents.size(); i++) if (allEvents.get(i).getId() == e.getId()) { idx = i; break; }
                if (idx >= 0) {
                    allEvents.remove(idx);
                    adapter.submit(new ArrayList<>(allEvents));
                }
                Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(Event e) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_event_update, null, false);

        EditText etName   = dialogView.findViewById(R.id.etName);
        EditText etDesc   = dialogView.findViewById(R.id.etDescription);
        EditText etMax    = dialogView.findViewById(R.id.etMax);
        TextView tvDate   = dialogView.findViewById(R.id.tvDate);
        TextView tvTime   = dialogView.findViewById(R.id.tvTime);
        RadioButton rbPriv= dialogView.findViewById(R.id.rbPrivate);
        RadioButton rbPubl= dialogView.findViewById(R.id.rbPublic);

        // prefill
        etName.setText(e.getName());
        etDesc.setText(e.getDescription());
        if (e.getMaxAttendance() != null) etMax.setText(String.valueOf(e.getMaxAttendance()));
        String[] dt = splitDateTime(safe(e.getDate())); // ["yyyy-MM-dd","HH:mm"]
        tvDate.setText(dt[0].isEmpty() ? "yyyy-MM-dd" : dt[0]);
        tvTime.setText(dt[1].isEmpty() ? "--:--" : dt[1]);
        if ("PUBLIC".equalsIgnoreCase(e.getStatus().toString())) rbPubl.setChecked(true); else rbPriv.setChecked(true);

        // pickers
        tvDate.setOnClickListener(v -> openDatePickerInto(tvDate));
        tvTime.setOnClickListener(v -> openTimePickerInto(tvTime));

        androidx.appcompat.app.AlertDialog dlg = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Update event")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dlg.setOnShowListener(d -> {
            if (dlg.getWindow() != null) {
                dlg.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE)
                );
            }

            int purple = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.light_purple);
            dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(purple);
            dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(purple);

            int titleId = requireContext().getResources()
                    .getIdentifier("alertTitle", "id", "android");
            TextView titleView = dlg.findViewById(titleId);
            if (titleView != null) titleView.setTextColor(purple);

            Button save = dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            save.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { etName.setError("Required"); return; }

                String dateIso;
                try { dateIso = toIso(tvDate.getText().toString(), tvTime.getText().toString()); }
                catch (Exception ex) { Toast.makeText(requireContext(), "Invalid date/time", Toast.LENGTH_SHORT).show(); return; }

                Event body = new Event();
                body.name = name;
                body.description = etDesc.getText().toString().trim();
                body.status = rbPubl.isChecked() ? EventStatus.PUBLIC : EventStatus.PRIVATE;
                body.date = dateIso;
                try { body.maxAttendance = Integer.parseInt(etMax.getText().toString().trim()); } catch (Exception ignored) {}
                if (e.getEventType() != null && e.getEventType().getId() != null) body.eventType = e.getEventType();

                // call API
                showLoading(true);
                RetrofitClient.eventService.updateEvent(e.getId(), body).enqueue(new retrofit2.Callback<Event>() {
                    @Override public void onResponse(retrofit2.Call<Event> call, retrofit2.Response<Event> resp) {
                        showLoading(false);
                        if (!resp.isSuccessful() || resp.body() == null) {
                            Toast.makeText(requireContext(), "Update failed: " + resp.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // update list item locally
                        Event updated = resp.body();
                        int idx = -1;
                        for (int i = 0; i < allEvents.size(); i++) if (allEvents.get(i).getId() == updated.getId()) { idx = i; break; }
                        if (idx >= 0) {
                            allEvents.set(idx, updated);
                            adapter.submit(new ArrayList<>(allEvents));
                        }
                        Toast.makeText(requireContext(), "Event updated", Toast.LENGTH_SHORT).show();
                        dlg.dismiss();
                    }
                    @Override public void onFailure(retrofit2.Call<Event> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dlg.show();
    }

    // helpers
    private void openDatePickerInto(TextView target) {
        final Calendar c = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(),
                (view, y, m, d) -> target.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m+1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void openTimePickerInto(TextView target) {
        final Calendar c = Calendar.getInstance();
        new android.app.TimePickerDialog(requireContext(),
                (view, h, min) -> target.setText(String.format(Locale.US, "%02d:%02d", h, min)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
        ).show();
    }

    private static String[] splitDateTime(String iso) {
        if (iso == null) return new String[]{"",""};
        int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
        if (t > 0) {
            String date = iso.substring(0, t);
            String time = iso.length() >= t+5 ? iso.substring(t+1, t+6) : "";
            return new String[]{date, time};
        }
        return new String[]{iso, ""};
    }

    private static String toIso(String ymd, String hm) throws Exception {
        // Input expected "yyyy-MM-dd" + "HH:mm"
        java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
        java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
        Date d = in.parse(ymd + " " + hm);
        return out.format(d);
    }

}
