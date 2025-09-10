package com.genomic.protocol;

public class GenomicProtocol {
    // Command types
    public static final String CREATE_PATIENT = "CREATE_PATIENT";
    public static final String GET_PATIENT = "GET_PATIENT";
    public static final String UPDATE_PATIENT = "UPDATE_PATIENT";
    public static final String DELETE_PATIENT = "DELETE_PATIENT";
    public static final String SEND_FASTA = "SEND_FASTA";
    
    // Response types
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String DISEASE_DETECTED = "DISEASE_DETECTED";
    public static final String NO_DISEASE = "NO_DISEASE";
    
    // Separators
    public static final String FIELD_SEPARATOR = "|";
    public static final String END_OF_MESSAGE = "\n";
    public static final String END_OF_TRANSMISSION = "EOT";
    
    // Protocol format:
    // COMMAND|field1|field2|field3|...|EOT
    
    public static String createCommand(String command, String... fields) {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        
        for (String field : fields) {
            sb.append(FIELD_SEPARATOR);
            sb.append(field != null ? field.replace(FIELD_SEPARATOR, "\\|") : "");
        }
        
        sb.append(FIELD_SEPARATOR);
        sb.append(END_OF_TRANSMISSION);
        
        return sb.toString();
    }
    
    public static String[] parseCommand(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid message format: empty message");
        }
        
        // Remove trailing newline if present
        String cleanMessage = message.trim();
        
        if (!cleanMessage.endsWith(END_OF_TRANSMISSION)) {
            throw new IllegalArgumentException("Invalid message format: missing EOT");
        }
        
        // Remove EOT marker
        cleanMessage = cleanMessage.substring(0, cleanMessage.length() - END_OF_TRANSMISSION.length());
        
        // Split and unescape field separators
        String[] parts = cleanMessage.split("\\" + FIELD_SEPARATOR);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace("\\|", FIELD_SEPARATOR);
        }
        
        return parts;
    }
    
    public static String createResponse(String responseType, String... data) {
        return createCommand(responseType, data);
    }
}
