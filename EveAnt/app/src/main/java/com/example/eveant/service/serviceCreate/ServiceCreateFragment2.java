package com.example.eveant.service.serviceCreate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ServiceCreateFragment2 extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<String> selectedImagesBase64 = new ArrayList<>();
    private ImageView previewImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_service_create2, container, false);

        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        EditText description = view.findViewById(R.id.description);
        EditText specification = view.findViewById(R.id.specifications);
        previewImage = view.findViewById(R.id.previewImage);

        // Dugme za dodavanje slike
        Button uploadImageButton = view.findViewById(R.id.upload_image_button);
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment1);
        });

        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            Service service = viewModel.getService().getValue();

            service.setDescription(description.getText().toString());
            service.setSpecification(specification.getText().toString());
            service.setPhotos(selectedImagesBase64);

            viewModel.updateService(service);

            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment3);
        });

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    convertToBase64(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                convertToBase64(imageUri);
            }
        }
    }

    private void convertToBase64(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytes(inputStream);
            String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            selectedImagesBase64.add(base64Image);

            // Prikaži prvu sliku kao preview
            if (previewImage != null && selectedImagesBase64.size() == 1) {
                previewImage.setImageURI(imageUri);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}