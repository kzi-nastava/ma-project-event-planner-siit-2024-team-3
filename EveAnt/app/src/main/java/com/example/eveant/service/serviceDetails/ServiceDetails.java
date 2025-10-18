package com.example.eveant.service.serviceDetails;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;
import com.example.eveant.user.model.Provider;
import com.example.eveant.user.security.AuthManager;
import com.google.android.flexbox.FlexboxLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetails extends Fragment {

    private boolean isFavourite = false;
    private Provider prov;
    String username;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_details, container, false);
        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);
        Service service = viewModel.getService().getValue();

        AuthManager auth = AuthManager.getInstance(requireContext());
        username = auth.getEmail(); // ili email ako backend tako očekuje
        if(username==null){
            username="provider";
        }

        if (service == null) {
            Toast.makeText(getContext(), "Error: service data not found", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }

        RetrofitClient.userService.getProviderByServiceId(service.getId().longValue()).enqueue(new Callback<Provider>() {
            @Override
            public void onResponse(Call<Provider> call, Response<Provider> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prov = response.body();
                } else {
                    Log.e("ProviderFetch", "Provider not found for service ID " + service.getId());
                }
            }

            @Override
            public void onFailure(Call<Provider> call, Throwable t) {
                Log.e("ProviderFetch", "Network error fetching provider", t);
            }
        });

        ImageView btnFavourite = view.findViewById(R.id.favourite);

        // UI elementi
        Button btnReserve = view.findViewById(R.id.btn_reserve_service);
        TextView unavailableText = view.findViewById(R.id.service_unavailable);
        Button btnProviderInfo = view.findViewById(R.id.btn_provider_info);
        Button btnCompanyInfo = view.findViewById(R.id.btn_company_info);

        TextView serviceName = view.findViewById(R.id.serviceName);
        TextView serviceCategory = view.findViewById(R.id.serviceCategory);
        TextView description = view.findViewById(R.id.description);
        TextView specification = view.findViewById(R.id.specification);
        TextView newPrice = view.findViewById(R.id.newPrice);
        TextView oldPrice = view.findViewById(R.id.oldPrice);
        TextView discountBadge = view.findViewById(R.id.discountBadge);
        FlexboxLayout eventTypesContainer = view.findViewById(R.id.eventTypesContainer);
        HorizontalScrollView photosContainer = view.findViewById(R.id.photosContainer);
        LinearLayout photosLinear = view.findViewById(R.id.photosLinear);
        Button btnChatWithUs = view.findViewById(R.id.btn_chat_with_us);


        // 🔹 Učitavanje podataka
        if (service != null) {
            serviceName.setText(service.getName());
            serviceCategory.setText(service.getCategory().getName());
            description.setText(service.getDescription());
            specification.setText(service.getSpecification());
            Double price = service.getPrice() != null ? service.getPrice() : 0.0;
            Integer discount = service.getDiscount() != 0 ? service.getDiscount() : 0;

            double discounted = price - (price * discount / 100.0);
            newPrice.setText(String.format("%.2f$/hr", discounted));
            oldPrice.setText(String.format("%.2f$/hr", price));
            discountBadge.setText(discount + "%");

            // 🔹 Status usluge
            if (!service.getStatus().equals(OfferStatus.AVAILABLE)) {
                btnReserve.setVisibility(View.GONE);
                unavailableText.setVisibility(View.VISIBLE);
            } else {
                btnReserve.setVisibility(View.VISIBLE);
                unavailableText.setVisibility(View.GONE);
            }


            if (service.getEventTypes() != null && !service.getEventTypes().isEmpty()) {
                eventTypesContainer.removeAllViews();

                for (Object eventObj : service.getEventTypes()) {
                    com.example.eveant.eventType.EventType eventType = (com.example.eveant.eventType.EventType) eventObj;

                    TextView chip = new TextView(getContext());
                    chip.setText(eventType.getName());
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                    chip.setTextSize(13);
                    chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
                    chip.setBackgroundResource(R.drawable.bg_event_chip);

                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(8, 8, 8, 8);
                    chip.setLayoutParams(params);

                    eventTypesContainer.addView(chip);
                }
            } else {
                Log.d("ServiceDetails", "No event types available for this service");
            }

            if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
                photosContainer.setVisibility(View.VISIBLE);
                photosLinear.removeAllViews();

                for (String photoUrl : service.getPhotos()) {
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            300, // širina slike
                            200  // visina slike
                    );
                    params.setMargins(8, 0, 8, 0);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setBackgroundResource(R.drawable.rounded_corners_white);

                    Glide.with(this)
                            .load(photoUrl)
                            .into(imageView);

                    photosLinear.addView(imageView);
                }
            } else {
                // Ako nema slika, sakrij ceo container
                photosContainer.setVisibility(View.GONE);
            }
        }


        btnChatWithUs.setOnClickListener(v -> {
            if (prov == null || prov.getProfile() == null) {
                Toast.makeText(getContext(), "Provider info not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            String providerUsername = prov.getProfile().getUsername();
            String currentUsername = AuthManager.getInstance(requireContext()).getUsername();
            Log.d("trenutni korisnik",currentUsername);

            Bundle bundle = new Bundle();
            bundle.putString("username", providerUsername);

            NavController navController = NavHostFragment.findNavController(ServiceDetails.this);
            navController.navigate(R.id.action_serviceDetails_to_chatFragment, bundle);
        });

        RetrofitClient.userService.isOfferInFavourites(username, service.getId().longValue())
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            isFavourite = response.body();
                            btnFavourite.setImageResource(isFavourite ? R.drawable.favourite_add_icon : R.drawable.favourite);
                        } else {
                            Log.e("Favourite", "Failed to check favourites: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.e("Favourite", "Network error checking favourites", t);
                    }
                });
        // Favoriti
        btnFavourite.setOnClickListener(v -> {
            if (!isFavourite) {
                // ➕ Dodaj u omiljene
                RetrofitClient.userService.addOfferToFavourites(username, service.getId().longValue())
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    isFavourite = true;
                                    btnFavourite.setImageResource(R.drawable.favourite_add_icon);
                                    Toast.makeText(getContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 409) {
                                    Toast.makeText(getContext(), "Already in favourites", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to add to favourites", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(getContext(), "Network error adding favourite", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Ukloni iz omiljenih
                RetrofitClient.userService.removeOfferFromFavourites(username, service.getId().longValue())
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    isFavourite = false;
                                    btnFavourite.setImageResource(R.drawable.favourite);
                                    Toast.makeText(getContext(), "Removed from favourites", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 409) {
                                    Toast.makeText(getContext(), "Not in favourites", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to remove from favourites", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(getContext(), "Network error removing favourite", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Rezervacija
        btnReserve.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Service reserved successfully!", Toast.LENGTH_SHORT).show();
            // ovde možeš da dodaš Retrofit poziv za rezervaciju
        });

        // Provider info popup
        btnProviderInfo.setOnClickListener(v -> {
            if (prov != null) {
                showProviderInfoPopup(prov);
            } else {
                Toast.makeText(getContext(), "Provider info not available yet", Toast.LENGTH_SHORT).show();
            }
        });


        // Company info popup
        btnCompanyInfo.setOnClickListener(v -> showCompanyDialog(service));

        return view;
    }

    //  Popup za provider info + prijavu
    @SuppressLint("SetTextI18n")
    private void showProviderInfoPopup(Provider provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_provider_info, null);

        TextView providerName = dialogView.findViewById(R.id.providerName);
        TextView providerInfo = dialogView.findViewById(R.id.providerInfo);
        EditText reportReason = dialogView.findViewById(R.id.reportReason);
        Button btnSubmitReport = dialogView.findViewById(R.id.btnSubmitReport);

        providerName.setText("Name: "+provider.getFirstName()+" "+provider.getLastName());
        providerInfo.setText("Address: "+provider.getAddress().getStreet()+" "+provider.getAddress().getCity()+" "+ provider.getAddress().getCountry());

        btnSubmitReport.setOnClickListener(v -> {
            String reason = reportReason.getText().toString();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "Please enter reason", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Report submitted", Toast.LENGTH_SHORT).show();
                // TODO: Retrofit poziv za prijavu providera
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Close", (d, w) -> d.dismiss());
        builder.show();
    }


    //  Popup za company info
    @SuppressLint("SetTextI18n")
    private void showCompanyDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_company_info, null);

        TextView companyName = dialogView.findViewById(R.id.companyName);
        TextView companyAddress = dialogView.findViewById(R.id.companyAddress);
        TextView companyPhone = dialogView.findViewById(R.id.companyPhone);

        companyName.setText("Name: "+prov.getCompany().getCompanyName());
        companyAddress.setText("Addres: " + prov.getCompany().getAddress().getStreet()+" "+prov.getAddress().getCity());
        companyPhone.setText("Contact: "+prov.getCompany().getContact());

        builder.setView(dialogView);
        builder.setPositiveButton("Close", (d, w) -> d.dismiss());
        builder.show();
    }
}
