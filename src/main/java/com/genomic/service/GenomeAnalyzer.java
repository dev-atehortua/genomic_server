package com.genomic.service;

import com.genomic.model.Disease;
import com.genomic.model.DetectionReport;
import com.genomic.util.FastaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenomeAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(GenomeAnalyzer.class);
    private static final double SIMILARITY_THRESHOLD = 0.8; // 80% similarity threshold
    private static final int MIN_MATCH_LENGTH = 50; // Minimum sequence length for matching
    
    private final DiseaseDatabase diseaseDatabase;
    
    public GenomeAnalyzer(DiseaseDatabase diseaseDatabase) {
        this.diseaseDatabase = diseaseDatabase;
    }
    
    public List<DetectionReport> analyzeGenome(String patientId, String fastaContent) {
        logger.info("Starting genome analysis for patient: {}", patientId);
        
        List<DetectionReport> detections = new ArrayList<>();
        
        if (!FastaValidator.isValidFasta(fastaContent)) {
            logger.error("Invalid FASTA format for patient: {}", patientId);
            return detections;
        }
        
        String patientSequence = FastaValidator.extractSequence(fastaContent);
        Map<String, Disease> diseases = diseaseDatabase.getAllDiseases();
        
        for (Disease disease : diseases.values()) {
            String diseaseSequence = FastaValidator.extractSequence(disease.getFastaSequence());
            
            double similarity = calculateSimilarity(patientSequence, diseaseSequence);
            
            if (similarity >= SIMILARITY_THRESHOLD) {
                String description = String.format(
                    "Sequence similarity detected: %.2f%% match with %s", 
                    similarity * 100, 
                    disease.getName()
                );
                
                DetectionReport report = new DetectionReport(
                    patientId, 
                    disease.getDiseaseId(), 
                    disease.getSeverity(), 
                    description
                );
                
                detections.add(report);
                
                logger.warn("Disease detected for patient {}: {} (Severity: {})", 
                    patientId, disease.getName(), disease.getSeverity());
            }
        }
        
        logger.info("Genome analysis completed for patient: {}. Detections: {}", 
            patientId, detections.size());
        
        return detections;
    }
    
    private double calculateSimilarity(String sequence1, String sequence2) {
        if (sequence1.length() < MIN_MATCH_LENGTH || sequence2.length() < MIN_MATCH_LENGTH) {
            return 0.0;
        }
        
        // Simple sliding window approach for sequence comparison
        int maxMatches = 0;
        int windowSize = Math.min(sequence1.length(), sequence2.length());
        
        for (int i = 0; i <= sequence1.length() - windowSize; i++) {
            String window1 = sequence1.substring(i, i + windowSize);
            
            for (int j = 0; j <= sequence2.length() - windowSize; j++) {
                String window2 = sequence2.substring(j, j + windowSize);
                
                int matches = countMatches(window1, window2);
                maxMatches = Math.max(maxMatches, matches);
            }
        }
        
        return (double) maxMatches / windowSize;
    }
    
    private int countMatches(String seq1, String seq2) {
        int matches = 0;
        int minLength = Math.min(seq1.length(), seq2.length());
        
        for (int i = 0; i < minLength; i++) {
            if (seq1.charAt(i) == seq2.charAt(i)) {
                matches++;
            }
        }
        
        return matches;
    }
}
