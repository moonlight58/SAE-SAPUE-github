# Smart Waste Server - Setup Guide

## Prerequisites

- Java 11 or higher
- MongoDB 4.4 or higher
- Maven (for dependency management)

## Step 1: Setup MongoDB

### Option A: Docker (Recommended)
```bash
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:latest
```

### Option B: Local Installation
Follow MongoDB installation guide for your OS: https://docs.mongodb.com/manual/installation/

## Step 2: Create Initial Data

Before running the server, you need to create at least one microcontroller in the database:

```javascript
// Connect to MongoDB
mongosh mongodb+srv://<db_username>:<db_password>@cluster0.vnh4z0a.mongodb.net/

// Use database
use smartwaste_dev

// Create a bin
db.mapPoints.insertOne({
  type: "recyclage",
  position: [6.0240, 47.2378],
  isSAPUE: true
})

// Get the bin ID (copy the _id value from above)

// Create a microcontroller
db.microcontrolleurs.insertOne({
    reference: "MC-001",
    mapPoint: ObjectId("60af8847e13f5a134c2c34b1"),
    ipAddress: "192.168.1.100",
    configSensor: {
        sensorType: "BME280",
        enabled: true,
        samplingInterval: 300,
        parameters: {
            tempOffset: 0.5,
            pressureUnit: "hPa"
        }
   }
})
```

## Step 3: Configure the Server

Create `config.yml` in your project root:

```yaml
server:
  port: 8888
  maxConnections: 100
  socketTimeout: 30000

mongodb:
  connectionString: "mongodb://localhost:27017"
  databaseName: "smartwaste_dev"
  environment: "dev"

logging:
  enableMetrics: true
  verbose: true  # Set to false in production
```

## Step 4: Add Maven Dependencies

Add to your `pom.xml`:

```xml
<dependencies>
    <!-- MongoDB Java Driver -->
    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongodb-driver-sync</artifactId>
        <version>4.11.1</version>
    </dependency>
    
    <!-- SnakeYAML for config -->
    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
        <version>2.0</version>
    </dependency>
</dependencies>
```

## Step 5: Build the Project

```bash
mvn clean compile
```

## Step 6: Run the Server

```bash
# With default config.yml
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.core.SmartWasteServer"

# With custom config
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.core.SmartWasteServer" -Dexec.args="/path/to/config.yml"
```

Expected output:
```
[SmartWasteServer] Connecting to MongoDB: mongodb://localhost:27017
[SmartWasteServer] Server initialized in dev mode
Configuration loaded:
ServerConfig{serverPort=8888, maxConnections=100, ...}
[SmartWasteServer] Server started on port 8888
[SmartWasteServer] Max connections: 100
[SmartWasteServer] Waiting for client connections...
```

## Step 7: Test the Server
### Option A: Using Telnet (Simple Testing) in another console
```bash
# Connect to server
telnet localhost 8888

# Once connected, try these commands:
REGISTER MC-001 192.168.1.100
# Expected: OK

PING MC-001
# Expected: OK

DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25
# Expected: OK

CONFIG_GET MC-001 
# Expected: OK sensorType:BME280 enabled:true samplingInterval:300 ...

STATUS MC-001 battery:87 uptime:3600 freeMemory:45000 
# Expected: OK

DISCONNECT MC-001
# Expected: OK
# Connection will close
```

### Option B: Using Netcat (nc)
```bash
# Test REGISTER command
echo "REGISTER MC-001 192.168.1.100" | nc localhost 8888

# Test DATA command
echo "DATA MC-001 BME280 temperature:22.5 humidity:65.0" | nc localhost 8888

# Test CONFIG_GET command
echo "CONFIG_GET MC-001" | nc localhost 8888
```
## Step 8: Test Error Handling
```bash
# Test REGISTER command
echo "REGISTER MC-001 192.168.1.100" | nc localhost 8888

# Test DATA command
echo "DATA MC-001 BME280 temperature:22.5 humidity:65.0" | nc localhost 8888

# Test CONFIG_GET command
echo "CONFIG_GET MC-001" | nc localhost 8888
```