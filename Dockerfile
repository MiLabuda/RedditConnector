# Bazowy obraz Kafka Connect
FROM confluentinc/cp-kafka-connect:7.8.0

USER root

WORKDIR /kafka-connect-source-reddit
RUN mkdir -p kafka-data && touch kafka-data/offsets
COPY config config
COPY target target

ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"

EXPOSE 5005

COPY target/classes/connect-log4j.properties /etc/kafka/connect-log4j.properties
COPY target/reddit-connector-1.0-SNAPSHOT.jar /usr/share/java/
CMD CLASSPATH="/usr/share/java/reddit-connector-1.0-SNAPSHOT.jar" connect-standalone config/worker.properties config/RedditSourceConnectorConfig.properties