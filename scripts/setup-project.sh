#!/bin/bash

echo "=== Configurando Proyecto Genómico ==="

# Crear directorios necesarios
echo "Creando directorios..."
mkdir -p data/diseases
mkdir -p logs
mkdir -p ssl
mkdir -p test-data

# Generar certificado SSL
echo "Generando certificado SSL..."
keytool -genkeypair -alias genomic-server \
    -keyalg RSA -keysize 2048 \
    -validity 365 \
    -keystore ssl/server.keystore \
    -storepass genomic123 \
    -keypass genomic123 \
    -dname "CN=localhost, OU=Genomic Server, O=Universidad, L=Ciudad, ST=Estado, C=CO"

# Crear datos de prueba
echo "Creando datos de prueba..."

# Catálogo de enfermedades
cat > data/disease_catalog.csv << 'EOF'
disease_id,name,severity
huntington,Huntington Disease,8
cystic_fibrosis,Cystic Fibrosis,7
sickle_cell,Sickle Cell Anemia,6
EOF

# FASTA de Huntington
cat > data/diseases/huntington.fasta << 'EOF'
>huntington_marker
CAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAG
CAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAG
EOF

# FASTA de Fibrosis Quística
cat > data/diseases/cystic_fibrosis.fasta << 'EOF'
>cftr_mutation
ATCATAGGAAACACCAAAGATAATACATTTGTATGACCCACTTTGGCAT
GCTTTGATGACGCTTCTGTATCTATATTCATCATAGGAAACACC
EOF

# FASTA de Anemia Falciforme
cat > data/diseases/sickle_cell.fasta << 'EOF'
>sickle_cell_mutation
GTGCACCTGACTCCTGAGGAGAAGTCTGCCGTTACTGCCCTGTGGGGC
AAGGTGAACGTGGATGAAGTTGGTGGTGAGGCCCTGGGCAGG
EOF

# Archivo FASTA de prueba para paciente
cat > test-data/patient_sample.fasta << 'EOF'
>patient_test_001
ACGTACGTGGCCTTAAACCGGTAGCTAGCTAGGCTAGCTAGCTAGCTAA
CAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAGCAG
ATCATAGGAAACACCAAAGATAATACATTTGTATGACCCACTTTGGCAT
GCTTTGATGACGCTTCTGTATCTATATTCATCATAGGAAACACC
GTGCACCTGACTCCTGAGGAGAAGTCTGCCGTTACTGCCCTGTGGGGC
EOF

echo "✅ Configuración completada!"
echo ""
echo "Próximos pasos:"
echo "1. Ejecutar: mvn clean compile"
echo "2. Terminal 1: mvn exec:java -Dexec.mainClass=\"com.genomic.server.GenomicServer\""
echo "3. Terminal 2: mvn exec:java -Dexec.mainClass=\"com.genomic.client.GenomicClient\""
