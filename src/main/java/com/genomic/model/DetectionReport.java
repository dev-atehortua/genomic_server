package com.genomic.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetectionReport {
    private String patientId;
    private String diseaseId;
    private int severity;
    private LocalDateTime detectionDateTime;
    private String description;
    
    public DetectionReport() {
        this.detectionDateTime = LocalDateTime.now();
    }
    
    public DetectionReport(String patientId, String diseaseId, int severity, String description) {
        this();
        this.patientId = patientId;
        this.diseaseId = diseaseId;
        this.severity = severity;
        this.description = description;
    }
    
    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getDiseaseId() { return diseaseId; }
    public void setDiseaseId(String diseaseId) { this.diseaseId = diseaseId; }
    
    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }
    
    public LocalDateTime getDetectionDateTime() { return detectionDateTime; }
    public void setDetectionDateTime(LocalDateTime detectionDateTime) { this.detectionDateTime = detectionDateTime; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String toCSVRow() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.join(",",
            patientId,
            diseaseId,
            String.valueOf(severity),
            detectionDateTime.format(formatter),
            "\"" + description + "\""
        );
    }
}
