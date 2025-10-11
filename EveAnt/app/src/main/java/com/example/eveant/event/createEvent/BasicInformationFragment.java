package com.example.eveant.event.createEvent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.EventActivity;
import com.example.eveant.event.EventCreationViewModel;
import com.example.eveant.event.EventStatus;
import com.example.eveant.eventType.EventType;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.security.AuthManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BasicInformationFragment extends Fragment {

    // UI (no Back/Next, no stepper)
    private TextView tvTitle, tvSubtitle, tvError, tvDate, tvTime;
    private EditText etName, etDescription, etCapacity;
    private EditText etCountry, etCity, etStreet, etHouseNo, etZip;
    private RadioGroup rgPrivacy;
    private RadioButton rbPrivate, rbPublic;
    private LinearLayout cardPhotos;
    private ProgressBar progress;

    // Data
    private final List<Uri> selectedPhotos = new ArrayList<>();
    private EventType eventType; // from VM
    private EventCreationViewModel vm;

    // Photo picker
    private final ActivityResultLauncher<String[]> photoPicker =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris == null || uris.isEmpty()) return;
                for (Uri u : uris) {
                    try {
                        requireContext().getContentResolver()
                                .takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                }
                selectedPhotos.clear();
                selectedPhotos.addAll(uris);
                Toast.makeText(requireContext(), selectedPhotos.size() + " photo(s) added", Toast.LENGTH_SHORT).show();
            });

    public BasicInformationFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basic_information, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind views
        tvTitle = v.findViewById(R.id.tvTitle);
        tvSubtitle = v.findViewById(R.id.tvSubtitle);
        tvError = v.findViewById(R.id.tvError);
        tvDate = v.findViewById(R.id.tvDate);
        tvTime = v.findViewById(R.id.tvTime);
        etName = v.findViewById(R.id.etName);
        etDescription = v.findViewById(R.id.etDescription);
        etCapacity = v.findViewById(R.id.etCapacity);
        etCountry = v.findViewById(R.id.etCountry);
        etCity = v.findViewById(R.id.etCity);
        etStreet = v.findViewById(R.id.etStreet);
        etHouseNo = v.findViewById(R.id.etHouseNo);
        etZip = v.findViewById(R.id.etZip);
        rgPrivacy = v.findViewById(R.id.rgPrivacy);
        rbPrivate = v.findViewById(R.id.rbPrivate);
        rbPublic = v.findViewById(R.id.rbPublic);
        cardPhotos = v.findViewById(R.id.cardPhotos);
        progress = v.findViewById(R.id.progress);

        // VM: get selected EventType from step 1
        vm = new ViewModelProvider(requireActivity()).get(EventCreationViewModel.class);
        vm.getSelectedType().observe(getViewLifecycleOwner(), et -> eventType = et);

        // Pickers
        tvDate.setOnClickListener(view -> openDatePicker());
        tvTime.setOnClickListener(view -> openTimePicker());
        cardPhotos.setOnClickListener(view -> photoPicker.launch(new String[]{"image/*"}));
    }

    /** Called by EventActivity when user taps Next. */
    public void handleNext() {
        if (!validate()) return;

        setLoading(true);

        // Build address
        Address address = new Address(
                s(etCountry), s(etCity), s(etStreet), s(etHouseNo), s(etZip)
        );

        // Privacy
        final EventStatus status = rbPublic.isChecked() ? EventStatus.PUBLIC : EventStatus.PRIVATE;

        // Combine date & time → ISO string
        final String isoDateTime;
        try {
            isoDateTime = toIsoDateTime(tvDate.getText().toString(), tvTime.getText().toString());
        } catch (ParseException e) {
            showError("Invalid date/time format.");
            setLoading(false);
            return;
        }

        // Organizer
        AuthManager auth = AuthManager.getInstance(requireContext());
        final String organizerEmail = auth.getEmail();

        // Off-UI thread: convert photos → base64, then call API
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> photosB64 = toBase64List(selectedPhotos, 1280, 80);

            CreateEventRequest body = new CreateEventRequest();
            body.organizer = organizerEmail;
            body.name = s(etName);
            body.description = s(etDescription);
            body.status = status;
            body.date = isoDateTime;
            body.photos = photosB64;
            body.address = address;
            body.maxAttendance = parseIntSafe(s(etCapacity), 1);
            body.eventType = eventType;

            RetrofitClient.eventService.createEvent(body)
                    .enqueue(new retrofit2.Callback<Event>() {
                        @Override
                        public void onResponse(@NonNull retrofit2.Call<Event> call,
                                               @NonNull retrofit2.Response<Event> resp) {
                            if (!isAdded()) return;
                            setLoading(false);

                            if (!resp.isSuccessful() || resp.body() == null) {
                                showError("Create failed: " + resp.code());
                                return;
                            }
                            Integer eventId = resp.body().getId();
                            if (eventId == null || eventId <= 0) {
                                showError("Server didn't return a valid event id.");
                                return;
                            }

                            // Save into VM for AGENDA
                            vm.setEventId(eventId);
                            try {
                                java.time.LocalDateTime ldt =
                                        null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    ldt = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vm.setEventDate(ldt.toLocalDate());
                                }
                            } catch (Exception ignore) {
                                // fallback: do nothing; Agenda will default to today (but server may reject)
                            }
                            // Tell Activity to move to AGENDA (single source of navigation)
                            if (requireActivity() instanceof EventActivity) {
                                ((EventActivity) requireActivity()).showStep(EventActivity.Step.AGENDA, true);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull retrofit2.Call<Event> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            setLoading(false);
                            showError("Network error: " + t.getMessage());
                        }
                    });
        });
    }

    // ==== Helpers ====

    private void setLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        if (tvError != null) {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private boolean validate() {
        if (TextUtils.isEmpty(s(etName))) {
            showError("Please enter the event name.");
            return false;
        }
        if (TextUtils.isEmpty(tvDate.getText())) {
            showError("Please select a date.");
            return false;
        }
        if (TextUtils.isEmpty(tvTime.getText())) {
            showError("Please select a time.");
            return false;
        }
        if (eventType == null) {
            showError("Please choose event type in previous step.");
            return false;
        }
        return true;
    }

    private void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> tvDate.setText(String.format(Locale.US, "%02d/%02d/%04d", m + 1, d, y)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void openTimePicker() {
        final Calendar c = Calendar.getInstance();
        TimePickerDialog dlg = new TimePickerDialog(requireContext(),
                (view, h, min) -> tvTime.setText(String.format(Locale.US, "%02d:%02d", h, min)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        dlg.show();
    }

    private static String s(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignored) { return def; }
    }

    // "MM/dd/yyyy HH:mm" -> ISO "yyyy-MM-dd'T'HH:mm:ss"
    private static String toIsoDateTime(String mmddyyyy, String hhmm) throws ParseException {
        SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
        SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date d = in.parse(mmddyyyy + " " + hhmm);
        return out.format(d);
    }

    // Downscale + JPEG compress + Base64
    private List<String> toBase64List(List<Uri> uris, int maxDim, int jpegQuality) {
        List<String> out = new ArrayList<>();
        for (Uri u : uris) {
            try (InputStream in = requireContext().getContentResolver().openInputStream(u)) {
                if (in == null) continue;
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, o);

                int scale = 1;
                int maxSide = Math.max(o.outWidth, o.outHeight);
                while (maxSide / (scale * 2) >= maxDim) scale *= 2;

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;

                try (InputStream in2 = requireContext().getContentResolver().openInputStream(u)) {
                    Bitmap bmp = BitmapFactory.decodeStream(in2, null, o2);
                    if (bmp == null) continue;

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality, bos);
                    String b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
                    out.add(b64);
                    bmp.recycle();
                }
            } catch (Exception ignored) {}
        }
        return out;
    }

    // Request model (align with your backend DTO)
    public static class CreateEventRequest {
        public String organizer;
        public String name;
        public String description;
        public EventStatus status;     // "PUBLIC" | "PRIVATE"
        public String date;       // ISO "yyyy-MM-dd'T'HH:mm:ss"
        public List<String> photos;
        public Address address;
        public int maxAttendance;
        public EventType eventType;
    }
}
