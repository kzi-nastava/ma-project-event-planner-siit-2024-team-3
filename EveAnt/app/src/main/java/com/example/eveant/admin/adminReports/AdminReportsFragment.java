package com.example.eveant.admin.adminReports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.report.Report; // Import the new Report class

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private AdminReportsAdapter adapter;
    private List<Report> reports = new ArrayList<>(); // Use Report class

    public static AdminReportsFragment newInstance() {
        return new AdminReportsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.reportsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminReportsAdapter(reports,
                new AdminReportsAdapter.ReportActionListener() {
                    @Override
                    public void onAction(Report report) {
                        suspendUser(report);
                    }
                },
                new AdminReportsAdapter.ReportActionListener() {
                    @Override
                    public void onAction(Report report) {
                        resolveReport(report.getId());
                    }
                }
        );        recyclerView.setAdapter(adapter);

        loadReports();
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        RetrofitClient.reportService.getReports().enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    reports.clear();
                    reports.addAll(response.body());
                    adapter.setReports(reports);
                    updateEmptyState();
                } else {
                    Toast.makeText(getContext(), "Failed to load reports: " + response.code(), Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading reports: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void suspendUser(Report report) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Suspend User")
                .setMessage("Suspend " + report.getReportedEmail() + " for 3 days?")
                .setPositiveButton("Suspend", (dialog, which) -> {
                    performSuspension(report);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSuspension(Report report) {
        RetrofitClient.reportService.suspendUser(report.getReportedEmail()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "User suspended successfully", Toast.LENGTH_SHORT).show();
                    resolveReport(report.getId());
                } else {
                    Toast.makeText(getContext(), "Failed to suspend user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error suspending user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resolveReport(int reportId) {
        RetrofitClient.reportService.deleteReport(reportId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    removeReportFromList(reportId);
                    Toast.makeText(getContext(), "Report resolved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to resolve report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error resolving report: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeReportFromList(int reportId) {
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getId() == reportId) {
                reports.remove(i);
                adapter.setReports(reports);
                updateEmptyState();
                break;
            }
        }
    }

    private void updateEmptyState() {
        if (reports.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}