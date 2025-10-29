#!/bin/bash
echo "Building application..."
mvn clean compile

echo "Running application..."
mvn exec:java -Dexec.mainClass="com.example.urlshortener.core.Application"