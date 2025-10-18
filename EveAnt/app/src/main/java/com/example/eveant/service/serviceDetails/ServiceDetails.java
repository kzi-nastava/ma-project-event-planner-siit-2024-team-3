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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;
import com.example.eveant.user.model.Provider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetails extends Fragment {

    private boolean isFavourite = false;
    private Provider prov;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_details, container, false);
        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);
        Service service = viewModel.getService().getValue();

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


        // UI elementi
        ImageView btnFavourite = view.findViewById(R.id.favourite);
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

        /*ImageView image1 = view.findViewById(R.id.image1);
        ImageView image2 = view.findViewById(R.id.image2);
        ImageView image3 = view.findViewById(R.id.image3);*/


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

            /*// Slike (ako imaš URL-ove)
            if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
                Glide.with(this).load(service.getPhotos().get(0)).into(image1);
                if (service.getPhotos().size() > 1)
                    Glide.with(this).load(service.getPhotos().get(1)).into(image2);
                if (service.getPhotos().size() > 2)
                    Glide.with(this).load(service.getPhotos().get(2)).into(image3);
            }
*/
            // 🔹 Status usluge
            if (!service.getStatus().equals(OfferStatus.AVAILABLE)) {
                btnReserve.setVisibility(View.GONE);
                unavailableText.setVisibility(View.VISIBLE);
            } else {
                btnReserve.setVisibility(View.VISIBLE);
                unavailableText.setVisibility(View.GONE);
            }
        }

        // Favoriti
        btnFavourite.setOnClickListener(v -> {
            isFavourite = !isFavourite;
            btnFavourite.setImageResource(isFavourite ? R.drawable.favourite : R.drawable.favourite);
            Toast.makeText(getActivity(),
                    isFavourite ? "Added to favourites" : "Removed from favourites",
                    Toast.LENGTH_SHORT).show();
            // pozovi backend da doda/ukloni
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
    private void showProviderInfoPopup(Provider provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_provider_info, null);

        TextView providerName = dialogView.findViewById(R.id.providerName);
        TextView providerInfo = dialogView.findViewById(R.id.providerInfo);
        EditText reportReason = dialogView.findViewById(R.id.reportReason);
        Button btnSubmitReport = dialogView.findViewById(R.id.btnSubmitReport);

        providerName.setText(provider.getFirstName()+" "+provider.getLastName());
        providerInfo.setText(provider.getAddress().toString());

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
    private void showCompanyDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_company_info, null);

        TextView companyName = dialogView.findViewById(R.id.companyName);
        TextView companyAddress = dialogView.findViewById(R.id.companyAddress);
        TextView companyPhone = dialogView.findViewById(R.id.companyPhone);

        companyName.setText(prov.getCompany().getCompanyName());
        companyAddress.setText(prov.getCompany().getAddress().toString());
        companyPhone.setText(prov.getCompany().getContact());

        builder.setView(dialogView);
        builder.setPositiveButton("Close", (d, w) -> d.dismiss());
        builder.show();
    }
}
