#!/bin/bash

# Generate SSL certificate for development
echo "Generating SSL certificate for Genomic Server..."

# Create keystore directory if it doesn't exist
mkdir -p ssl

# Generate keystore with self-signed certificate
keytool -genkeypair \
    -alias genomic-server \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -keystore server.keystore \
    -storepass genomic123 \
    -keypass genomic123 \
    -dname "CN=localhost, OU=Genomic Server, O=University, L=City, ST=State, C=US"

echo "SSL certificate generated successfully!"
echo "Keystore: server.keystore"
echo "Password: genomic123"
