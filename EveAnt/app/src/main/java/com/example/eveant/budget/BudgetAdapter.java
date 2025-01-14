package com.example.eveant.budget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.Category;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private ArrayList<Category> categoryList;
    private ArrayList<ItemDTO> itemDTOList;
    private Fragment fragment;
    private int budgetId=1;
    public BudgetAdapter(ArrayList<Category> categoryList, Fragment fragment) {
        this.categoryList = categoryList;
        this.fragment = fragment;
    }


    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.budget_item_category, parent, false);

        return  new BudgetViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, @SuppressLint("RecyclerView") int position){
        Category category = categoryList.get(position);
            holder.item_title.setText(category.getName());
            holder.add_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToItemList(category,position,holder.itemView.getContext());
                }
            });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {

        TextView item_title;
        Button add_category;

        public BudgetViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
                item_title = itemView.findViewById(R.id.item_title);
                add_category = itemView.findViewById(R.id.add_category);
        }

    }
    private void addToItemList(Category category, int position, Context context){
        /*ItemDTO itemDTO =new ItemDTO(category.getId(),0L,0,0L);
        RetrofitClient.budgetService.addItemToBudget(budgetId, itemDTO).enqueue(new Callback<ItemDTO>() {
            @Override
            public void onResponse(Call<ItemDTO> call, Response<ItemDTO> response) {
                if(response.isSuccessful()&& response.body()!=null){
                    Toast.makeText(context,"usplo je",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(context, " eco me tu sam Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ItemDTO> call, Throwable t) {
                Toast.makeText(context, "evo me tu sam Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }


}
