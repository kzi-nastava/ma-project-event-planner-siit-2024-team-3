package com.example.eveant.service.serviceDetails;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.communication.ChatActivity;
import com.example.eveant.review.Review;
import com.example.eveant.review.ReviewDTO;
import com.example.eveant.review.ReviewService;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.user.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetails extends Fragment {
    Service currentOffer;
    User currentUser;
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_details, container, false);
        long serviceId = getArguments().getLong("serviceId");


        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);
        Service serviceModel = viewModel.getService().getValue();

        TextView serviceName = view.findViewById(R.id.serviceName);
        TextView serviceCategory = view.findViewById(R.id.serviceCategory);
        TextView description = view.findViewById(R.id.description);
        TextView specification = view.findViewById(R.id.specification);
        TextView newPrice = view.findViewById(R.id.newPrice);
        TextView oldPrice = view.findViewById(R.id.oldPrice);
        TextView discountBadge = view.findViewById(R.id.discountBadge);

        RetrofitClient.serviceService.getService(serviceId).enqueue(new Callback<ServiceDTO>() {
            @Override
            public void onResponse(Call<ServiceDTO> call, Response<ServiceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("ovde sam i pogledaj me","pogledaj me");
                    ServiceDTO service = response.body();
                    currentOffer = null;

                    serviceName.setText(service.getName());
                    serviceCategory.setText(service.getCategory().getName());
                    description.setText(service.getDescription());
                    specification.setText(service.getSpecification());
                    oldPrice.setText(String.valueOf(service.getPrice()));
                    discountBadge.setText(String.valueOf(service.getDiscount()));
                    newPrice.setText(String.valueOf(
                            (int) (service.getPrice() - (service.getPrice() * service.getDiscount() / 100)))
                    );
                    /*TODO datviti da nema buy prodact kada je vec kupljen dugme vec samo da se vidi kakvo je*/
                }
            }

            @Override
            public void onFailure(Call<ServiceDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading service", Toast.LENGTH_SHORT).show();
            }
        });

         if (serviceModel != null) {
            currentOffer = serviceModel;

            serviceName.setText(serviceModel.getName());
            /*serviceCategory.setText(serviceModel.getCategory().getName());*/ // objekat Category
            description.setText(serviceModel.getDescription());
            specification.setText(serviceModel.getSpecification());
            /*oldPrice.setText(String.valueOf(serviceModel.getPrice()));
            discountBadge.setText(String.valueOf(serviceModel.getDiscount()));
            newPrice.setText(String.valueOf(
                    (int) (serviceModel.getPrice() - (serviceModel.getPrice() * serviceModel.getDiscount() / 100)))
            );*/

        } else {
            Toast.makeText(getContext(), "Service details not available", Toast.LENGTH_SHORT).show();
        }


        ImageView btnAddToFavourites = view.findViewById(R.id.favourite);
        Button btnBuyProduct = view.findViewById(R.id.btn_buy_product);
        Button btnProviderInfo = view.findViewById(R.id.btn_provider_info);
        Button btnCompanyInfo = view.findViewById(R.id.btn_company_info);


        btnAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReviewPopup();
            }
        });

        btnBuyProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*doda se u listu za kupljene i moze da prica sa pupom*/
                Toast.makeText(getActivity(), "Product Bought", Toast.LENGTH_SHORT).show();
            }
        });

        Service service = viewModel.getService().getValue();
        currentOffer=service;

        btnProviderInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: "+currentOffer);
                Log.d(TAG, "onClick: "+ currentUser);
                if ( currentOffer != null) {  /*currentUser != null &&*/
                    Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                    chatIntent.putExtra("userId", 1);/*currentUser.getId()*/
                    chatIntent.putExtra("providerId", 1);/*currentOffer.getProvider()*/
                    startActivity(chatIntent);
                } else {
                    Toast.makeText(getActivity(), "User or provider data is missing", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Company Info action
                Toast.makeText(getActivity(), "Company Info", Toast.LENGTH_SHORT).show();
            }
        });
       /* setupButtons(view);*/

        return view;
    }
    private void setupButtons(View view) {
        ImageView btnAddToFavourites = view.findViewById(R.id.favourite);
        Button btnBuyProduct = view.findViewById(R.id.btn_buy_product);
        Button btnProviderInfo = view.findViewById(R.id.btn_provider_info);
        Button btnCompanyInfo = view.findViewById(R.id.btn_company_info);

        btnAddToFavourites.setOnClickListener(v -> showReviewPopup());

        btnBuyProduct.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Product Bought", Toast.LENGTH_SHORT).show());

        btnProviderInfo.setOnClickListener(v -> {
            if (currentOffer != null) {
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("userId", 1); // TODO: zameni sa realnim ID
                chatIntent.putExtra("providerId", 1); // TODO: zameni sa provider id
                startActivity(chatIntent);
            } else {
                Toast.makeText(getActivity(), "Provider data is missing", Toast.LENGTH_SHORT).show();
            }
        });

        btnCompanyInfo.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Company Info", Toast.LENGTH_SHORT).show());
    }


    public void showReviewPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.review_popup, null);

        TextView name=popupView.findViewById(R.id.dialog_title);
        RatingBar ratingBar = popupView.findViewById(R.id.rating_bar);
        EditText commentText = popupView.findViewById(R.id.comment);
        Button btnSubmit = popupView.findViewById(R.id.button_save);

        name.setText(currentOffer.getName());


        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String comment =commentText.getText().toString();

            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setOfferId(currentOffer.getId());
            reviewDTO.setRating(rating);
            reviewDTO.setComment(comment);
            reviewDTO.setReviewerId(1);  /*currentUser.getId()*/

            sendReviewToServer(reviewDTO);
            dialog.dismiss();
        });


    }

    private void sendReviewToServer(ReviewDTO review) {
        RetrofitClient.reviewService.createReview(review).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(getActivity(), "Added to Favourites", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(getContext(), " nije uspelo ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                Toast.makeText(getContext(), " eco me tu sam Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}