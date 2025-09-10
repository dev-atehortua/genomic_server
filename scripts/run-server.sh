#!/bin/bash
echo "🚀 Iniciando Servidor Genómico..."
mvn clean compile
mvn exec:java -Dexec.mainClass="com.genomic.server.GenomicServer"
