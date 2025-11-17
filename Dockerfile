FROM eclipse-temurin:25.0.1_8-jdk-noble
WORKDIR /app
COPY src /app/src
RUN echo "Main-Class: GoogleJavaFormatter" > manifest.txt
RUN javac GoogleJavaFormatter.java && \
    jar cfm GoogleJavaFormatter.jar manifest.txt GoogleJavaFormatter.class

ENTRYPOINT ["sh", "-c", "java -jar GoogleJavaFormatter.jar $0 $1 \"$2\""]