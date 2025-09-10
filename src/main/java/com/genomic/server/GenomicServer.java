package com.genomic.server;

import com.genomic.service.DiseaseDatabase;
import com.genomic.service.GenomeAnalyzer;
import com.genomic.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenomicServer {
    private static final Logger logger = LoggerFactory.getLogger(GenomicServer.class);
    private static final int PORT = 8443;
    private static final String KEYSTORE_PATH = "server.keystore";
    private static final String KEYSTORE_PASSWORD = "genomic123";

    private SSLServerSocket serverSocket;
    private ExecutorService threadPool;
    private PatientService patientService;
    private GenomeAnalyzer genomeAnalyzer;
    private DiseaseDatabase diseaseDatabase;
    private boolean running = false;

    public GenomicServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.patientService = new PatientService("data/patients.csv", "data/disease_reports.csv");
        this.diseaseDatabase = new DiseaseDatabase();
        this.genomeAnalyzer = new GenomeAnalyzer(diseaseDatabase);
    }

    public void start() throws Exception {
        logger.info("Starting Genomic Server...");

        // Load disease database
        loadDiseaseDatabase();

        // Setup SSL
        setupSSL();

        // Start server
        running = true;
        logger.info("Server started on port {} with SSL/TLS", PORT);

        while (running) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, patientService, genomeAnalyzer);
                threadPool.submit(handler);

                logger.info("New client connection accepted");
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection: {}", e.getMessage());
                }
            }
        }
    }

    private void loadDiseaseDatabase() throws IOException {
        logger.info("Loading disease database...");
        diseaseDatabase.loadDiseaseDatabase("data/disease_catalog.csv", "data/diseases");
        logger.info("Disease database loaded successfully. Total diseases: {}", diseaseDatabase.getDiseaseCount());
    }

    private void setupSSL() throws Exception {
        // Load keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        // Setup key manager
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // Setup trust manager
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        // Create SSL server socket
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);

        logger.info("SSL/TLS configured successfully");
    }

    public void stop() {
        logger.info("Stopping Genomic Server...");
        running = false;

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }

        threadPool.shutdown();
        logger.info("Server stopped");
    }

    public static void main(String[] args) {
        GenomicServer server = new GenomicServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            server.start();
        } catch (Exception e) {
            logger.error("Failed to start server: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
