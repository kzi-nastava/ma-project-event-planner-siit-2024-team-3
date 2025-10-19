package com.example.eveant.budget;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrganizerEventsFragment extends Fragment {

    private RecyclerView rvBudgets;
    private BudgetAdapter adapter;

    public OrganizerEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rvBudgets = v.findViewById(R.id.rvBudgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadBudgets();
    }

    private void loadBudgets() {
        RetrofitClient.budgetService.getAllBudgets().enqueue(new Callback<List<Budget>>() {
            @Override
            public void onResponse(Call<List<Budget>> call, Response<List<Budget>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new BudgetAdapter(response.body(), budget -> openBudget(budget));
                    rvBudgets.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(), "Failed to load budgets", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Budget>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openBudget(Budget budget) {
        Bundle args = new Bundle();
        args.putInt("budgetId", budget.getId());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_organizerEventsFragment_to_budgetFragment, args);
    }


}
