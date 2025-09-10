package com.genomic.model;

public class Disease {
    private String diseaseId;
    private String name;
    private int severity; // 1-10
    private String fastaSequence;
    
    public Disease() {}
    
    public Disease(String diseaseId, String name, int severity, String fastaSequence) {
        this.diseaseId = diseaseId;
        this.name = name;
        this.severity = severity;
        this.fastaSequence = fastaSequence;
    }
    
    // Getters and Setters
    public String getDiseaseId() { return diseaseId; }
    public void setDiseaseId(String diseaseId) { this.diseaseId = diseaseId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }
    
    public String getFastaSequence() { return fastaSequence; }
    public void setFastaSequence(String fastaSequence) { this.fastaSequence = fastaSequence; }
    
    @Override
    public String toString() {
        return "Disease{" +
                "diseaseId='" + diseaseId + '\'' +
                ", name='" + name + '\'' +
                ", severity=" + severity +
                '}';
    }
}
