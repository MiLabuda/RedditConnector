#!/usr/bin/env bash


# Set the Docker image tag
TAG=$(git describe --tags --abbrev=0)

# Build and push Docker image using Maven
mvn clean package docker:build -Ddocker.tag=$TAG docker:push

# Stop and remove existing Docker containers
docker compose down

# Build and start the Docker containers
docker compose up --build -d