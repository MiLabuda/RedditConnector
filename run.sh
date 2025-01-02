#!/usr/bin/env bash

# Set the Docker image tag based on the latest Git tag
TAG=$(git describe --tags --abbrev=0)

# Set the Docker image name
IMAGE_NAME="milabuda544/kafka-reddit-connector"

# Build and push Docker image with the specific tag
mvn clean package docker:build -Ddocker.tag=$TAG docker:push

# Tag the image as 'latest'
docker tag $IMAGE_NAME:$TAG $IMAGE_NAME:latest

# Push the 'latest' tag
docker push $IMAGE_NAME:latest

# Stop and remove existing Docker containers
docker compose down

# Build and start the Docker containers
docker compose up --build -d