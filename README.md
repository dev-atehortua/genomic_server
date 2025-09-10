# Secure Genomic Intake and Disease Detection Server

A secure client-server system for genomic data processing and disease detection using SSL/TLS over TCP.

## Features

- **Secure Communication**: SSL/TLS encrypted communication between client and server
- **Genomic Analysis**: FASTA file processing and disease pattern matching
- **Multi-threading**: Concurrent client handling with thread pool
- **Data Persistence**: CSV-based patient records and detection reports
- **Custom Protocol**: Text-based communication protocol
- **Comprehensive Logging**: Detailed operation logging

## Architecture

### Server Components
- `GenomicServer`: Main server with SSL/TLS support
- `ClientHandler`: Multi-threaded client request processing
- `PatientService`: Patient data management and persistence
- `DiseaseDatabase`: Disease pattern database loader
- `GenomeAnalyzer`: Genomic sequence analysis engine

### Client Components
- `GenomicClient`: Console-based client interface
- Interactive menu system for all operations

### Data Models
- `Patient`: Patient metadata and genomic information
- `Disease`: Disease patterns and severity levels
- `DetectionReport`: Disease detection results

## Setup Instructions

### 1. Prerequisites
- Java 11 or higher
- Maven 3.6+
- IntelliJ IDEA (recommended)

### 2. Project Setup
\`\`\`bash
# Clone the repository
git clone <repository-url>
cd secure-genomic-server

# Create necessary directories
mkdir -p data logs ssl test-data

# Generate SSL certificate
chmod +x scripts/generate-ssl-certificate.sh
./scripts/generate-ssl-certificate.sh

# Build the project
mvn clean compile
\`\`\`

### 3. Running the Server
\`\`\`bash
# Start the server
mvn exec:java -Dexec.mainClass="com.genomic.server.GenomicServer"
\`\`\`

### 4. Running the Client
\`\`\`bash
# In a separate terminal, start the client
mvn exec:java -Dexec.mainClass="com.genomic.client.GenomicClient"
\`\`\`

## Protocol Specification

### Command Format
\`\`\`
COMMAND|field1|field2|field3|...|EOT\n
\`\`\`

### Supported Commands
- `CREATE_PATIENT|fullName|documentId|age|sex|email|notes|checksum|fileSize|EOT`
- `GET_PATIENT|patientId|EOT`
- `UPDATE_PATIENT|patientId|fullName|age|sex|email|notes|EOT`
- `DELETE_PATIENT|patientId|EOT`
- `SEND_FASTA|patientId|fastaContent|EOT`

### Response Format
- `SUCCESS|message|data...|EOT`
- `ERROR|errorMessage|EOT`
- `DISEASE_DETECTED|diseaseId|severity|description|EOT`
- `NO_DISEASE|message|EOT`

## File Structure
\`\`\`
src/
├── main/java/com/genomic/
│   ├── client/          # Client implementation
│   ├── model/           # Data models
│   ├── protocol/        # Communication protocol
│   ├── server/          # Server implementation
│   ├── service/         # Business logic services
│   └── util/            # Utility classes
├── main/resources/      # Configuration files
└── test/                # Unit tests

data/
├── disease_catalog.csv  # Disease database catalog
├── diseases/            # Disease FASTA files
├── patients.csv         # Patient records
└── disease_reports.csv  # Detection reports

test-data/
└── patient_sample.fasta # Sample patient data
\`\`\`

## Development Workflow

### Git Branch Strategy
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: Feature development branches
- `bugfix/*`: Bug fix branches

### Recommended Development Order
1. **Setup Phase**: SSL certificates, project structure
2. **Protocol Development**: Communication protocol implementation
3. **Data Models**: Patient, Disease, DetectionReport classes
4. **File Handling**: FASTA validation, CSV operations
5. **Core Services**: PatientService, DiseaseDatabase
6. **Analysis Engine**: GenomeAnalyzer implementation
7. **Server Implementation**: Multi-threaded server
8. **Client Implementation**: Console interface
9. **Testing & Integration**: End-to-end testing
10. **Documentation**: Code documentation and user guides

## Testing

### Unit Tests
\`\`\`bash
mvn test
\`\`\`

### Integration Testing
1. Start the server
2. Run client operations
3. Verify data persistence
4. Check log files

## Security Considerations

- Self-signed certificates for development only
- Production deployment requires proper CA-signed certificates
- Input validation for all user data
- Secure file handling for genomic data
- Proper error handling without information leakage

## Performance Notes

- Thread pool for concurrent client handling
- Efficient sequence matching algorithms
- Memory-conscious file processing
- Configurable similarity thresholds

## Troubleshooting

### Common Issues
1. **SSL Handshake Failures**: Verify certificate generation
2. **Port Already in Use**: Check for running server instances
3. **File Not Found**: Ensure data directories exist
4. **Invalid FASTA Format**: Validate input files

### Log Files
- Server logs: `logs/genomic-server.log`
- Patient data: `data/patients.csv`
- Detection reports: `data/disease_reports.csv`
