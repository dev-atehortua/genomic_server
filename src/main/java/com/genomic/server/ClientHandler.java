package com.genomic.server;

import com.genomic.model.Patient;
import com.genomic.model.DetectionReport;
import com.genomic.protocol.GenomicProtocol;
import com.genomic.service.GenomeAnalyzer;
import com.genomic.service.PatientService;
import com.genomic.util.ChecksumUtil;
import com.genomic.util.FastaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final SSLSocket clientSocket;
    private final PatientService patientService;
    private final GenomeAnalyzer genomeAnalyzer;
    private BufferedReader reader;
    private PrintWriter writer;
    
    public ClientHandler(SSLSocket clientSocket, PatientService patientService, GenomeAnalyzer genomeAnalyzer) {
        this.clientSocket = clientSocket;
        this.patientService = patientService;
        this.genomeAnalyzer = genomeAnalyzer;
    }
    
    @Override
    public void run() {
        try {
            setupStreams();
            handleClient();
        } catch (Exception e) {
            logger.error("Error handling client: {}", e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }
    
    private void setupStreams() throws IOException {
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        logger.info("Client connected from: {}", clientSocket.getRemoteSocketAddress());
    }
    
    private void handleClient() throws IOException {
        String message;
        while ((message = reader.readLine()) != null) {
            try {
                logger.debug("Raw message received: '{}'", message);
                processMessage(message);
            } catch (Exception e) {
                logger.error("Error processing message: {}", e.getMessage());
                sendErrorResponse(e.getMessage());
            }
        }
    }
    
    private void processMessage(String message) throws Exception {
        logger.debug("Received message: {}", message);
        
        String[] parts = GenomicProtocol.parseCommand(message);
        String command = parts[0];
        
        switch (command) {
            case GenomicProtocol.CREATE_PATIENT:
                handleCreatePatient(parts);
                break;
            case GenomicProtocol.GET_PATIENT:
                handleGetPatient(parts);
                break;
            case GenomicProtocol.UPDATE_PATIENT:
                handleUpdatePatient(parts);
                break;
            case GenomicProtocol.DELETE_PATIENT:
                handleDeletePatient(parts);
                break;
            case GenomicProtocol.SEND_FASTA:
                handleSendFasta(parts);
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + command);
        }
    }
    
    private void handleCreatePatient(String[] parts) throws Exception {
        if (parts.length < 8) {
            throw new IllegalArgumentException("Insufficient parameters for CREATE_PATIENT");
        }
        
        Patient patient = new Patient();
        patient.setFullName(parts[1]);
        patient.setDocumentId(parts[2]);
        patient.setAge(Integer.parseInt(parts[3]));
        patient.setSex(parts[4].charAt(0));
        patient.setContactEmail(parts[5]);
        patient.setClinicalNotes(parts[6]);
        patient.setChecksumFasta(parts[7]);
        patient.setFileSizeBytes(Long.parseLong(parts[8]));
        
        String patientId = patientService.createPatient(patient);
        
        String response = GenomicProtocol.createResponse(
            GenomicProtocol.SUCCESS, 
            "Patient created successfully", 
            patientId
        );
        writer.println(response);
        
        logger.info("Created patient: {}", patientId);
    }
    
    private void handleGetPatient(String[] parts) throws Exception {
        if (parts.length < 2) {
            throw new IllegalArgumentException("Patient ID required for GET_PATIENT");
        }
        
        String patientId = parts[1];
        Patient patient = patientService.getPatient(patientId);
        
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        
        String response = GenomicProtocol.createResponse(
            GenomicProtocol.SUCCESS,
            patient.getPatientId(),
            patient.getFullName(),
            patient.getDocumentId(),
            String.valueOf(patient.getAge()),
            String.valueOf(patient.getSex()),
            patient.getContactEmail(),
            patient.getClinicalNotes(),
            String.valueOf(patient.isActive())
        );
        writer.println(response);
        
        logger.info("Retrieved patient: {}", patientId);
    }
    
    private void handleUpdatePatient(String[] parts) throws Exception {
        if (parts.length < 8) {
            throw new IllegalArgumentException("Insufficient parameters for UPDATE_PATIENT");
        }
        
        String patientId = parts[1];
        Patient patient = patientService.getPatient(patientId);
        
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        
        // Update patient fields
        patient.setFullName(parts[2]);
        patient.setAge(Integer.parseInt(parts[3]));
        patient.setSex(parts[4].charAt(0));
        patient.setContactEmail(parts[5]);
        patient.setClinicalNotes(parts[6]);
        
        patientService.updatePatient(patient);
        
        String response = GenomicProtocol.createResponse(
            GenomicProtocol.SUCCESS, 
            "Patient updated successfully"
        );
        writer.println(response);
        
        logger.info("Updated patient: {}", patientId);
    }
    
    private void handleDeletePatient(String[] parts) throws Exception {
        if (parts.length < 2) {
            throw new IllegalArgumentException("Patient ID required for DELETE_PATIENT");
        }
        
        String patientId = parts[1];
        patientService.deletePatient(patientId);
        
        String response = GenomicProtocol.createResponse(
            GenomicProtocol.SUCCESS, 
            "Patient deleted successfully"
        );
        writer.println(response);
        
        logger.info("Deleted patient: {}", patientId);
    }
    
    private void handleSendFasta(String[] parts) throws Exception {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Patient ID and FASTA content required for SEND_FASTA");
        }
        
        String patientId = parts[1];
        String fastaContent = parts[2];
        
        Patient patient = patientService.getPatient(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        
        // Validate FASTA format
        if (!FastaValidator.isValidFasta(fastaContent)) {
            throw new IllegalArgumentException("Invalid FASTA format");
        }
        
        // Verify checksum
        if (!ChecksumUtil.verifyChecksum(fastaContent, patient.getChecksumFasta())) {
            throw new IllegalArgumentException("FASTA checksum verification failed");
        }
        
        // Analyze genome for diseases
        List<DetectionReport> detections = genomeAnalyzer.analyzeGenome(patientId, fastaContent);
        
        if (!detections.isEmpty()) {
            // Save detection reports
            patientService.saveDetectionReports(detections);
            
            // Notify client of disease detection
            for (DetectionReport detection : detections) {
                String response = GenomicProtocol.createResponse(
                    GenomicProtocol.DISEASE_DETECTED,
                    detection.getDiseaseId(),
                    String.valueOf(detection.getSeverity()),
                    detection.getDescription()
                );
                writer.println(response);
            }
        } else {
            String response = GenomicProtocol.createResponse(
                GenomicProtocol.NO_DISEASE,
                "No diseases detected"
            );
            writer.println(response);
        }
        
        logger.info("Processed FASTA for patient: {}. Detections: {}", patientId, detections.size());
    }
    
    private void sendErrorResponse(String errorMessage) {
        String response = GenomicProtocol.createResponse(GenomicProtocol.ERROR, errorMessage);
        writer.println(response);
    }
    
    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (clientSocket != null) clientSocket.close();
            logger.info("Client connection closed");
        } catch (IOException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }
}
