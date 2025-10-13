package com.example.eveant.eventType;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.databinding.DialogEventTypeCreateBinding;
import com.example.eveant.databinding.FragmentEventTypeBinding;
import com.example.eveant.databinding.ItemEventTypeRowBinding;
import com.example.eveant.service.model.Category;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventTypeFragment extends Fragment {

    private FragmentEventTypeBinding binding;
    private String token;

    private final List<EventType> eventTypes = new ArrayList<>();
    private final EventTypeAdapter eventTypeAdapter = new EventTypeAdapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventTypeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sp = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        token = sp.getString("token", "");

        setupRecycler();
        fetchEventTypesAll();

        // Non-Material create button
        binding.ibCreate.setOnClickListener(v -> openCreateDialog());

        return root;
    }

    private void setupRecycler() {
        binding.rvEventTypes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEventTypes.setAdapter(eventTypeAdapter);
        eventTypeAdapter.setOnToggleActive((et, newActive, position) -> toggleEventTypeOnServer(et, newActive, position));
        eventTypeAdapter.setOnEditClick(this::openEditDialog);
    }

    private void fetchEventTypesAll() {
        decodeAndLogToken(token);
        RetrofitClient.eventTypeService.getAll().enqueue(new Callback<List<EventType>>() {
            @Override public void onResponse(Call<List<EventType>> call, Response<List<EventType>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(resp.body());
                    eventTypeAdapter.submit(eventTypes);
                } else {
                    Log.e("EventTypeFragment", "fetch all: " + resp.code());
                    eventTypeAdapter.submit(Collections.emptyList());
                }
            }
            @Override public void onFailure(Call<List<EventType>> call, Throwable t) {
                Log.e("EventTypeFragment", "fetch all error", t);
                eventTypeAdapter.submit(Collections.emptyList());
            }
        });
    }

    static class EventTypePatch {
        Boolean active;
        EventTypePatch(Boolean a){ this.active = a; }
    }

    private void toggleEventTypeOnServer(EventType et, boolean newActive, int adapterPosition) {
        EventType body = buildUpdateBody(et);
        body.setActive(newActive);
        RetrofitClient.eventTypeService.update(et.getId(), body)
                .enqueue(new Callback<EventType>() {
                    @Override public void onResponse(Call<EventType> c, Response<EventType> r) {
                        if (r.isSuccessful() && r.body()!=null) {
                            et.setActive(r.body().getActive());
                            eventTypeAdapter.notifyItemChanged(adapterPosition);
                        } else {
                            et.setActive(!newActive);
                            eventTypeAdapter.notifyItemChanged(adapterPosition);
                        }
                        Toast.makeText(requireContext(),
                                newActive ? "Event type activated" : "Event type deactivated",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onFailure(Call<EventType> c, Throwable t) {
                        et.setActive(!newActive);
                        eventTypeAdapter.notifyItemChanged(adapterPosition);
                    }
                });
    }

    // ----- Create dialog (classic AlertDialog, no Material style) -----
    private void openCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        DialogEventTypeCreateBinding db = DialogEventTypeCreateBinding.inflate(getLayoutInflater());
        builder.setView(db.getRoot());
        AlertDialog dialog = builder.create();

        final Set<Integer> selectedIds = new HashSet<>();
        final List<Category> allCats = new ArrayList<>();
        final AttachCategoryAdapter attachAdapter = new AttachCategoryAdapter(allCats, selectedIds);

        db.rvAllCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        db.rvAllCategories.setAdapter(attachAdapter);

        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    allCats.clear(); allCats.addAll(resp.body());
                    attachAdapter.notifyDataSetChanged();
                } else Log.e("EventTypeFragment", "load cats: " + resp.code());
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) { Log.e("EventTypeFragment", "load cats error", t); }
        });

        db.btnCreate.setOnClickListener(v -> {
            String name = db.etName.getText().toString().trim();
            String desc = db.etDescription.getText().toString().trim();

            EventType newEt = new EventType();
            newEt.setName(name); newEt.setDescription(desc);
            newEt.setActive(true); newEt.setVirtual(false);

            List<Category> chosen = new ArrayList<>();
            for (Category c : allCats) if (selectedIds.contains(c.getId())) chosen.add(c);
            newEt.setSuggestedCategories(chosen);

            RetrofitClient.eventTypeService.create(newEt).enqueue(new Callback<EventType>() {
                @Override public void onResponse(Call<EventType> call, Response<EventType> resp) {
                    if (resp.isSuccessful() && resp.body()!=null) {
                        eventTypes.add(0, resp.body());
                        eventTypeAdapter.submit(eventTypes);
                    } else Log.e("EventTypeFragment","create et: "+resp.code());
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<EventType> call, Throwable t) { Log.e("EventTypeFragment","create et error", t); dialog.dismiss(); }
            });
        });

        dialog.show();
    }

    private EventType buildUpdateBody(EventType src) {
        EventType b = new EventType(); // NEW empty object; we won't set id
        b.setName(src.getName());
        b.setDescription(src.getDescription());
        b.setSuggestedCategories(src.getSuggestedCategories());
        b.setActive(src.getActive());
        b.setVirtual(src.getVirtual());
        return b; // id remains null -> Gson won't serialize it
    }

    private void openEditDialog(EventType et) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        DialogEventTypeCreateBinding db = DialogEventTypeCreateBinding.inflate(getLayoutInflater());
        builder.setView(db.getRoot());
        AlertDialog dialog = builder.create();

        db.etName.setText(et.getName());
        db.etDescription.setText(et.getDescription());
        db.btnCreate.setText("Save");

        final Set<Integer> selectedIds = new HashSet<>();
        final List<Category> allCats = new ArrayList<>();
        final AttachCategoryAdapter attachAdapter = new AttachCategoryAdapter(allCats, selectedIds);

        if (et.getSuggestedCategories()!=null) for (Category c : et.getSuggestedCategories()) selectedIds.add(c.getId());

        db.rvAllCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        db.rvAllCategories.setAdapter(attachAdapter);

        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    allCats.clear(); allCats.addAll(resp.body());
                    attachAdapter.notifyDataSetChanged();
                } else Log.e("EventTypeFragment","load cats (edit): "+resp.code());
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) { Log.e("EventTypeFragment","load cats (edit) err", t); }
        });

        db.btnCreate.setOnClickListener(v -> {
            et.setName(db.etName.getText().toString().trim());
            et.setDescription(db.etDescription.getText().toString().trim());

            List<Category> chosen = new ArrayList<>();
            for (Category c : allCats) if (selectedIds.contains(c.getId())) chosen.add(c);
            et.setSuggestedCategories(chosen);
            int id = et.getId();                      // keep path id
            EventType body = buildUpdateBody(et);
            RetrofitClient.eventTypeService.update(id, body).enqueue(new Callback<EventType>() {
                @Override public void onResponse(Call<EventType> call, Response<EventType> resp) {
                    if (resp.isSuccessful()) {
                        for (int i=0;i<eventTypes.size();i++) if (eventTypes.get(i).getId().equals(et.getId())) { eventTypes.set(i, et); break; }
                        eventTypeAdapter.submit(eventTypes);
                    } else Log.e("EventTypeFragment","update et: "+resp.code());
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<EventType> call, Throwable t) { Log.e("EventTypeFragment","update et err", t); dialog.dismiss(); }
            });
        });

        dialog.show();
    }

    private void decodeAndLogToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(payload);
                Log.d("EventTypeFragment", "email=" + jsonObject.optString("sub") + ", role=" + jsonObject.optString("role"));
            }
        } catch (Exception e) { Log.w("EventTypeFragment", "Token decode failed", e); }
    }
}
