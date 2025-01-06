#!/bin/bash

curl -X POST -H "Content-Type: application/json" \
  --data '{
    "name": "kafka-to-postgres-sink",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
      "tasks.max": "6",
      "topics": "reddit-posts,reddit-comments",
      "connection.url": "jdbc:postgresql://postgres:5432/subreddit-db",
      "connection.user": "user",
      "connection.password": "developmentpassword",
      "insert.mode": "upsert",
      "auto.create": "true",
      "auto.evolve": "true",
      "pk.mode": "record_value",
      "pk.fields": "id"
    }
  }' \
  http://localhost:8084/connectors