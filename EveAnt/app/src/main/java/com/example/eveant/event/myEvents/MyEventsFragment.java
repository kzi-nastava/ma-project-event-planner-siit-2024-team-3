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

import com.example.eveant.BaseFragment;
import com.example.eveant.HomeFragment;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.EventStatus;
import com.example.eveant.event.EventUpdateDTO;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.eventType.EventType;
import com.example.eveant.eventType.EventTypeService;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.security.AuthManager;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyEventsFragment extends BaseFragment {

    private TextView tvDateFilter;
    private Spinner spEventType;
    private Button btnApply, btnClear;
    private RecyclerView rvEvents;
    private ProgressBar progress;
    private TextView tvEmpty;

    private MyEventsAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<EventType> allTypes = new ArrayList<>();
    @Override protected int getMainContainerId() { return R.id.home_container; } // your Activity container id
    @NonNull @Override protected Fragment createHomeFragment() { return new HomeFragment(); }

    private Integer selectedTypeId = null;
    private String selectedDateIso = null; // yyyy-MM-dd


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        setupBackBar(v);   // <-- one line, done

        tvDateFilter = v.findViewById(R.id.tvDateFilter);
        spEventType   = v.findViewById(R.id.spEventType);
        btnApply      = v.findViewById(R.id.btnApply);
        btnClear      = v.findViewById(R.id.btnClear);
        rvEvents      = v.findViewById(R.id.rvEvents);
        progress      = v.findViewById(R.id.progress);
        tvEmpty       = v.findViewById(R.id.tvEmpty);

        // Grid like web: 2 cards per row (phones portrait)
        rvEvents.setLayoutManager(new GridLayoutManager(requireContext(), 1));
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
            @Override public void onAgenda(Event e){ showAgendaDialog(e.getId()); }
        });
        rvEvents.setAdapter(adapter);

        tvDateFilter.setOnClickListener(v1 -> openDatePicker());
        btnApply.setOnClickListener(v12 -> applyFilters());
        btnClear.setOnClickListener(v13 -> clearFilters());

        fetchEventTypes();  // fills spinner
        loadEvents();       // loads user's events
    }
    private void showAgendaDialog(int eventId) {
        AgendaDialogFragment.newInstance(eventId).show(
                getParentFragmentManager(), "agenda_dialog");
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

    private void confirmDeleteDialog(Event a) {
        // 1) Inflate your custom layout
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.delete_dialog_box, null, false);

        // 2) Build a dialog WITHOUT default buttons
        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();

        // Optional: let your rounded background show edge-to-edge
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            );
        }

        // 3) Hook up your custom views
        TextView tvMsg   = view.findViewById(R.id.dialog_message);
        Button btnYes    = view.findViewById(R.id.button_yes);
        Button btnNo     = view.findViewById(R.id.button_no);

        // Optional: dynamic message
        tvMsg.setText("Are you sure you want to delete \"" + a.name + "\"?");

        // 4) Button actions
        btnNo.setOnClickListener(v -> dlg.dismiss());

        btnYes.setOnClickListener(v -> {
            // prevent double taps while the call is in flight
            btnYes.setEnabled(false);
            RetrofitClient.eventService.deleteEvent(a.id).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) {
                    if (!isAdded()) return;
                    btnYes.setEnabled(true);
                    if (r.isSuccessful()) {
                        toast("Deleted");
                        dlg.dismiss();
                        loadEvents();
                    } else {
                        toast("Delete failed: " + r.code());
                    }
                }
                @Override public void onFailure(retrofit2.Call<Void> c, Throwable t) {
                    if (!isAdded()) return;
                    btnYes.setEnabled(true);
                    toast("Error: " + t.getMessage());
                }
            });
        });

        // 5) Show it
        dlg.show();
    }
    private void toast(String s) { Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }



    private void showUpdateDialog(Event e) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_event_update, null, false);

        EditText etName   = dialogView.findViewById(R.id.etName);
        EditText etDesc   = dialogView.findViewById(R.id.etDescription);
        EditText etMax    = dialogView.findViewById(R.id.etMax);
        TextView tvDate   = dialogView.findViewById(R.id.tvDate);
        TextView tvTime   = dialogView.findViewById(R.id.tvTime);
        RadioButton rbPriv= dialogView.findViewById(R.id.rbPrivate);
        RadioButton rbPubl= dialogView.findViewById(R.id.rbPublic);

        EditText etCountry   = dialogView.findViewById(R.id.etCountry);
        EditText etCity      = dialogView.findViewById(R.id.etCity);
        EditText etStreet    = dialogView.findViewById(R.id.etStreet);
        EditText etHouseNo   = dialogView.findViewById(R.id.etHouseNo);
        EditText etZip       = dialogView.findViewById(R.id.etZip);
        // prefill
        etName.setText(e.getName());
        etDesc.setText(e.getDescription());
        if (e.getMaxAttendance() != null) etMax.setText(String.valueOf(e.getMaxAttendance()));
        String[] dt = splitDateTime(safe(e.getDate())); // ["yyyy-MM-dd","HH:mm"]
        tvDate.setText(dt[0].isEmpty() ? "yyyy-MM-dd" : dt[0]);
        tvTime.setText(dt[1].isEmpty() ? "--:--" : dt[1]);
        if ("PUBLIC".equalsIgnoreCase(e.getStatus().toString())) rbPubl.setChecked(true); else rbPriv.setChecked(true);

        // Address
        if (e.getAddress() != null) {
            etCountry.setText(safe(e.getAddress().getCountry()));
            etCity.setText(safe(e.getAddress().getCity()));
            etStreet.setText(safe(e.getAddress().getStreet()));
            etHouseNo.setText(safe(e.getAddress().getHouseNumber()));
            etZip.setText(safe(e.getAddress().getPostalNumber())); // or getZip() depending on your model
        }
        // pickers
        tvDate.setOnClickListener(v -> openDatePickerInto(tvDate));
        tvTime.setOnClickListener(v -> openTimePickerInto(tvTime));

        androidx.appcompat.app.AlertDialog dlg = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
                Address addr = new Address(
                        safe(etCountry.getText().toString()),
                        safe(etCity.getText().toString()),
                        safe(etStreet.getText().toString()),
                        safe(etHouseNo.getText().toString()),
                        safe(etZip.getText().toString())
                );

                EventUpdateDTO body = new EventUpdateDTO();
                body.name = name;
                body.description = etDesc.getText().toString().trim();
                body.status = rbPubl.isChecked() ? EventStatus.PUBLIC : EventStatus.PRIVATE;
                body.date = dateIso;
                body.address = addr;
                body.eventType = e.getEventType();
                body.photos = e.getPhotos();
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
