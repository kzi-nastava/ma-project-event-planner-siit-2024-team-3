package com.example.eveant.priceList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListAdapter extends RecyclerView.Adapter<PriceListAdapter.PriceListViewHolder>{
    private ArrayList<PriceListItem> priceList;
    private Fragment fragment;

    public PriceListAdapter(ArrayList<PriceListItem> priceList,Fragment fragment){
        this.priceList=priceList;
        this.fragment=fragment;
    }

    @NonNull
    @Override
    public PriceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.price_list_item,parent,false);
        return new PriceListViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PriceListItem priceListItem = priceList.get(position);

        holder.number.setText(String.valueOf(position + 1));
        holder.offerName.setText(priceListItem.getName());
        holder.price.setText(String.valueOf(priceListItem.getPrice()));
        holder.discount.setText(String.valueOf(priceListItem.getDiscount()));
        holder.discountedPrice.setText(String.valueOf(priceListItem.getPriceWithDiscount()));
        holder.editPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdatePopup(priceListItem,position, holder.itemView.getContext());
            }
        });
    }


    public int getItemCount(){
        return priceList.size();
    }


    public static class PriceListViewHolder extends RecyclerView.ViewHolder{
        TextView number,offerName,price,discount,discountedPrice;
        ImageButton editPrice;

        public PriceListViewHolder(@NonNull View itemView,int viewType){
            super(itemView);
            number=itemView.findViewById(R.id.number);
            offerName=itemView.findViewById(R.id.offerName);
            price=itemView.findViewById(R.id.price);
            discount=itemView.findViewById(R.id.discount);
            discountedPrice=itemView.findViewById(R.id.discountedPrice);
            editPrice=itemView.findViewById(R.id.editPrice);
        }
    }

    private void showUpdatePopup(PriceListItem priceListItem, int position, Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.edit_price_dialog_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        TextView dialog_title=dialogView.findViewById(R.id.dialog_title);
        EditText price_edit=dialogView.findViewById(R.id.price_edit);
        EditText discount_edit=dialogView.findViewById(R.id.discount_edit);
        Button button_save=dialogView.findViewById(R.id.button_save);

        dialog_title.setText(priceListItem.getName());
        price_edit.setText(String.valueOf(priceListItem.getPrice()));
        discount_edit.setText(String.valueOf(priceListItem.getDiscount()));

        AlertDialog dialog = builder.create();

        button_save.setOnClickListener(v -> {
            String priceText = price_edit.getText().toString().trim();
            String discountText = discount_edit.getText().toString().trim();

            if (priceText.isEmpty() || discountText.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceText);
                int discount = Integer.parseInt(discountText);

                if (price < 0) {
                    price_edit.setError("Price cannot be negative");
                    price_edit.requestFocus();
                    return;
                }

                if (discount < 0) {
                    discount_edit.setError("Discount cannot be negative");
                    discount_edit.requestFocus();
                    return;
                }

                if (discount > 100) {
                    discount_edit.setError("Discount cannot exceed 100%");
                    discount_edit.requestFocus();
                    return;
                }

                priceListItem.setPrice((long) price);
                priceListItem.setDiscount(discount);

                RetrofitClient.offerService.updateOfferPriceAndDiscount(priceListItem.getId(), priceListItem)
                        .enqueue(new Callback<PriceListItem>() {
                            @Override
                            public void onResponse(Call<PriceListItem> call, Response<PriceListItem> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    priceList.set(position, response.body());
                                    notifyItemChanged(position);
                                    Toast.makeText(context, "Price updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to update price", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<PriceListItem> call, Throwable t) {
                                Toast.makeText(context, "Error updating price: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();

    }

}
