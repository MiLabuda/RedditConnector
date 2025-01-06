# **Kafka Reddit Connector**
The **Kafka Reddit Connector** is a project designed to integrate the Reddit API with a Kafka ecosystem. It was created as a PoC.
Main part of the project is a Custom Kafka Connector which I wanted to create to fetch posts and comments from a specified subreddit and route them through to Kafka cluster, 
allowing downstream applications (e.g., database sinks, analytics pipelines) to process Reddit data in real time. Which is second part of the project: Subreddit Statistics.
To give my little connector a purpose I wanted to create a simple analytics pipeline which would allow me to get some insights about the subreddit I'm interested in.

This repository contains configurations for setting up and running a fully functional Kafka environment, including support for a Kafka consumer UI, a Postgres database, Redis, and Grafana for monitoring.

---
## **Features**

**Important!: This is still ongoing project, more features and cleanups will be added soon.**

- Kafka Connector to fetch subreddit posts from Reddit API.
- Integration with:
    - **Postgres**: To store processed records downstream.
    - **Redis**: A caching layer to store intermediary or offset data.
    - **Kafka UI (Kafbat)**: For visualizing and managing Kafka topics and configurations.
    - **Grafana**: For monitoring the system performance and metrics.

- Support for scalable Kafka workloads with a **multi-node Kafka cluster**.

---
## **Architecture**
The system architecture consists of several services:
1. **Kafka Cluster**: A 3-node Kafka setup with a KRaft mode controller for fault tolerance.
2. **Kafka Connect**:
    - **Reddit Source Connector**: Fetches and streams Reddit posts to a Kafka topic.
    - **Postgres Sink Connector**: Consumes records from Kafka topics and stores them in a Postgres database.

3. **Redis**: Provides caching for intermediary data or states.
4. **Postgres**: Stores data received from the Kafka sink connector.
5. **Grafana**: Monitors the performance of the Kafka system and its components.
6. **Kafka UI (Kafbat)**: Provides an interface to view topics, partitions, and connector configurations.

---
## **Project Configuration**
The project relies on configuration files located in the `config/` directory. Key files:
### 1. Reddit API Integration
- `RedditSourceConnectorConfig.properties`: Configures the Reddit Source Connector with subreddit details, rate limits, and API credentials.
- Example contents:
``` properties
  name=RedditSourceConnector
  connector.class=com.milabuda.redditconnector.RedditSourceConnector
  reddit.base.url=https://www.reddit.com
  reddit.client.id=your_client_id
  reddit.client.secret=your_client_secret 
  reddit.user.agent=your_user_agent
  reddit.subreddit=your_subreddit
  reddit.initial.full.scan=true
```
### 2. Kafka Worker Properties
- `worker.properties`: Kafka Connect worker configuration.
- Example contents:
``` properties
  bootstrap.servers=kafka1:29092,kafka2:29092,kafka3:29092
  key.converter=io.confluent.connect.avro.AvroConverter
  key.converter.schema.registry.url=http://schema-registry:8081
  value.converter=io.confluent.connect.avro.AvroConverter
  value.converter.schema.registry.url=http://schema-registry:8081
```
---
