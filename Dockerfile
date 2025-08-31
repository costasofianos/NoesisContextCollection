
FROM eclipse-temurin:24

# Set working directory
WORKDIR /app

# Create mount point directory
RUN mkdir -p /mnt/ContextCollectionWorkspace

# Copy only the JAR file
COPY target/BatchContextCollectorRunner-1.0.jar /app/

# Use ENTRYPOINT to fix the jar execution, and CMD for default arguments
ENTRYPOINT ["java", "-jar", "BatchContextCollectorRunner-1.0.jar"]
CMD []
