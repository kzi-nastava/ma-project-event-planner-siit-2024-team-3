package com.example.eveant.event.createEvent;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.eventType.EventType;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseEventTypeFragment extends Fragment {

    private RadioGroup rgEventTypes;
    private TextView tvTitle, tvSubtitle, tvError;
    private TextView step1, step2, step3, step4, step5;
    private Button btnBack, btnNext;
    private ProgressBar progress;

    private List<EventType> types = new ArrayList<>();
    private EventType selected;

    public ChooseEventTypeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_event_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        rgEventTypes = v.findViewById(R.id.rgEventTypes);
        tvTitle = v.findViewById(R.id.tvTitle);
        tvSubtitle = v.findViewById(R.id.tvSubtitle);
        tvError = v.findViewById(R.id.tvError);
        step1 = v.findViewById(R.id.step1);
        step2 = v.findViewById(R.id.step2);
        step3 = v.findViewById(R.id.step3);
        step4 = v.findViewById(R.id.step4);
        step5 = v.findViewById(R.id.step5);
        btnBack = v.findViewById(R.id.btnBack);
        btnNext = v.findViewById(R.id.btnNext);
        progress = v.findViewById(R.id.progress);

        forceBlack(step1, step2, step3, step4, step5, tvTitle, tvSubtitle, tvError);

        highlightStep(1);

        btnBack.setOnClickListener(view -> requireActivity().onBackPressed());
        btnNext.setOnClickListener(view -> {
            if (selected == null) return;
            // inside ChooseEventTypeFragment, in btnNext.setOnClickListener(...)
            Fragment next = new BasicInformationFragment();

// (optional) pass data
            Bundle args = new Bundle();
            args.putParcelable("eventType", selected);
            next.setArguments(args);

// do the transition
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.slide_in_left,   // enter
                            android.R.anim.fade_out,        // exit
                            android.R.anim.fade_in,         // popEnter
                            android.R.anim.slide_out_right  // popExit
                    )
                    .replace(R.id.fragmentContainer, next) // container in your activity XML
                    .addToBackStack("BasicEventInfo")      // enables back button
                    .commit();

            Toast.makeText(requireContext(),
                    "Next → " + selected.getName() + " (id=" + selected.getId() + ")",
                    Toast.LENGTH_SHORT).show();
        });

        fetchEventTypes();
    }

    private void fetchEventTypes() {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        // choose the endpoint you want; activated is typical for UX
        RetrofitClient.eventTypeService.getAllActivated().enqueue(new Callback<List<EventType>>() {
            @Override public void onResponse(Call<List<EventType>> call, Response<List<EventType>> resp) {
                showLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    showError("Failed to load event types (" + resp.code() + ")");
                    return;
                }
                types = resp.body();
                populateEventTypeRadios(types);
            }
            @Override public void onFailure(Call<List<EventType>> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void populateEventTypeRadios(List<EventType> data) {
        rgEventTypes.removeAllViews();
        btnNext.setEnabled(false);
        selected = null;

        if (data == null || data.isEmpty()) {
            showError("No event types available.");
            return;
        }

        for (EventType et : data) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setText(!TextUtils.isEmpty(et.getName()) ? et.getName() : ("EventType " + et.getId()));
            rb.setTextColor(0xFF000000); // black text
            rb.setPadding(dp(12), dp(8), dp(12), dp(8));
            rb.setTag(et);
            rgEventTypes.addView(rb);
        }

        rgEventTypes.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null && rb.getTag() instanceof EventType) {
                selected = (EventType) rb.getTag();
                btnNext.setEnabled(true);
            }
        });
    }

    private void highlightStep(int stepIndex) {
        // bold the active step, normal the others
        TextView[] arr = new TextView[]{step1, step2, step3, step4, step5};
        for (int i = 0; i < arr.length; i++) {
            arr[i].setTypeface(null, (i + 1 == stepIndex) ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void forceBlack(TextView... tviews) {
        for (TextView t : tviews) if (t != null) t.setTextColor(0xFF000000);
    }

    private int dp(int v) {
        float s = getResources().getDisplayMetrics().density;
        return (int) (v * s + 0.5f);
    }
}
