#!/bin/bash
echo "Running unit tests..."
mvn test

echo "Running integration tests..."
mvn verify

echo "Generating test coverage report..."
mvn jacoco:report

echo "All tests completed!"