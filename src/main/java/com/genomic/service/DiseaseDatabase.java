package com.genomic.service;

import com.genomic.model.Disease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DiseaseDatabase {
    private static final Logger logger = LoggerFactory.getLogger(DiseaseDatabase.class);
    private final Map<String, Disease> diseases;
    
    public DiseaseDatabase() {
        this.diseases = new HashMap<>();
    }
    
    public void loadDiseaseDatabase(String catalogPath, String diseaseFolderPath) throws IOException {
        logger.info("Loading disease database from catalog: {}", catalogPath);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(catalogPath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String diseaseId = parts[0].trim();
                    String name = parts[1].trim();
                    int severity = Integer.parseInt(parts[2].trim());
                    
                    // Load corresponding FASTA file
                    String fastaPath = diseaseFolderPath + "/" + diseaseId + ".fasta";
                    String fastaContent = loadFastaFile(fastaPath);
                    
                    Disease disease = new Disease(diseaseId, name, severity, fastaContent);
                    diseases.put(diseaseId, disease);
                    
                    logger.info("Loaded disease: {} - {}", diseaseId, name);
                }
            }
        }
        
        logger.info("Disease database loaded successfully. Total diseases: {}", diseases.size());
    }
    
    private String loadFastaFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("FASTA file not found: " + filePath);
        }
        
        return Files.readString(path);
    }
    
    public Map<String, Disease> getAllDiseases() {
        return new HashMap<>(diseases);
    }
    
    public Disease getDisease(String diseaseId) {
        return diseases.get(diseaseId);
    }
    
    public int getDiseaseCount() {
        return diseases.size();
    }
}
