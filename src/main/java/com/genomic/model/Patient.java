package com.genomic.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Patient {
    private String patientId;
    private String fullName;
    private String documentId;
    private int age;
    private char sex; // 'M' or 'F'
    private String contactEmail;
    private LocalDateTime registrationDate;
    private String clinicalNotes;
    private String checksumFasta;
    private long fileSizeBytes;
    private boolean active;
    
    public Patient() {
        this.registrationDate = LocalDateTime.now();
        this.active = true;
    }
    
    public Patient(String fullName, String documentId, int age, char sex, 
                   String contactEmail, String clinicalNotes, 
                   String checksumFasta, long fileSizeBytes) {
        this();
        this.fullName = fullName;
        this.documentId = documentId;
        this.age = age;
        this.sex = sex;
        this.contactEmail = contactEmail;
        this.clinicalNotes = clinicalNotes;
        this.checksumFasta = checksumFasta;
        this.fileSizeBytes = fileSizeBytes;
    }
    
    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public char getSex() { return sex; }
    public void setSex(char sex) { this.sex = sex; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }
    
    public String getChecksumFasta() { return checksumFasta; }
    public void setChecksumFasta(String checksumFasta) { this.checksumFasta = checksumFasta; }
    
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String toCSVRow() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.join(",",
            patientId != null ? patientId : "",
            "\"" + fullName + "\"",
            documentId,
            String.valueOf(age),
            String.valueOf(sex),
            contactEmail,
            registrationDate.format(formatter),
            "\"" + clinicalNotes + "\"",
            checksumFasta,
            String.valueOf(fileSizeBytes),
            String.valueOf(active)
        );
    }
    
    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                ", contactEmail='" + contactEmail + '\'' +
                ", registrationDate=" + registrationDate +
                ", active=" + active +
                '}';
    }
}
