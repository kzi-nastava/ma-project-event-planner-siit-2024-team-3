package com.example.eveant.event.agenda;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.EventActivity;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.user.model.Address;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaFragment extends Fragment {

    // Visible day window
    private static final int DAY_START_HOUR = 8;    // 08:00
    private static final int DAY_END_HOUR   = 22;   // 22:00
    private static final int PX_PER_MIN     = 2;    // vertical scale: 1 min = 2 px

    private LinearLayout hoursColumn;
    private FrameLayout timelineCanvas;
    private Button btnAdd;
    private com.example.eveant.event.EventCreationViewModel vm;
    private java.time.LocalDate eventDay;

    private ActivityCardAdapter listAdapter;
    private final List<Activity> activities = new ArrayList<>();
    private int eventId = -1;

    @SuppressLint("NewApi")
    private final DateTimeFormatter iso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static AgendaFragment newInstance(int eventId) {
        Bundle b = new Bundle();
        b.putInt("eventId", eventId);
        AgendaFragment f = new AgendaFragment();
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        eventId = getArguments() != null ? getArguments().getInt("eventId", -1) : -1;

    }

    @Override public @Nullable View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {

        return inf.inflate(R.layout.fragment_agenda, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        hoursColumn    = v.findViewById(R.id.hoursColumn);
        timelineCanvas = v.findViewById(R.id.timelineCanvas);
        btnAdd         = v.findViewById(R.id.btnAddActivity);
        vm = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(com.example.eveant.event.EventCreationViewModel.class);
        vm.getEventDate().observe(getViewLifecycleOwner(), d -> eventDay = d);
        listAdapter = new ActivityCardAdapter(activities, new ActivityCardAdapter.Listener() {
            @Override public void onEdit(Activity a) { showAddEditDialog(a); }
            @Override public void onDelete(Activity a) { confirmDelete(a); }
        });

        renderHourScaleVertical();
        btnAdd.setOnClickListener(v1 -> showAddEditDialog(null));

        fetch();
    }

    /** Called by EventCreationActivity when user taps Next on this step. */
    public void handleNext() {
        Boolean pub = vm.getIsPublic().getValue();
        boolean isPublic = pub != null && pub;

        if (requireActivity() instanceof EventActivity) {
            if (isPublic) {
                // Skip invitations entirely for public events
                requireActivity().finish();
            } else {
                ((EventActivity) requireActivity()).showStep(EventActivity.Step.INVITATIONS, true);
            }
        }
    }

    // --- Data ---

    private void fetch() {
        Call<List<Activity>> call = (eventId > 0)
                ? RetrofitClient.activityService.getByEventId(eventId)
                : RetrofitClient.activityService.getAll();

        call.enqueue(new Callback<List<Activity>>() {
            @Override public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                if (!r.isSuccessful() || r.body() == null) { toast("Failed: " + r.code()); return; }
                activities.clear();
                activities.addAll(r.body());
                activities.sort(Comparator.comparing(a -> parseBackendDate(a.startTime)));
                listAdapter.notifyDataSetChanged();
                drawTimelineVertical();
            }
            @Override public void onFailure(Call<List<Activity>> c, Throwable t) { toast("Error: " + t.getMessage()); }
        });
    }

    // --- Vertical UI ---

    private void renderHourScaleVertical() {
        hoursColumn.removeAllViews();

        int totalMinutes = (DAY_END_HOUR - DAY_START_HOUR) * 60;
        int canvasHeight = Math.max(dp(200), totalMinutes * PX_PER_MIN);

        // Build vertical labels at each hour mark
        for (int hour = DAY_START_HOUR; hour <= DAY_END_HOUR; hour++) {
            TextView tv = new TextView(getContext());
            tv.setText(String.format(Locale.getDefault(), "%02d:00", hour));
            tv.setTextColor(Color.BLACK);                 // readable on black
            tv.setPadding(dp(8), 0, dp(8), 0);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    hour == DAY_START_HOUR ? ViewGroup.LayoutParams.WRAP_CONTENT : PX_PER_MIN * 60
            );
            tv.setLayoutParams(lp);
            hoursColumn.addView(tv);
        }

        // Ensure the canvas matches the full day height
        ViewGroup.LayoutParams clp = timelineCanvas.getLayoutParams();
        clp.height = canvasHeight;
        timelineCanvas.setLayoutParams(clp);
        timelineCanvas.requestLayout();

        // (Optional) faint hour separator lines across the canvas
        timelineCanvas.post(() -> {
            timelineCanvas.removeViewsInLayout(0, timelineCanvas.getChildCount());
            for (int hour = DAY_START_HOUR; hour <= DAY_END_HOUR; hour++) {
                View line = new View(getContext());
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, dp(1));
                lp.topMargin = (hour - DAY_START_HOUR) * 60 * PX_PER_MIN;
                line.setLayoutParams(lp);
                line.setBackgroundColor(0x33FFFFFF); // 20% white
                timelineCanvas.addView(line);
            }
            // activities are added later by drawTimelineVertical()
            drawTimelineVertical();
        });
    }


    private void drawTimelineVertical() {
        timelineCanvas.removeAllViews();

        for (Activity a : activities) {
            LocalDateTime s = parseBackendDate(a.startTime);
            LocalDateTime e = parseBackendDate(a.endTime);
            if (s == null || e == null) continue;

            int top = minutesFromDayStart(s) * PX_PER_MIN;
            @SuppressLint({"NewApi", "LocalSuppress"})
            int height = Math.max(dp(48), (int) Duration.between(s, e).toMinutes() * PX_PER_MIN);

            View card = LayoutInflater.from(getContext()).inflate(R.layout.item_activity_card, timelineCanvas, false);

            TextView tvTitle = card.findViewById(R.id.tvTitle);
            TextView tvTime  = card.findViewById(R.id.tvTime);
            TextView tvAddr  = card.findViewById(R.id.tvAddress);
            TextView tvDesc  = card.findViewById(R.id.tvDesc);
            ImageButton btnE = card.findViewById(R.id.btnEdit);
            ImageButton btnD = card.findViewById(R.id.btnDelete);

            tvTitle.setText(a.name);
            tvTime.setText(fmt(s) + " - " + fmt(e));
            String address = joinNonEmpty(", ", a.address.getStreet(), a.address.getHouseNumber(), a.address.getCity());
            tvAddr.setText(address);
            tvAddr.setVisibility(TextUtils.isEmpty(address) ? View.GONE : View.VISIBLE);
            tvDesc.setText(a.description);
            try { card.getBackground().setTint(Color.parseColor(a.color)); } catch (Exception ignored) {}

            // Position by topMargin; width fills canvas
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    height
            );
            lp.topMargin = top;
            lp.leftMargin = dp(4);
            lp.rightMargin = dp(4);
            card.setLayoutParams(lp);

            btnE.setOnClickListener(v -> showAddEditDialog(a));
            btnD.setOnClickListener(v -> confirmDelete(a));

            timelineCanvas.addView(card);
        }
    }

    private void confirmDelete(Activity a) {
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
            RetrofitClient.activityService.delete(a.id).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) {
                    if (!isAdded()) return;
                    btnYes.setEnabled(true);
                    if (r.isSuccessful()) {
                        toast("Deleted");
                        dlg.dismiss();
                        fetch(); // refresh timeline
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


    // --- Dialog & helpers ---

    @SuppressLint("NewApi")
    private void showAddEditDialog(@Nullable Activity edit) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_activity, null, false);

        EditText etName   = view.findViewById(R.id.etName);
        EditText etDesc   = view.findViewById(R.id.etDesc);
        EditText etStreet = view.findViewById(R.id.etStreet);
        EditText etHouse  = view.findViewById(R.id.etHouse);
        EditText etCity   = view.findViewById(R.id.etCity);
        TextView tvStart  = view.findViewById(R.id.tvStart);
        TextView tvEnd    = view.findViewById(R.id.tvEnd);
        LinearLayout colorRow = view.findViewById(R.id.colorRow);
        Button btnSubmit  = view.findViewById(R.id.btnSubmit);

        final String[] pickedColor = { "#7386E0" };
        setUpColorDots(colorRow, pickedColor);

        final LocalTime[] chosenStart = { LocalTime.of(10, 0) };
        final LocalTime[] chosenEnd   = { LocalTime.of(11, 0) };

        tvStart.setOnClickListener(v -> timePick(chosenStart, tvStart));
        tvEnd.setOnClickListener(v -> timePick(chosenEnd, tvEnd));

        if (edit != null) {
            etName.setText(edit.name);
            etDesc.setText(edit.description);
            etStreet.setText(edit.address.getStreet());
            etHouse.setText(edit.address.getHouseNumber());
            etCity.setText(edit.address.getCity());
            LocalDateTime s = parseBackendDate(edit.startTime);
            LocalDateTime e = parseBackendDate(edit.endTime);
            if (s != null) { chosenStart[0] = s.toLocalTime(); tvStart.setText(fmt(chosenStart[0])); }
            if (e != null) { chosenEnd[0]   = e.toLocalTime(); tvEnd.setText(fmt(chosenEnd[0])); }
            pickedColor[0] = safeColor(edit.color, pickedColor[0]);
        } else {
            tvStart.setText(fmt(chosenStart[0]));
            tvEnd.setText(fmt(chosenEnd[0]));
        }

        AlertDialog dlg = new AlertDialog.Builder(getContext())
                .setTitle(edit == null ? "Add event activity" : "Edit activity")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .create();

        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_white_20));
        }

// Purple buttons
        dlg.setOnShowListener(d -> {
            int purple = ContextCompat.getColor(requireContext(), R.color.purple);
            Button pos = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            Button neg = dlg.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (pos != null) pos.setTextColor(purple);
            if (neg != null) neg.setTextColor(purple);
        });

        btnSubmit.setOnClickListener(v -> {
            String name = String.valueOf(etName.getText()).trim();
            if (TextUtils.isEmpty(name)) { etName.setError("Required"); return; }
            java.time.LocalDate day = (eventDay != null) ? eventDay : java.time.LocalDate.now();
            String startIso = LocalDateTime.of(day, chosenStart[0]).format(iso);
            String endIso   = LocalDateTime.of(day, chosenEnd[0]).format(iso);

            ActivityDTO create = new ActivityDTO();
            create.event = eventId;
            create.name = name;
            create.description = String.valueOf(etDesc.getText());
            create.startTime = startIso;
            create.endTime = endIso;
            create.color = pickedColor[0];
            String street = String.valueOf(etStreet.getText());
            String houseNumber = String.valueOf(etHouse.getText());
            String city = String.valueOf(etCity.getText());
            Address address = new Address("", city, street, houseNumber, "");
            create.address = address;


            if (edit == null) {
                RetrofitClient.activityService.create(create).enqueue(new Callback<ActivityDTO>() {
                    @Override public void onResponse(Call<ActivityDTO> c, Response<ActivityDTO> r) {
                        if (r.isSuccessful()) { toast("Created"); dlg.dismiss(); fetch(); }
                        else toast("Create failed: " + r.code());
                    }
                    @Override public void onFailure(Call<ActivityDTO> c, Throwable t) { toast("Error: " + t.getMessage()); }
                });
            } else {
                Activity dto = new Activity();
                dto.id = edit.id;
                dto.event = eventId;
                dto.name = name;
                dto.description = String.valueOf(etDesc.getText());
                dto.startTime = startIso;
                dto.endTime = endIso;
                dto.color = pickedColor[0];
                dto.address = address;
                RetrofitClient.activityService.update(edit.id, dto).enqueue(new Callback<Activity>() {
                    @Override public void onResponse(Call<Activity> c, Response<Activity> r) {
                        if (r.isSuccessful()) { toast("Updated"); dlg.dismiss(); fetch(); }
                        else toast("Update failed: " + r.code());
                    }
                    @Override public void onFailure(Call<Activity> c, Throwable t) { toast("Error: " + t.getMessage()); }
                });
            }
        });

        dlg.show();
    }

    private void setUpColorDots(LinearLayout wrap, String[] picked) {
        wrap.removeAllViews();
        String[] palette = {"#86D5FF","#D4B8FF","#B8EBDD","#3E5CCF","#F5F0AA","#F5D44F"};
        for (String hex : palette) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(28), dp(28));
            lp.setMargins(dp(6), dp(6), dp(6), dp(6));
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.bg_color_dot);
            dot.getBackground().setTint(Color.parseColor(hex));
            dot.setOnClickListener(v -> { picked[0] = hex; toast("Color picked"); });
            wrap.addView(dot);
        }
    }

    @SuppressLint("NewApi")
    private void timePick(LocalTime[] box, TextView target) {
        int h = box[0].getHour(), m = box[0].getMinute();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            box[0] = LocalTime.of(hourOfDay, minute);
            target.setText(fmt(box[0]));
        }, h, m, true).show();
    }

    private String fmt(LocalTime t) { return t.toString(); }
    @SuppressLint("NewApi")
    private String fmt(LocalDateTime t) { return t.toLocalTime().toString(); }

    private int minutesFromDayStart(LocalDateTime t) {
        @SuppressLint({"NewApi", "LocalSuppress"}) int mins = t.getHour() * 60 + t.getMinute() - DAY_START_HOUR * 60;
        return Math.max(0, mins);
    }

    @SuppressLint("NewApi")
    private LocalDateTime parseBackendDate(String s) {
        try { return LocalDateTime.parse(s, iso); }
        catch (Exception e) { return null; }
    }

    private String joinNonEmpty(String sep, String... parts) {
        List<String> out = new ArrayList<>();
        for (String p : parts) if (!TextUtils.isEmpty(p)) out.add(p);
        return TextUtils.join(sep, out);
    }

    private int dp(int v) { float d = getResources().getDisplayMetrics().density; return (int)(v * d); }
    private String safeColor(String c, String def) { try { Color.parseColor(c); return c; } catch (Exception e) { return def; } }
    private void toast(String s) { Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }
}
