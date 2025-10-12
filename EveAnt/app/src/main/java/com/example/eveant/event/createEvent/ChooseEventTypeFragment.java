package com.example.eveant.event.createEvent;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.EventCreationViewModel;
import com.example.eveant.eventType.EventType;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseEventTypeFragment extends Fragment {

    private LinearLayout llEventTypes;
    private TextView tvError;
    private ProgressBar progress;

    private final List<EventType> types = new ArrayList<>();
    private final List<Button> typeButtons = new ArrayList<>();
    private Button btnAll;             // the *real* ALL from your backend
    private EventType allType;         // ref to the server “ALL” object
    private EventType selected;        // currently selected type

    private EventCreationViewModel vm;

    public ChooseEventTypeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_event_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        llEventTypes = v.findViewById(R.id.llEventTypes);
        tvError = v.findViewById(R.id.tvError);
        progress = v.findViewById(R.id.progress);

        vm = new ViewModelProvider(requireActivity()).get(EventCreationViewModel.class);
        fetchEventTypes();
    }

    private void fetchEventTypes() {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        RetrofitClient.eventTypeService.getAllActivated().enqueue(new Callback<List<EventType>>() {
            @Override public void onResponse(Call<List<EventType>> call, Response<List<EventType>> resp) {
                showLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    showError("Failed to load event types (" + resp.code() + ")");
                    return;
                }
                types.clear();
                types.addAll(resp.body());
                orderAllFirst(types);
                renderButtons(types);
            }
            @Override public void onFailure(Call<List<EventType>> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    /** Put your real ALL event type at index 0 if present. */
    private void orderAllFirst(List<EventType> list) {
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (isAllType(list.get(i))) { idx = i; break; }
        }
        if (idx > 0) {
            EventType et = list.remove(idx);
            list.add(0, et);
        }
    }

    /** Heuristic to detect your existing ALL: adjust if you have a dedicated flag. */
    private boolean isAllType(EventType et) {
        if (et == null) return false;
        // Prefer explicit flag if you have one:
        // return Boolean.TRUE.equals(et.getVirtual()) || Boolean.TRUE.equals(et.getAll());
        String n = et.getName();
        return n != null && n.trim().equalsIgnoreCase("ALL");
    }

    private void renderButtons(List<EventType> data) {
        llEventTypes.removeAllViews();
        typeButtons.clear();
        selected = null;
        btnAll = null;
        allType = null;

        if (data == null || data.isEmpty()) {
            showError("No event types available.");
            return;
        }

        for (EventType et : data) {
            String label = !TextUtils.isEmpty(et.getName()) ? et.getName() : ("EventType " + et.getId());
            Button b = createChoiceButton(label);
            b.setTag(et);

            if (isAllType(et)) {
                btnAll = b;
                allType = et;
                b.setOnClickListener(v -> {
                    // visually: ALL + every other pill selected
                    setAllVisualSelected(true);
                    selected = allType;
                    vm.setSelectedType(allType); // use your real ALL object
                    Toast.makeText(requireContext(), "Selected: " + label, Toast.LENGTH_SHORT).show();
                    // NO auto-advance here
                });
            } else {
                b.setOnClickListener(v -> {
                    setAllVisualSelected(false);   // ALL off; others off
                    setOnlyThisSelected(b);        // this one on
                    selected = (EventType) b.getTag();
                    vm.setSelectedType(selected);
                    Toast.makeText(requireContext(), "Selected: " + label, Toast.LENGTH_SHORT).show();
                    // NO auto-advance here
                });
            }

            llEventTypes.addView(b);
            typeButtons.add(b);
        }
    }

    /** Create a pill button that uses our selector drawable. */
    private Button createChoiceButton(String text) {
        Button btn = new Button(requireContext(), null, androidx.appcompat.R.attr.buttonStyle);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setBackgroundResource(R.drawable.bg_event_type_choice);
        btn.setTextColor(0xFF8599E0); // default; flips to white when selected
        btn.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = dp(8);
        btn.setLayoutParams(lp);
        btn.setSelected(false);
        return btn;
    }

    /** Only this button selected (ALL unselected). */
    private void setOnlyThisSelected(Button target) {
        if (btnAll != null) mark(btnAll, false);
        for (Button b : typeButtons) mark(b, b == target);
    }

    /** When ALL is pressed: ALL + every pill looks selected; otherwise everything off. */
    private void setAllVisualSelected(boolean sel) {
        if (btnAll != null) mark(btnAll, sel);
        for (Button b : typeButtons) mark(b, sel);
    }

    private void mark(Button b, boolean sel) {
        b.setSelected(sel); // drives bg_choice_button
        b.setTextColor(sel ? 0xFFFFFFFF : 0xFF8599E0);
    }

    private void showLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        if (tvError != null) {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private int dp(int v) {
        float s = getResources().getDisplayMetrics().density;
        return (int) (v * s + 0.5f);
    }
}
