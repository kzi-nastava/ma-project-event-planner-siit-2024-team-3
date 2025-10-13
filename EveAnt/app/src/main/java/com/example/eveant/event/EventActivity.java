package com.example.eveant.event;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.event.createEvent.BasicInformationFragment;
import com.example.eveant.event.createEvent.ChooseEventTypeFragment;
import com.example.eveant.event.agenda.AgendaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EventActivity extends AppCompatActivity {

    public enum Step {
        CHOOSE_TYPE, BASIC_INFO, AGENDA, INVITATIONS
    }

    private Step current = Step.CHOOSE_TYPE;

    private Button btnBack, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event);

        btnBack = findViewById(R.id.goToBack);
        btnNext = findViewById(R.id.goToNext);

        btnBack.setOnClickListener(v -> goPrev());
        btnNext.setOnClickListener(v -> goNext());
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        if (bnv != null) {
            bnv.getMenu().findItem(R.id.createEvent).setChecked(true);
        }
        if (savedInstanceState == null) {
            showStep(Step.CHOOSE_TYPE, false);
        } else {
            String name = savedInstanceState.getString("step", Step.CHOOSE_TYPE.name());
            current = Step.valueOf(name);
            showStep(current, false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("step", current.name());
    }

    public void goNext() {
        switch (current) {
            case CHOOSE_TYPE:
                showStep(Step.BASIC_INFO, true);
                break;

            case BASIC_INFO: {
                Fragment f = getSupportFragmentManager().findFragmentByTag(Step.BASIC_INFO.name());
                if (f instanceof BasicInformationFragment) {
                    // Let the fragment validate + create event; it will call showStep(AGENDA) on success
                    ((BasicInformationFragment) f).handleNext();
                } else {
                    showStep(Step.BASIC_INFO, true);
                }
                break;
            }

            case AGENDA:
                showStep(Step.INVITATIONS, true);
                break;

            case INVITATIONS:
                finish();
                break;
        }
    }

    public void goPrev() {
        switch (current) {
            case CHOOSE_TYPE:
                finish();
                break;
            case BASIC_INFO:
                showStep(Step.CHOOSE_TYPE, true);
                break;
            case AGENDA:
                showStep(Step.BASIC_INFO, true);
                break;
            case INVITATIONS:
                showStep(Step.AGENDA, true);
                break;
        }
    }

    public void showStep(Step step, boolean animate) {
        current = step;

        Fragment fragment;
        switch (step) {
            case CHOOSE_TYPE:
                fragment = new ChooseEventTypeFragment();
                break;

            case BASIC_INFO:
                fragment = new BasicInformationFragment();
                break;

            case AGENDA: {
                // Create AGENDA with the freshly created eventId from the VM
                EventCreationViewModel vm = new ViewModelProvider(this).get(EventCreationViewModel.class);
                Integer id = vm.getEventId().getValue();
                fragment = (id != null && id > 0) ? com.example.eveant.event.agenda.AgendaFragment.newInstance(id)
                        : new com.example.eveant.event.agenda.AgendaFragment();
                break;
            }

            case INVITATIONS:
            default:
                fragment = new com.example.eveant.event.InvitationListFragment();
                break;
        }

        androidx.fragment.app.FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        if (animate) {
            tx.setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.slide_out_right
            );
        }
        tx.replace(R.id.container, fragment, step.name());
        tx.commit();

        updateProgress(getIndex(step));
        updateButtons(step);
    }

    private void updateButtons(Step step) {
        btnBack.setEnabled(step != Step.CHOOSE_TYPE);
        btnNext.setText(step == Step.INVITATIONS ? getString(R.string.action_finish)
                : getString(R.string.action_next));
    }

    private int getIndex(Step step) {
        switch (step) {
            case BASIC_INFO:  return 2;
            case AGENDA:      return 3;
            case INVITATIONS: return 4;
            default:          return 1;
        }
    }

    public void updateProgress(int completedStep) {
        TextView[] ellipses = new TextView[] {
                find(R.id.ellipse_1),
                find(R.id.ellipse_2),
                find(R.id.ellipse_3),
                find(R.id.ellipse_4)
        };
        View[] lines = new View[] {
                find(R.id.line_1),
                find(R.id.line_2),
                find(R.id.line_3)
        };

        for (TextView e : ellipses) {
            e.setBackgroundResource(0);
            e.setTextColor(Color.GRAY);
        }
        for (View l : lines) {
            l.setBackgroundResource(R.drawable.line_black);
        }

        for (int i = 0; i < completedStep; i++) {
            if (i < ellipses.length) {
                ellipses[i].setBackgroundResource(R.drawable.rounded_corners_8599e0);
                ellipses[i].setTextColor(getColor(R.color.white));
            }
            if (i < lines.length) {
                lines[i].setBackgroundResource(R.drawable.line_completed);
            }
        }
    }

    private <T extends View> T find(@IdRes int id) { return findViewById(id); }
}
