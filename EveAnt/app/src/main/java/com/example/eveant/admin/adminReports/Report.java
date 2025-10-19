package com.example.eveant.report;

import com.google.gson.annotations.SerializedName;

public class Report {
    @SerializedName("id")
    private Integer id;

    @SerializedName("reporterEmail")
    private String reporterEmail;

    @SerializedName("reportedEmail")
    private String reportedEmail;

    @SerializedName("reason")
    private String reason;

    @SerializedName("reportTimestamp")
    private String reportTimestamp;

    @SerializedName("status")
    private String status;

    // Constructors
    public Report() {}

    public Report(String reporterEmail, String reportedEmail, String reason) {
        this.reporterEmail = reporterEmail;
        this.reportedEmail = reportedEmail;
        this.reason = reason;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getReportedEmail() { return reportedEmail; }
    public void setReportedEmail(String reportedEmail) { this.reportedEmail = reportedEmail; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReportTimestamp() { return reportTimestamp; }
    public void setReportTimestamp(String reportTimestamp) { this.reportTimestamp = reportTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}