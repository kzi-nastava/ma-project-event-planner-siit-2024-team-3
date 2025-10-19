package com.example.eveant.admin.adminReports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.report.Report; // Import the new Report class

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminReportsAdapter extends RecyclerView.Adapter<AdminReportsAdapter.ReportViewHolder> {

    private List<Report> reports;
    private final ReportActionListener suspendListener;
    private final ReportActionListener resolveListener;

    public interface ReportActionListener {
        void onAction(Report report);
    }

    public AdminReportsAdapter(List<Report> reports, ReportActionListener suspendListener, ReportActionListener resolveListener) {
        this.reports = reports;
        this.suspendListener = suspendListener;
        this.resolveListener = resolveListener;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private TextView reporterText;
        private TextView reportedText;
        private TextView reasonText;
        private TextView dateText;
        private Button suspendButton;
        private Button dismissButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reporterText = itemView.findViewById(R.id.reporterText);
            reportedText = itemView.findViewById(R.id.reportedText);
            reasonText = itemView.findViewById(R.id.reasonText);
            dateText = itemView.findViewById(R.id.dateText);
            suspendButton = itemView.findViewById(R.id.suspendButton);
            dismissButton = itemView.findViewById(R.id.dismissButton);
        }

        public void bind(Report report) {
            // Set reporter email
            reporterText.setText("Reporter: " + report.getReporterEmail());

            // Set reported email
            reportedText.setText("Reported: " + report.getReportedEmail());

            // Set reason
            reasonText.setText("Reason: " + report.getReason());

            // Set date
            String formattedDate = formatDate(report.getReportTimestamp());
            dateText.setText("Date: " + formattedDate);

            // Set button listeners
            suspendButton.setOnClickListener(v -> suspendListener.onAction(report));
            dismissButton.setOnClickListener(v -> resolveListener.onAction(report));
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateString != null ? dateString : "Unknown date";
            }
        }
    }
}