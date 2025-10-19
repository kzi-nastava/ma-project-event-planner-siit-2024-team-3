package com.example.eveant;

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

import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.CategoryStatus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private ArrayList<Category> categoryList;
    private Fragment fragment;

    public CategoryAdapter(ArrayList<Category> categoryList, Fragment fragment) {
        this.categoryList = categoryList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category, parent, false); // ✅ samo category.xml
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getName());
        holder.status.setText(category.getStatus().toString());
        holder.categoryDescription.setText(category.getDescription());

        holder.editButtoncategory.setOnClickListener(v -> showUpdatePopup(category, position, holder.itemView.getContext()));
        holder.deleteButtonCategory.setOnClickListener(v -> showDeleteDialog(category, position, holder.itemView.getContext()));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Category> newList) {
        this.categoryList.clear();
        this.categoryList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, status, categoryDescription;
        ImageButton editButtoncategory, deleteButtonCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            status = itemView.findViewById(R.id.status);
            categoryDescription = itemView.findViewById(R.id.categoryDescription);
            editButtoncategory = itemView.findViewById(R.id.editButtoncategory);
            deleteButtonCategory = itemView.findViewById(R.id.deleteButtonCategory);
        }
    }

    private void showUpdatePopup(Category category, int position, Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.category_dialog_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        TextView dialog_title=dialogView.findViewById(R.id.dialog_title);
        EditText input_name=dialogView.findViewById(R.id.input_name);
        EditText input_description=dialogView.findViewById(R.id.input_description);
        Button button_save=dialogView.findViewById(R.id.button_save);

        if(category!=null) {
            dialog_title.setText(category.getName());
            input_name.setText(category.getName());
            input_description.setText(category.getDescription());
        }
        AlertDialog dialog = builder.create();

        if(category!=null) {
            button_save.setOnClickListener(v -> {
                category.setStatus(CategoryStatus.APPROVED);
                category.setName(input_name.getText().toString());
                category.setDescription(input_description.getText().toString());

                RetrofitClient.categoryService.updateCategory(category.getId(), category).enqueue(new Callback<Category>() {
                    @Override
                    public void onResponse(Call<Category> call, Response<Category> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Category updatedCategory = response.body();
                            notifyItemChanged(position);
                            Toast.makeText(context, "Category approved successfully: " + updatedCategory.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to approve category", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Category> call, Throwable t) {
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });
        }else{
            button_save.setOnClickListener(v -> {
                category.setStatus(CategoryStatus.APPROVED);
                category.setName(input_name.getText().toString());
                category.setDescription(input_description.getText().toString());

                RetrofitClient.categoryService.createCategory(category).enqueue(new Callback<Category>() {
                    @Override
                    public void onResponse(Call<Category> call, Response<Category> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Category updatedCategory = response.body();
                            notifyItemChanged(position);
                            Toast.makeText(context, "Category added successfully: " + updatedCategory.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to add category", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Category> call, Throwable t) {
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });
        }
        dialog.show();

    }

    private void showDeleteDialog(Category category, int position, Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.delete_dialog_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonYes = dialogView.findViewById(R.id.button_yes);
        Button buttonNo = dialogView.findViewById(R.id.button_no);

        AlertDialog dialog = builder.create();

        buttonYes.setOnClickListener(v -> {
            RetrofitClient.categoryService.deleteCategory(category.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) { categoryList.remove(position)
                       ;
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Can not delete. Ofer is connected to this category", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


}
