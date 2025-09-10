package com.genomic.service;

import com.genomic.model.Patient;
import com.genomic.model.DetectionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private final Map<String, Patient> patients;
    private final Map<String, String> documentIdToPatientId;
    private final String csvFilePath;
    private final String reportFilePath;
    
    public PatientService(String csvFilePath, String reportFilePath) {
        this.patients = new ConcurrentHashMap<>();
        this.documentIdToPatientId = new ConcurrentHashMap<>();
        this.csvFilePath = csvFilePath;
        this.reportFilePath = reportFilePath;
        
        initializeCSVFile();
        initializeReportFile();
        loadPatientsFromCSV();
    }
    
    private void initializeCSVFile() {
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath))) {
                writer.println("patient_id,full_name,document_id,age,sex,contact_email,registration_date,clinical_notes,checksum_fasta,file_size_bytes,active");
                logger.info("Created new CSV file: {}", csvFilePath);
            } catch (IOException e) {
                logger.error("Error creating CSV file: {}", e.getMessage());
            }
        }
    }
    
    private void initializeReportFile() {
        File reportFile = new File(reportFilePath);
        if (!reportFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFilePath))) {
                writer.println("patient_id,disease_id,severity,detection_datetime,description");
                logger.info("Created new report file: {}", reportFilePath);
            } catch (IOException e) {
                logger.error("Error creating report file: {}", e.getMessage());
            }
        }
    }
    
    private void loadPatientsFromCSV() {
        // Implementation to load existing patients from CSV
        // This would be implemented to restore state on server restart
        logger.info("Loading patients from CSV file: {}", csvFilePath);
    }
    
    public synchronized String createPatient(Patient patient) throws Exception {
        // Check for duplicate document ID
        if (documentIdToPatientId.containsKey(patient.getDocumentId())) {
            throw new IllegalArgumentException("Patient with document ID already exists: " + patient.getDocumentId());
        }
        
        // Generate unique patient ID
        String patientId = "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        patient.setPatientId(patientId);
        
        // Store patient
        patients.put(patientId, patient);
        documentIdToPatientId.put(patient.getDocumentId(), patientId);
        
        // Save to CSV
        savePatientToCSV(patient);
        
        logger.info("Created new patient: {} ({})", patientId, patient.getFullName());
        return patientId;
    }
    
    public Patient getPatient(String patientId) {
        return patients.get(patientId);
    }
    
    public synchronized void updatePatient(Patient updatedPatient) throws Exception {
        String patientId = updatedPatient.getPatientId();
        Patient existingPatient = patients.get(patientId);
        
        if (existingPatient == null) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        
        // Update the patient
        patients.put(patientId, updatedPatient);
        
        // Update CSV (in a real implementation, you'd rewrite the entire file or use a database)
        savePatientToCSV(updatedPatient);
        
        logger.info("Updated patient: {} ({})", patientId, updatedPatient.getFullName());
    }
    
    public synchronized void deletePatient(String patientId) throws Exception {
        Patient patient = patients.get(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        
        // Logical delete
        patient.setActive(false);
        patients.put(patientId, patient);
        
        // Update CSV
        savePatientToCSV(patient);
        
        logger.info("Logically deleted patient: {} ({})", patientId, patient.getFullName());
    }
    
    private void savePatientToCSV(Patient patient) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath, true))) {
            writer.println(patient.toCSVRow());
        } catch (IOException e) {
            logger.error("Error saving patient to CSV: {}", e.getMessage());
        }
    }
    
    public void saveDetectionReport(DetectionReport report) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFilePath, true))) {
            writer.println(report.toCSVRow());
            logger.info("Saved detection report for patient: {}", report.getPatientId());
        } catch (IOException e) {
            logger.error("Error saving detection report: {}", e.getMessage());
        }
    }
    
    public void saveDetectionReports(List<DetectionReport> reports) {
        for (DetectionReport report : reports) {
            saveDetectionReport(report);
        }
    }
}
