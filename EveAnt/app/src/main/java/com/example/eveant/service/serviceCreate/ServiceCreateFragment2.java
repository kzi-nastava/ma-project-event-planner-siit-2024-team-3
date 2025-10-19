package com.example.eveant.service.serviceCreate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ServiceCreateFragment2 extends Fragment {

    private ServiceCreateViewModel viewModel;
    private EditText descriptionInput, specificationInput;
    private Button uploadImagesButton, nextButton, previousButton;
    private LinearLayout imagePreviewContainer;
    private List<String> imageBase64List = new ArrayList<>();

    // Launcher za izbor slika iz galerije
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            handleImageUri(imageUri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        handleImageUri(imageUri);
                    }
                    refreshImagePreview();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create2, container, false);

        // UI init
        descriptionInput = view.findViewById(R.id.description);
        specificationInput = view.findViewById(R.id.specifications);
        uploadImagesButton = view.findViewById(R.id.upload_images_button);
        imagePreviewContainer = view.findViewById(R.id.image_preview_container);
        nextButton = view.findViewById(R.id.next_button);
        previousButton = view.findViewById(R.id.previous_button);

        viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        // Ako korisnik ide "nazad", popuni prethodno unete podatke (opciono)
        boolean isEditMode = Boolean.TRUE.equals(viewModel.getEditMode().getValue());
        Service existingService = viewModel.getService().getValue();

        if (existingService != null) {
            //  Opis i specifikacija
            if (existingService.getDescription() != null)
                descriptionInput.setText(existingService.getDescription());
            if (existingService.getSpecification() != null)
                specificationInput.setText(existingService.getSpecification());

            //  Slike
            if (existingService.getPhotos() != null && !existingService.getPhotos().isEmpty()) {
                imageBase64List = new ArrayList<>(existingService.getPhotos());
                refreshImagePreview();
            }
        }


        previousButton.setOnClickListener(v -> {
            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment1);
        });

        uploadImagesButton.setOnClickListener(v -> openImagePicker());

        nextButton.setOnClickListener(v -> handleNext());

        return view;
    }

    private void handleNext() {
        String description = descriptionInput.getText().toString().trim();
        String specification = specificationInput.getText().toString().trim();

        //  Validacija
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), "Description is required", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Uzmi već postojeći servis iz ViewModel-a
        Service service = viewModel.getService().getValue();
        if (service == null) {
            service = new Service(); // fallback, ali ovo se skoro nikad ne desi
        }

        service.setDescription(description);
        service.setSpecification(specification);

        // Ako korisnik nije dodao/obrisao slike, zadrži stare
        if (imageBase64List != null && !imageBase64List.isEmpty()) {
            service.setPhotos(imageBase64List);
        }

        viewModel.updateService(service);

        NavController navController = ((MainActivity) getActivity()).getNavController();
        navController.navigate(R.id.serviceCreateFragment3);
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select images"));
    }

    private void handleImageUri(Uri uri) {
        try {
            // 🔥 Opciono: ako backend očekuje base64, ovde konvertuj sliku u base64 string
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            String base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
            imageBase64List.add(base64);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to read image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshImagePreview() {
        imagePreviewContainer.removeAllViews();
        for (int i = 0; i < imageBase64List.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            byte[] decoded = android.util.Base64.decode(imageBase64List.get(i), android.util.Base64.DEFAULT);
            imageView.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length));

            int finalI = i;
            imageView.setOnLongClickListener(v -> {
                imageBase64List.remove(finalI);
                refreshImagePreview();
                return true;
            });

            imagePreviewContainer.addView(imageView);
        }
    }
}
