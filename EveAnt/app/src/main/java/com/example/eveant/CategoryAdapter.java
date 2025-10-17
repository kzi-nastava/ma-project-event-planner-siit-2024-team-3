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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private ArrayList<Category> categoryList;
    private Fragment fragment;
    public CategoryAdapter(ArrayList<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public CategoryAdapter(ArrayList<Category> categoryList, Fragment fragment) {
        this.categoryList = categoryList;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        Category category = categoryList.get(position);
        return CategoryStatus.SUGGESTED.equals(category.getStatus()) ? 0 : 1;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.suggested_category, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category, parent, false);
        }
        return new CategoryViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getName());
        if(holder.fabAddCategory!=null) {
            holder.fabAddCategory.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(), "Add Category clicked", Toast.LENGTH_SHORT).show();
            });
        }
        if (getItemViewType(position) == 0) {
            holder.status.setText(category.getStatus().toString());
            holder.addCategoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUpdatePopup(category, position, holder.itemView.getContext());
                }
            });
        } else {
            holder.categoryDescription.setText(category.getDescription());
            holder.editButtoncategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUpdatePopup(category, position, holder.itemView.getContext());
                }
            });
            holder.deleteButtonCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(category, position, holder.itemView.getContext());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName,status,categoryDescription;
        ImageButton addCategoryButton,editButtoncategory,deleteButtonCategory;
        ImageButton fabAddCategory;

        public CategoryViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            fabAddCategory = itemView.findViewById(R.id.fabAddCategory);

            if (viewType == 0) { // Suggested category
                addCategoryButton = itemView.findViewById(R.id.addCategoryButton);
                categoryName = itemView.findViewById(R.id.categoryName);
                status = itemView.findViewById(R.id.status);
            } else { // Regular category
                categoryName = itemView.findViewById(R.id.categoryName);
                status = itemView.findViewById(R.id.status);
                categoryDescription=itemView.findViewById(R.id.categoryDescription);
                editButtoncategory = itemView.findViewById(R.id.editButtoncategory);
                deleteButtonCategory = itemView.findViewById(R.id.deleteButtonCategory);

            }

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
                        Toast.makeText(context, "Failed to delete category", Toast.LENGTH_SHORT).show();
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
