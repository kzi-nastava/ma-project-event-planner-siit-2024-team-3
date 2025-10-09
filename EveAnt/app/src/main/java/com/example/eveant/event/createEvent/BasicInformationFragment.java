package com.example.eveant.event.createEvent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.eventType.EventType;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.security.AuthManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Step 2: Basic event information
 */
public class BasicInformationFragment extends Fragment {

    // Steps + chrome
    private TextView step1, step2, step3, step4, step5, tvTitle, tvSubtitle, tvError;
    private Button btnBack, btnNext;

    // Event information
    private EditText etName, etDescription, etCapacity;

    // Event details
    private EditText etCountry, etCity, etStreet, etHouseNo, etZip;
    private TextView tvDate, tvTime;

    // Privacy
    private RadioGroup rgPrivacy;
    private RadioButton rbPrivate, rbPublic;

    // Photos
    private LinearLayout cardPhotos;
    private final List<Uri> selectedPhotos = new ArrayList<>();

    // From Step 1
    private EventType eventType;

    private final ActivityResultLauncher<String[]> photoPicker =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    // Persist read permission for future use
                    for (Uri u : uris) {
                        try {
                            requireContext().getContentResolver()
                                    .takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) { }
                    }
                    selectedPhotos.clear();
                    selectedPhotos.addAll(uris);
                    Toast.makeText(requireContext(), selectedPhotos.size() + " photo(s) added", Toast.LENGTH_SHORT).show();
                }
            });

    public BasicInformationFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basic_information, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Bundle b = getArguments();
        if (b != null) {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                eventType = b.getParcelable("eventType", EventType.class);
            } else {
                eventType = b.getParcelable("eventType");
            }
        }

        // Steps header + chrome
        step1 = v.findViewById(R.id.step1);
        step2 = v.findViewById(R.id.step2);
        step3 = v.findViewById(R.id.step3);
        step4 = v.findViewById(R.id.step4);
        step5 = v.findViewById(R.id.step5);
        tvTitle = v.findViewById(R.id.tvTitle);
        tvSubtitle = v.findViewById(R.id.tvSubtitle);
        tvError = v.findViewById(R.id.tvError);
        btnBack = v.findViewById(R.id.btnBack);
        btnNext = v.findViewById(R.id.btnNext);

        forceBlack(step1, step2, step3, step4, step5, tvTitle, tvSubtitle);
        highlightStep(2);


        // Inputs
        etName = v.findViewById(R.id.etName);
        etDescription = v.findViewById(R.id.etDescription);
        etCapacity = v.findViewById(R.id.etCapacity);

        etCountry = v.findViewById(R.id.etCountry);
        etCity = v.findViewById(R.id.etCity);
        etStreet = v.findViewById(R.id.etStreet);
        etHouseNo = v.findViewById(R.id.etHouseNo);
        etZip = v.findViewById(R.id.etZip);
        tvDate = v.findViewById(R.id.tvDate);
        tvTime = v.findViewById(R.id.tvTime);

        rgPrivacy = v.findViewById(R.id.rgPrivacy);
        rbPrivate = v.findViewById(R.id.rbPrivate);
        rbPublic = v.findViewById(R.id.rbPublic);

        cardPhotos = v.findViewById(R.id.cardPhotos);

        // Pickers
        tvDate.setOnClickListener(view -> openDatePicker());
        tvTime.setOnClickListener(view -> openTimePicker());

        // Photos
        cardPhotos.setOnClickListener(view ->
                photoPicker.launch(new String[]{"image/*"})
        );

        // Nav
        btnBack.setOnClickListener(view -> requireActivity().onBackPressed());
        btnNext.setOnClickListener(view -> onNextClicked());
    }

    private void onNextClicked() {
        if (!validate()) return;

        btnNext.setEnabled(false);
        tvError.setVisibility(View.GONE);

        // Build address (use your actual constructor/setters)
        Address address = new Address(
                s(etCountry),
                s(etCity),
                s(etStreet),
                s(etHouseNo),
                s(etZip)
        );

        // Privacy → EventStatus
        final String status = rbPublic.isChecked() ? "PUBLIC" : "PRIVATE";

        // Combine date & time into ISO-8601 string (so Spring parses LocalDateTime)
        final String isoDateTime;
        try {
            isoDateTime = toIsoDateTime(tvDate.getText().toString(), tvTime.getText().toString());
        } catch (ParseException e) {
            showError("Invalid date/time format.");
            btnNext.setEnabled(true);
            return;
        }

        // Organizer
        AuthManager auth = AuthManager.getInstance(requireContext());
        final String organizerEmail = auth.getEmail();

        // Convert photos → base64 strings OFF the UI thread, then call API
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> photosB64 = toBase64List(selectedPhotos, /*maxDim*/1280, /*jpegQuality*/80);

            // Build request body that mirrors CreateEventDTO (date as ISO string)
            CreateEventRequest body = new CreateEventRequest();
            body.organizer = organizerEmail;
            body.name = s(etName);
            body.description = s(etDescription);
            body.status = status;
            body.date = isoDateTime;                // <-- String, ISO, backend parses to LocalDateTime
            body.photos = photosB64;               // <-- List<String>
            body.address = address;
            body.maxAttendance = parseIntSafe(s(etCapacity), 1);
            body.eventType = eventType;


            requireActivity().runOnUiThread(() -> {
                RetrofitClient.eventService.createEvent(body)
                        .enqueue(new retrofit2.Callback<Event>() {
                            @Override
                            public void onResponse(@NonNull retrofit2.Call<Event> call,
                                                   @NonNull retrofit2.Response<Event> resp) {
                                btnNext.setEnabled(true);
                                if (!resp.isSuccessful() || resp.body() == null) {
                                    showError("Create failed: " + resp.code());
                                    return;
                                }
                                Event created = resp.body();
                                Toast.makeText(requireContext(),
                                        "Event created" + (created.getName() != null ? (": " + created.getName()) : ""),
                                        Toast.LENGTH_SHORT).show();

                                Log.e("BasicInfoFragment", "Created event message ");

                            }

                            @Override
                            public void onFailure(@NonNull retrofit2.Call<Event> call,
                                                  @NonNull Throwable t) {
                                btnNext.setEnabled(true);
                                showError("Network error: " + t.getMessage());
                                Log.e("BasicInfoFragment", "Failed creation event message ");
                            }
                        });
            });

        });
    }

    // === Helpers ===

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

    private boolean validate() {
        tvError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(s(etName))) {
            showError("Please enter the event name.");
            return false;
        }
        if (TextUtils.isEmpty(tvDate.getText()) || tvDate.getText().toString().startsWith("mm/")) {
            showError("Please select a date.");
            return false;
        }
        if (TextUtils.isEmpty(tvTime.getText()) || tvTime.getText().toString().startsWith("--")) {
            showError("Please select a time.");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void highlightStep(int stepIndex) {
        TextView[] arr = new TextView[]{step1, step2, step3, step4, step5};
        for (int i = 0; i < arr.length; i++) {
            arr[i].setTypeface(null, (i + 1 == stepIndex) ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void forceBlack(TextView... tviews) {
        for (TextView t : tviews) if (t != null) t.setTextColor(0xFF000000);
    }

    private static String s(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ignored) { return def; }
    }

    // Convert "MM/dd/yyyy" + "HH:mm" -> "yyyy-MM-dd'T'HH:mm:ss"
    private static String toIsoDateTime(String mmddyyyy, String hhmm) throws ParseException {
        SimpleDateFormat in = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
        SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date d = in.parse(mmddyyyy + " " + hhmm);
        return out.format(d);
    }

    // Downscale + JPEG compress + Base64 (keep strings reasonably small)
    private List<String> toBase64List(List<Uri> uris, int maxDim, int jpegQuality) {
        List<String> out = new ArrayList<>();
        for (Uri u : uris) {
            try (InputStream in = requireContext().getContentResolver().openInputStream(u)) {
                if (in == null) continue;

                // First decode bounds
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, o);

                int scale = 1;
                int maxSide = Math.max(o.outWidth, o.outHeight);
                while (maxSide / (scale * 2) >= maxDim) scale *= 2;

                // Decode with inSampleSize
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
            } catch (Exception ignored) { }
        }
        return out;
    }

    // ===== Client-side request models (match your CreateEventDTO) =====
    public static class CreateEventRequest {
        public String organizer;
        public String name;
        public String description;
        public String status;           // "PUBLIC" | "PRIVATE"  (EventStatus)
        public String date;             // ISO "yyyy-MM-dd'T'HH:mm:ss" so backend LocalDateTime parses
        public List<String> photos;     // base64 strings
        public Address address;
        public int maxAttendance;
        public EventType eventType;
    }


}
