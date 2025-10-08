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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.RetrofitClient;
import com.example.eveant.databinding.DialogEventTypeCreateBinding;
import com.example.eveant.databinding.FragmentEventTypeBinding;
import com.example.eveant.databinding.ItemAttachCategoryRowBinding;
import com.example.eveant.databinding.ItemEventTypeRowBinding;
import com.example.eveant.service.model.Category;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Web-like tabela: samo lista EventType-ova u RecyclerView-u + Create/Edit dijalozi.
 */
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

        binding.btnCreateEventType.setOnClickListener(v -> openCreateDialog());

        return root;
    }

    private void setupRecycler() {
        binding.rvEventTypes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEventTypes.setAdapter(eventTypeAdapter);
        eventTypeAdapter.setOnToggleActive((et, newActive, position) -> {
            // optimistic UI if you like, but the method above already handles rollback
            toggleEventTypeOnServer(et, newActive, position);
        });
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
        RetrofitClient.eventTypeService.patch(et.getId(), new EventTypePatch(newActive))
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




    // ----- Create dialog -----
    private void openCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        DialogEventTypeCreateBinding db = DialogEventTypeCreateBinding.inflate(getLayoutInflater());
        builder.setView(db.getRoot());
        AlertDialog dialog = builder.create();

        final Set<Integer> selectedIds = new HashSet<>();
        final List<Category> allCats = new ArrayList<>();
        final AttachCategoryAdapter attachAdapter = new AttachCategoryAdapter(allCats, selectedIds);

        db.rvAllCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        db.rvAllCategories.setAdapter(attachAdapter);

        // load categories
        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    allCats.clear();
                    allCats.addAll(resp.body());
                    attachAdapter.notifyDataSetChanged();
                } else {
                    Log.e("EventTypeFragment", "load cats: " + resp.code());
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("EventTypeFragment", "load cats error", t);
            }
        });

        db.btnCreate.setOnClickListener(v -> {
            String name = db.etName.getText().toString().trim();
            String desc = db.etDescription.getText().toString().trim();

            EventType newEt = new EventType();
            newEt.setName(name);
            newEt.setDescription(desc);
            newEt.setActive(true);
            newEt.setVirtual(false);

            // attach selected
            List<Category> chosen = new ArrayList<>();
            for (Category c : allCats) {
                if (selectedIds.contains(c.getId())) chosen.add(c);
            }
            newEt.setSuggestedCategories(chosen);

            RetrofitClient.eventTypeService.create(newEt).enqueue(new Callback<EventType>() {
                @Override public void onResponse(Call<EventType> call, Response<EventType> resp) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        eventTypes.add(0, resp.body());
                        eventTypeAdapter.submit(eventTypes);
                    } else {
                        Log.e("EventTypeFragment", "create et: " + resp.code());
                    }
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<EventType> call, Throwable t) {
                    Log.e("EventTypeFragment", "create et error", t);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // ----- Edit dialog -----
    private void openEditDialog(EventType et) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        DialogEventTypeCreateBinding db = DialogEventTypeCreateBinding.inflate(getLayoutInflater());
        builder.setView(db.getRoot());
        AlertDialog dialog = builder.create();

        db.etName.setText(et.getName());
        db.etDescription.setText(et.getDescription());
        db.btnCreate.setText("Save");

        final Set<Integer> selectedIds = new HashSet<>();
        final List<Category> allCats = new ArrayList<>();
        final AttachCategoryAdapter attachAdapter = new AttachCategoryAdapter(allCats, selectedIds);

        // preselect
        if (et.getSuggestedCategories() != null) {
            for (Category c : et.getSuggestedCategories()) {
                selectedIds.add(c.getId());
            }
        }

        db.rvAllCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        db.rvAllCategories.setAdapter(attachAdapter);

        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    allCats.clear();
                    allCats.addAll(resp.body());
                    attachAdapter.notifyDataSetChanged();
                } else {
                    Log.e("EventTypeFragment", "load cats (edit): " + resp.code());
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("EventTypeFragment", "load cats (edit) err", t);
            }
        });

        db.btnCreate.setOnClickListener(v -> {
            et.setName(db.etName.getText().toString().trim());
            et.setDescription(db.etDescription.getText().toString().trim());

            List<Category> chosen = new ArrayList<>();
            for (Category c : allCats) if (selectedIds.contains(c.getId())) chosen.add(c);
            et.setSuggestedCategories(chosen);

            RetrofitClient.eventTypeService.update(et.getId(), et).enqueue(new Callback<EventType>() {
                @Override public void onResponse(Call<EventType> call, Response<EventType> resp) {
                    if (resp.isSuccessful()) {
                        // refresh local list
                        for (int i = 0; i < eventTypes.size(); i++) {
                            if (eventTypes.get(i).getId().equals(et.getId())) {
                                eventTypes.set(i, et);
                                break;
                            }
                        }
                        eventTypeAdapter.submit(eventTypes);
                    } else {
                        Log.e("EventTypeFragment", "update et: " + resp.code());
                    }
                    dialog.dismiss();
                }
                @Override public void onFailure(Call<EventType> call, Throwable t) {
                    Log.e("EventTypeFragment", "update et err", t);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void updateEventTypeOnServer(EventType et) {
        RetrofitClient.eventTypeService.update(et.getId(), et).enqueue(new Callback<EventType>() {
            @Override public void onResponse(Call<EventType> call, Response<EventType> response) {
                if (!response.isSuccessful()) {
                    Log.e("EventTypeFragment", "toggle active fail: " + response.code());
                }
            }
            @Override public void onFailure(Call<EventType> call, Throwable t) {
                Log.e("EventTypeFragment", "toggle active err", t);
            }
        });
    }

    private void decodeAndLogToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(payload);
                String email = jsonObject.optString("sub");
                String role = jsonObject.optString("role");
                Log.d("EventTypeFragment", "email=" + email + ", role=" + role);
            }
        } catch (Exception e) {
            Log.w("EventTypeFragment", "Token decode failed", e);
        }
    }


    // ===================== ADAPTER ZA TABELU =====================
    private static class EventTypeAdapter extends RecyclerView.Adapter<EventTypeAdapter.VH> {

        interface OnEditClick { void onEdit(EventType et); }
        interface OnToggleActive { void onToggle(EventType et, boolean newActive, int adapterPosition); }

        private final List<EventType> items = new ArrayList<>();
        private OnEditClick editClick;
        private OnToggleActive toggleActive;

        void setOnEditClick(OnEditClick cb) { this.editClick = cb; }
        void setOnToggleActive(OnToggleActive cb) { this.toggleActive = cb; }

        void submit(List<EventType> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ItemEventTypeRowBinding b;
            VH(ItemEventTypeRowBinding b) { super(b.getRoot()); this.b = b; }
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemEventTypeRowBinding b = ItemEventTypeRowBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            EventType it = items.get(position);

            // Title + Description
            h.b.tvTitle.setText(it.getName() == null ? "" : it.getName());
            h.b.tvSubtitle.setText(it.getDescription() == null ? "" : it.getDescription());

            // Suggested categories
            h.b.llSuggestedContainer.removeAllViews();
            if (it.getSuggestedCategories() != null) {
                int pad = (int) (6f * h.itemView.getResources().getDisplayMetrics().density);
                for (Category c : it.getSuggestedCategories()) {
                    TextView tv = new TextView(h.itemView.getContext());
                    tv.setText(c.getName() == null ? "" : c.getName());
                    tv.setTextColor(0xFF2D6DE8);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setMaxLines(1);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                    tv.setPadding(0, pad, 0, pad);
                    h.b.llSuggestedContainer.addView(tv);
                }
            }

            // Edit
            h.b.btnEdit.setOnClickListener(v -> { if (editClick != null) editClick.onEdit(it); });

            // --- Switch binding ---
            boolean isActive = Boolean.TRUE.equals(it.getActive());
            h.b.swActive.setOnCheckedChangeListener(null);
            h.b.swActive.setChecked(isActive);
            applySwitchLabelAndColor(h, isActive);

            h.b.swActive.setOnCheckedChangeListener((buttonView, checked) -> {
                applySwitchLabelAndColor(h, checked);
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && toggleActive != null) {
                    toggleActive.onToggle(it, checked, pos);
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        private void applySwitchLabelAndColor(VH h, boolean active) {
            h.b.swActive.setText(active ? "Active" : "Inactive");
            h.b.swActive.setTextColor(active ? 0xFF2E7D32 : 0xFFD32F2F);
        }
    }


    // ===================== ADAPTER ZA CATEGORIES U DIJALOGU =====================

    private static class AttachCategoryAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<AttachCategoryAdapter.VH> {
        private final List<Category> items;
        private final Set<Integer> selectedIds;

        AttachCategoryAdapter(List<Category> items, Set<Integer> selectedIds) {
            this.items = items; this.selectedIds = selectedIds;
        }

        static class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            final ItemAttachCategoryRowBinding b;
            VH(ItemAttachCategoryRowBinding b) { super(b.getRoot()); this.b = b; }
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAttachCategoryRowBinding b = ItemAttachCategoryRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Category c = items.get(position);
            h.b.tvCatName.setText(c.getName() == null ? "" : c.getName());
            h.b.tvCatDesc.setText(c.getDescription() == null ? "" : c.getDescription());

            boolean attached =  selectedIds.contains(c.getId());
        }

        @Override
        public int getItemCount() { return items.size(); }
    }

}
