package com.genomic.util;

import java.util.regex.Pattern;

public class FastaValidator {
    private static final Pattern HEADER_PATTERN = Pattern.compile("^>[a-zA-Z0-9_]+$");
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^[ACGTN]+$");
    
    public static boolean isValidFasta(String fastaContent) {
        if (fastaContent == null || fastaContent.trim().isEmpty()) {
            return false;
        }
        
        String[] lines = fastaContent.trim().split("\n");
        
        if (lines.length < 2) {
            return false;
        }
        
        // Check header line
        if (!HEADER_PATTERN.matcher(lines[0].trim()).matches()) {
            return false;
        }
        
        // Check sequence lines
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim().toUpperCase();
            if (!SEQUENCE_PATTERN.matcher(line).matches()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static String extractSequence(String fastaContent) {
        if (!isValidFasta(fastaContent)) {
            throw new IllegalArgumentException("Invalid FASTA format");
        }
        
        String[] lines = fastaContent.trim().split("\n");
        StringBuilder sequence = new StringBuilder();
        
        for (int i = 1; i < lines.length; i++) {
            sequence.append(lines[i].trim().toUpperCase());
        }
        
        return sequence.toString();
    }
    
    public static String extractHeader(String fastaContent) {
        if (!isValidFasta(fastaContent)) {
            throw new IllegalArgumentException("Invalid FASTA format");
        }
        
        String[] lines = fastaContent.trim().split("\n");
        return lines[0].substring(1); // Remove '>' character
    }
}
