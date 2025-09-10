package com.genomic.client;

import com.genomic.protocol.GenomicProtocol;
import com.genomic.util.ChecksumUtil;
import com.genomic.util.FastaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class GenomicClient {
    private static final Logger logger = LoggerFactory.getLogger(GenomicClient.class);
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8443;
    
    private SSLSocket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;
    
    public GenomicClient() {
        this.scanner = new Scanner(System.in);
    }
    
    public void connect() throws Exception {
        logger.info("Connecting to server at {}:{}", SERVER_HOST, SERVER_PORT);
        
        // Create SSL context that accepts self-signed certificates
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new AcceptAllTrustManager()}, null);
        
        SSLSocketFactory factory = sslContext.getSocketFactory();
        socket = (SSLSocket) factory.createSocket(SERVER_HOST, SERVER_PORT);
        
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        
        logger.info("Connected to server successfully");
    }
    
    public void start() {
        try {
            connect();
            showMenu();
        } catch (Exception e) {
            logger.error("Error starting client: {}", e.getMessage(), e);
        } finally {
            disconnect();
        }
    }
    
    private void showMenu() {
        while (true) {
            System.out.println("\n=== Genomic Server Client ===");
            System.out.println("1. Create new patient");
            System.out.println("2. Get patient information");
            System.out.println("3. Update patient");
            System.out.println("4. Delete patient");
            System.out.println("5. Send FASTA file");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            try {
                switch (choice) {
                    case 1:
                        createPatient();
                        break;
                    case 2:
                        getPatient();
                        break;
                    case 3:
                        updatePatient();
                        break;
                    case 4:
                        deletePatient();
                        break;
                    case 5:
                        sendFasta();
                        break;
                    case 6:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    private void createPatient() throws IOException {
        System.out.println("\n=== Create New Patient ===");
        
        System.out.print("Full name: ");
        String fullName = scanner.nextLine();
        
        System.out.print("Document ID: ");
        String documentId = scanner.nextLine();
        
        System.out.print("Age: ");
        int age = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Sex (M/F): ");
        char sex = scanner.nextLine().toUpperCase().charAt(0);
        
        System.out.print("Contact email: ");
        String email = scanner.nextLine();
        
        System.out.print("Clinical notes: ");
        String notes = scanner.nextLine();
        
        System.out.print("FASTA file path: ");
        String fastaPath = scanner.nextLine();
        
        // Read and validate FASTA file
        String fastaContent = Files.readString(Paths.get(fastaPath));
        if (!FastaValidator.isValidFasta(fastaContent)) {
            throw new IllegalArgumentException("Invalid FASTA format");
        }
        
        String checksum = ChecksumUtil.calculateSHA256(fastaContent);
        long fileSize = Files.size(Paths.get(fastaPath));
        
        String command = GenomicProtocol.createCommand(
            GenomicProtocol.CREATE_PATIENT,
            fullName, documentId, String.valueOf(age), String.valueOf(sex),
            email, notes, checksum, String.valueOf(fileSize)
        );
        
        writer.println(command);
        String response = reader.readLine();
        
        String[] parts = GenomicProtocol.parseCommand(response);
        if (GenomicProtocol.SUCCESS.equals(parts[0])) {
            System.out.println("Patient created successfully!");
            System.out.println("Patient ID: " + parts[2]);
        } else {
            System.err.println("Error: " + parts[1]);
        }
    }
    
    private void getPatient() throws IOException {
        System.out.println("\n=== Get Patient Information ===");
        
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        
        String command = GenomicProtocol.createCommand(GenomicProtocol.GET_PATIENT, patientId);
        writer.println(command);
        
        String response = reader.readLine();
        String[] parts = GenomicProtocol.parseCommand(response);
        
        if (GenomicProtocol.SUCCESS.equals(parts[0])) {
            System.out.println("\nPatient Information:");
            System.out.println("Patient ID: " + parts[1]);
            System.out.println("Full Name: " + parts[2]);
            System.out.println("Document ID: " + parts[3]);
            System.out.println("Age: " + parts[4]);
            System.out.println("Sex: " + parts[5]);
            System.out.println("Email: " + parts[6]);
            System.out.println("Clinical Notes: " + parts[7]);
            System.out.println("Active: " + parts[8]);
        } else {
            System.err.println("Error: " + parts[1]);
        }
    }
    
    private void updatePatient() throws IOException {
        System.out.println("\n=== Update Patient ===");
        
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        
        System.out.print("New full name: ");
        String fullName = scanner.nextLine();
        
        System.out.print("New age: ");
        int age = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("New sex (M/F): ");
        char sex = scanner.nextLine().toUpperCase().charAt(0);
        
        System.out.print("New contact email: ");
        String email = scanner.nextLine();
        
        System.out.print("New clinical notes: ");
        String notes = scanner.nextLine();
        
        String command = GenomicProtocol.createCommand(
            GenomicProtocol.UPDATE_PATIENT,
            patientId, fullName, String.valueOf(age), String.valueOf(sex), email, notes
        );
        
        writer.println(command);
        String response = reader.readLine();
        
        String[] parts = GenomicProtocol.parseCommand(response);
        if (GenomicProtocol.SUCCESS.equals(parts[0])) {
            System.out.println("Patient updated successfully!");
        } else {
            System.err.println("Error: " + parts[1]);
        }
    }
    
    private void deletePatient() throws IOException {
        System.out.println("\n=== Delete Patient ===");
        
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        
        System.out.print("Are you sure? (y/N): ");
        String confirm = scanner.nextLine();
        
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("Operation cancelled");
            return;
        }
        
        String command = GenomicProtocol.createCommand(GenomicProtocol.DELETE_PATIENT, patientId);
        writer.println(command);
        
        String response = reader.readLine();
        String[] parts = GenomicProtocol.parseCommand(response);
        
        if (GenomicProtocol.SUCCESS.equals(parts[0])) {
            System.out.println("Patient deleted successfully!");
        } else {
            System.err.println("Error: " + parts[1]);
        }
    }
    
    private void sendFasta() throws IOException {
        System.out.println("\n=== Send FASTA File ===");
        
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine();
        
        System.out.print("FASTA file path: ");
        String fastaPath = scanner.nextLine();
        
        String fastaContent = Files.readString(Paths.get(fastaPath));
        if (!FastaValidator.isValidFasta(fastaContent)) {
            throw new IllegalArgumentException("Invalid FASTA format");
        }
        
        String command = GenomicProtocol.createCommand(
            GenomicProtocol.SEND_FASTA,
            patientId, fastaContent
        );
        
        writer.println(command);
        
        // Read all responses (there might be multiple disease detections)
        String response;
        boolean hasDetections = false;
        
        while ((response = reader.readLine()) != null) {
            String[] parts = GenomicProtocol.parseCommand(response);
            
            if (GenomicProtocol.DISEASE_DETECTED.equals(parts[0])) {
                if (!hasDetections) {
                    System.out.println("\n⚠️  DISEASE DETECTED! ⚠️");
                    hasDetections = true;
                }
                System.out.println("Disease ID: " + parts[1]);
                System.out.println("Severity: " + parts[2] + "/10");
                System.out.println("Description: " + parts[3]);
                System.out.println("---");
            } else if (GenomicProtocol.NO_DISEASE.equals(parts[0])) {
                System.out.println("✅ No diseases detected - Genome appears healthy!");
                break;
            } else if (GenomicProtocol.ERROR.equals(parts[0])) {
                System.err.println("Error: " + parts[1]);
                break;
            }
        }
    }
    
    private void disconnect() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.error("Error disconnecting: {}", e.getMessage());
        }
    }
    
    // Trust manager that accepts all certificates (for development only)
    private static class AcceptAllTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    
    public static void main(String[] args) {
        GenomicClient client = new GenomicClient();
        client.start();
    }
}
