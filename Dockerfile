FROM eclipse-temurin:25.0.1_8-jdk-noble
WORKDIR /app
COPY GoogleJavaFormatter.jar .

# RUN echo "Main-Class: GoogleJavaFormatter" > manifest.txt
# RUN javac GoogleJavaFormatter.java && \
#     jar cfm GoogleJavaFormatter.jar manifest.txt GoogleJavaFormatter.class

# CMD ["sleep", "infinity"]
ENTRYPOINT ["sh", "-c", "java -jar GoogleJavaFormatter.jar $0 $1 \"$2\""]