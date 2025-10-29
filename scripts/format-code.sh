#!/bin/bash
echo "Formatting Java code..."
mvn spotless:apply

echo "Running checkstyle..."
mvn checkstyle:check

echo "Code quality check completed!"