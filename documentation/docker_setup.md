# Build the JAR
Ensure the following is in the pom file:
```
<groupId>org.noesis</groupId>
<artifactId>BatchContextCollectorRunner</artifactId>
<version>1.0</version>
```

Build the jar by running the 'package' item in the Maven view

# Create or find docker file
Create file called 'Dockerfile' in project root and put the following in

```
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
```

Open View → Tool Windows → Maven (or the little Maven tab on the right side).

Expand your project.

Under Lifecycle, double-click package.
IntelliJ will run the equivalent of:

bash
Copy
Edit
mvn clean package
in the background, and you’ll see the output in the Run/Build tool window.

# Build Docker Image

## Start Docker
Start Docker Desktop
Open Docker Desktop from Applications.

Wait until it finishes starting; you should see “Docker is running” in the Docker icon in your menu bar.

Verify the daemon is running
In your terminal, run:

```
docker info
```


Run the below command from the project root, the same directory where your Dockerfile.

```
docker build -t batch-context-collector .
```


Check that image was created

```
docker images
```

# Test Run 
```
docker run -v /Users/costa/Development/JetBrainsContextCollection/:/mnt/ContextCollectionWorkspace/ batch-context-collector --stage=private
``` 

# Extract the Docker Image
```
docker save -o batch-context-collector.tar batch-context-collector
```
