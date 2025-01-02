#!/usr/bin/env bash

# Stop and remove existing Docker containers
docker compose down

# Build and push Docker image using Maven
mvn clean package docker:build docker:push

# Build and start the Docker containers
docker compose up --build -d