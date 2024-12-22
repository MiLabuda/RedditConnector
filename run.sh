#!/usr/bin/env bash

# Clean and package the Maven project
mvn clean package

# Build the Docker image
docker build . -t my-reddit-connector:1.0

# Stop and remove existing Docker containers
docker-compose down

# Build and start the Docker containers
docker-compose up --build -d