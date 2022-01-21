FROM openjdk:8
ENV JAVA_OPTIONS -server -Xms512m -Xmx1024m
COPY example-gke-*.jar /example-gke-*.jar
EXPOSE 8080/tcp
ENTRYPOINT exec java $JAVA_OPTIONS -jar /example-gke-*.jar
