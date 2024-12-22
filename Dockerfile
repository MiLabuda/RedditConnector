# Bazowy obraz Kafka Connect
FROM confluentinc/cp-kafka-connect:7.8.0

WORKDIR /kafka-connect-source-reddit
COPY config config
COPY target target

VOLUME /kafka-connect-source-reddit/config
VOLUME /kafka-connect-source-reddit/offsets
VOLUME /kafka-connect-source-reddit/logs


COPY target/reddit-connector-1.0-SNAPSHOT.jar /usr/share/java/
COPY src/main/resources/connect-log4j.properties /etc/kafka/

CMD CLASSPATH="/usr/share/java/reddit-connector-1.0-SNAPSHOT.jar" connect-standalone config/worker.properties config/RedditSourceConnectorConfig.properties & sleep infinity