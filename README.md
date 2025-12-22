# Smart Waste TCP Server - Complete Documentation

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Setup Guide](#setup-guide)
4. [Protocol Documentation](#protocol-documentation)
5. [Database Schema](#database-schema)
6. [Docker Deployment](#docker-deployment)
7. [Testing](#testing)
8. [Migration Guide](#migration-guide)

---

## Overview

Smart Waste TCP Server is a multi-threaded TCP server for the Smart Waste project, designed to handle concurrent connections from microcontrollers (ESP32/ESP8266) and multimedia analysis services. It centralizes waste management data collection and storage.

### Key Features

- **Multi-threaded Architecture**: Thread-per-client model with configurable connection limits
- **Protocol**: Space-delimited text-based TCP protocol with standardized error codes
- **Database**: MongoDB with POJO mapping for all collections
- **Dual Storage**: API-based (Node.js) and direct MongoDB access with fallback
- **Metrics**: Real-time tracking of connections, requests, errors, and data transfer
- **Automatic Updates**: Real-time `lastMeasurement` updates in Poubelles collection

---

## Architecture

### Components

#### 1. **SmartWasteServer** (Main Server)
- Accepts incoming TCP connections
- Manages server lifecycle (start/stop/restart)
- Maintains registry of connected clients
- Enforces connection limits
- Graceful shutdown with cleanup

#### 2. **ClientHandler** (Per-Connection Thread)
- Handles communication with one microcontroller
- Parses and routes commands
- Executes database operations
- Tracks per-connection metrics

#### 3. **ProtocolParser** (Command Parser)
- Parses raw TCP requests into structured objects
- Validates format, parameters, and values
- Returns standardized error codes on failure

#### 4. **CommandHandler** (Business Logic)
- Executes parsed commands
- Interacts with database via DataDriver
- Updates Poubelles `lastMeasurement` automatically
- Manages device registration and configuration

#### 5. **MongoDataDriver** (Data Access Layer)
- Implements DataDriver interface
- Provides CRUD operations for all collections
- Thread-safe with POJO codec support
- Connection pooling and error handling

#### 6. **ServerConfig** (Configuration)
- YAML-based configuration
- Server settings (port, limits, timeouts)
- MongoDB connection strings
- Logging preferences

#### 7. **ServerMetrics** (Monitoring)
- Thread-safe atomic counters
- Tracks active/total connections
- Monitors requests, errors, data transfer
- Auto-prints summary every 60 seconds

---

## Setup Guide

### Prerequisites

- Java 17 or higher
- MongoDB 4.4+ (Docker recommended)
- Maven 3.9+

### Step 1: MongoDB Setup

**Option A: Docker (Recommended)**
```bash
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:latest
```

**Option B: Local Installation**
Follow: https://docs.mongodb.com/manual/installation/

### Step 2: Create Initial Data

Connect to MongoDB:
```bash
mongosh mongodb://localhost:27017
use smartwaste_dev
```

Create Microcontroller:
```javascript
db.microcontrolleurs.insertOne({
    reference: "MC-001",
    mapPoint: null,
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

Create Poubelle:
```javascript
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 1.0,
    location: {
        type: "Point",
        coordinates: [6.0240, 47.2378]
    },
    adress: "1 Rue de la République, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.100",
        microcontroller: ["MC-001"],
        sensors: ["BME280"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "BME280",
            temperature: 20.5,
            humidity: 65.0,
            pressure: 1013.25
        }
    },
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idSignalement: null
    }
})
```

Create geospatial index:
```javascript
db.poubelles.createIndex({ "location": "2dsphere" })
```

### Step 3: Configure Server

Create `config.yml`:
```yaml
server:
  port: 8888
  maxConnections: 100
  socketTimeout: 30000  # 30 seconds

mongodb:
  connectionString: "mongodb://localhost:27017"
  databaseName: "smartwaste_dev"
  environment: "dev"

logging:
  enableMetrics: true
  verbose: false
```

### Step 4: Build & Run

```bash
# Build
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main"

# Or with custom config
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main" -Dexec.args="/path/to/config.yml"
```

### Step 5: Test Connection

**Using Telnet:**
```bash
telnet localhost 8888

# Commands:
REGISTER MC-001 192.168.1.100
PING MC-001
DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25
CONFIG_GET MC-001
STATUS MC-001 battery:87 uptime:3600
DISCONNECT MC-001
```

**Using Netcat:**
```bash
echo "REGISTER MC-001 192.168.1.100" | nc localhost 8888
echo "DATA MC-001 BME280 temperature:22.5" | nc localhost 8888
```

---

## Protocol Documentation

### Protocol Format

```
COMMAND <reference> [parameters...]
```

- **COMMAND**: Uppercase command name
- **reference**: Microcontroller identifier (3-50 chars, alphanumeric + hyphens/underscores)
- **parameters**: Space-separated `key:value` pairs

### Response Format

- **Success**: `OK` or `OK [data]`
- **Error**: `ERR_CODE` (see error codes below)

### Commands

#### 1. REGISTER

Register a microcontroller with the server.

**Format:**
```
REGISTER <reference> <ipAddress>
```

**Example:**
```
→ REGISTER MC-001 192.168.1.100
← OK
```

**Responses:**
- `OK` - Registration successful
- `ERR_ALREADY_REGISTERED` - Device already connected
- `ERR_DEVICE_NOT_FOUND` - Device not in database
- `ERR_INVALID_VALUE` - Invalid reference or IP format

#### 2. DATA

Send sensor readings to be stored.

**Format:**
```
DATA <reference> <sensorType> <key>:<value> [<key>:<value> ...]
```

**Supported Sensor Types:**
- `BME280` - Temperature, humidity, pressure
- `HX711` - Weight sensor
- `HCSR04` - Ultrasonic distance
- `MQ135` - Air quality
- `REED` - Reed switch
- `DHT22` - Temperature and humidity
- `BATTERY` - Battery monitoring

**Example:**
```
→ DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25
← OK
```

**Note:** This automatically updates `lastMeasurement` in the Poubelle collection!

**Responses:**
- `OK` - Data stored successfully
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered
- `ERR_SENSOR_NOT_FOUND` - Unknown sensor type
- `ERR_INVALID_VALUE` - Invalid number format

#### 3. CONFIG_GET

Retrieve current sensor configuration.

**Format:**
```
CONFIG_GET <reference>
```

**Example:**
```
→ CONFIG_GET MC-001
← OK sensorType:BME280 enabled:true samplingInterval:300 tempOffset:0.5
```

#### 4. CONFIG_UPDATE

Update sensor configuration.

**Format:**
```
CONFIG_UPDATE <reference> <key>:<value> [<key>:<value> ...]
```

**Example:**
```
→ CONFIG_UPDATE MC-001 samplingInterval:600 enabled:true
← OK
```

#### 5. STATUS

Report microcontroller status.

**Format:**
```
STATUS <reference> <key>:<value> [<key>:<value> ...]
```

**Example:**
```
→ STATUS MC-001 battery:87 uptime:3600 freeMemory:45000
← OK
```

#### 6. PING

Simple keep-alive check.

**Format:**
```
PING <reference>
```

**Example:**
```
→ PING MC-001
← OK
```

#### 7. DISCONNECT

Gracefully disconnect.

**Format:**
```
DISCONNECT <reference>
```

**Example:**
```
→ DISCONNECT MC-001
← OK
```

### Error Codes

#### Client Errors (4xx)
```
ERR_INVALID_FORMAT          # Malformed request (400)
ERR_MISSING_PARAMS          # Required parameters missing (400)
ERR_INVALID_VALUE           # Value out of range (400)
ERR_INVALID_COMMAND         # Unknown command (400)
ERR_MALFORMED_REQUEST       # Request corrupted (400)
ERR_DEVICE_NOT_REGISTERED   # Device not registered (401)
ERR_DEVICE_NOT_FOUND        # Device ID not in database (404)
ERR_SENSOR_NOT_FOUND        # Unsupported sensor type (404)
ERR_ALREADY_REGISTERED      # Device already connected (409)
ERR_PAYLOAD_TOO_LARGE       # Data too large (413)
ERR_RATE_LIMIT              # Too many requests (429)
```

#### Server Errors (5xx)
```
ERR_INTERNAL_ERROR          # Generic server error (500)
ERR_DATABASE_ERROR          # Database operation failed (500)
ERR_API_UNAVAILABLE         # API Node unavailable (503)
ERR_SERVICE_UNAVAILABLE     # Server overloaded (503)
ERR_STORAGE_FULL            # Storage full (507)
ERR_QUEUE_FULL              # Queue full (507)
```

### Validation Rules

**Reference Format:**
- Length: 3-50 characters
- Allowed: alphanumeric, hyphens (-), underscores (_)
- Example: `MC-001`, `BIN_SENSOR_42`

**IP Address Format:**
- Standard IPv4: `xxx.xxx.xxx.xxx`
- Each octet: 0-255

**Key:Value Pairs:**
- Format: `key:value` (colon separator, no spaces)
- Key: alphanumeric + underscores
- Value: any string (numeric values auto-parsed)

---

## Database Schema

### Collections

#### 1. **poubelles**
```javascript
{
  _id: ObjectId,
  type: String,                    // "Poubelle", "Bac", etc.
  isSapue: Boolean,                // Smart bin or not
  certaintyLevel: Double,          // AI detection confidence (0-1)
  location: {
    type: "Point",
    coordinates: [longitude, latitude]
  },
  adress: String,
  hardwareConfig: {
    ipAddress: String,
    microcontroller: [String],     // References like ["MC-001"]
    sensors: [String]              // ["BME280", "HX711"]
  },
  lastMeasurement: {               // AUTO-UPDATED by DATA command
    date: Date,
    measurement: {
      sensorType: String,
      temperature: Double,
      humidity: Double,
      // ... sensor-specific fields
    }
  },
  activeAlerts: {
    hasIssue: Boolean,
    issueType: String,             // "Plein", "Problème technique"
    idSignalement: ObjectId
  }
}
```

#### 2. **microcontrolleurs**
```javascript
{
  _id: ObjectId,
  reference: String,               // Unique identifier (e.g., "MC-001")
  mapPoint: ObjectId,              // Deprecated, use idPoubelle
  idPoubelle: ObjectId,            // Reference to poubelles
  ipAddress: String,
  configSensor: {
    sensorType: String,
    enabled: Boolean,
    samplingInterval: Integer,     // seconds
    parameters: {                  // Flexible sensor-specific config
      tempOffset: Double,
      pressureUnit: String,
      // ...
    }
  }
}
```

#### 3. **releves**
```javascript
{
  _id: ObjectId,
  idPoubelle: ObjectId,            // Reference to poubelles
  timestamp: Date,
  measurements: {
    fillLevel: Double,             // %
    weight: Double,                // kg
    temperature: Double,           // °C
    humidity: Double,              // %
    airQuality: Double,            // ppm/ppb
    batteryLevel: Double           // %
  }
}
```

#### 4. **signalements**
```javascript
{
  _id: ObjectId,
  author: {
    idUser: ObjectId,
    name: String,
    role: String                   // "user", "agent", "admin"
  },
  cleaner: { /* same structure */ },
  createdAt: Date,
  updatedAt: Date,
  status: String,                  // "Ouvert", "En cours", "Résolu"
  idPoubelle: ObjectId,            // Link to bin (if bin issue)
  type: String,                    // "Depot sauvage", "Problème poubelle"
  issueType: String,               // Specific issue
  location: {
    type: "Point",
    coordinates: [longitude, latitude]
  },
  photo: {
    initialPhoto: String,
    finalPhoto: String
  },
  history: [
    {
      date: Date,
      status: String,
      byUser: ObjectId
    }
  ]
}
```

#### 5. **analyseMedias**
```javascript
{
  _id: ObjectId,
  resultat: {                      // Flexible structure
    wasteDetected: Boolean,
    wasteType: String,
    confidence: Double,
    // ... analysis-specific fields
  }
}
```

### Indexes

**Required:**
```javascript
db.poubelles.createIndex({ "location": "2dsphere" })
db.poubelles.createIndex({ "hardwareConfig.microcontroller": 1 })
db.microcontrolleurs.createIndex({ "reference": 1 }, { unique: true })
db.releves.createIndex({ "idPoubelle": 1, "timestamp": -1 })
```

---

## Docker Deployment

### Build Image

```bash
docker build -t sapue-server-tcp:latest .
```

### Run Container

```bash
docker run --network host sapue-server-tcp:latest
```

### Dockerfile

```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
COPY config.yml /app/
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Testing

### Unit Tests (Cucumber)

```bash
mvn test
```

### Feature Files

Located in `src/test/resources/fr/smart_waste/sapue/`:
- `BinMonitoringClient.feature` - Bin sensor data collection
- `APICommunication.feature` - API communication scenarios
- `MediaAnalysisServer.feature` - Media analysis results
- `MongoDBManipulation.feature` - Database operations
- `ErrorRecovery.feature` - Error handling and recovery
- `PerformanceLoad.feature` - Load and performance testing
- `ProtocolEdgeCases.feature` - Protocol edge cases

### Manual Testing with TestClient

```java
java fr.smart_waste.sapue.TestClient localhost 8888
```

---

## Migration Guide

### From MapPoint to Poubelles Structure

**Step 1: Migrate existing MapPoints**

```javascript
db.mapPoints.find().forEach(function(mapPoint) {
    var mc = db.microcontrolleurs.findOne({ mapPoint: mapPoint._id });
    
    if (mc) {
        db.poubelles.insertOne({
            type: mapPoint.type,
            isSapue: mapPoint.isSAPUE,
            certaintyLevel: 1.0,
            location: {
                type: "Point",
                coordinates: mapPoint.position
            },
            adress: "À renseigner",
            hardwareConfig: {
                ipAddress: mc.ipAddress,
                microcontroller: [mc.reference],
                sensors: [mc.configSensor.sensorType]
            },
            lastMeasurement: null,
            activeAlerts: {
                hasIssue: false,
                issueType: null,
                idSignalement: null
            }
        });
    }
});
```

**Step 2: Update Microcontrolleur references**

```javascript
db.microcontrolleurs.find().forEach(function(mc) {
    var poubelle = db.poubelles.findOne({
        "hardwareConfig.microcontroller": mc.reference
    });
    
    if (poubelle) {
        db.microcontrolleurs.updateOne(
            { _id: mc._id },
            { $set: { idPoubelle: poubelle._id } }
        );
    }
});
```

### Key Changes

1. **lastMeasurement is automatically updated** when DATA command is received
2. **activeAlerts must be manually updated** when signalement is created (implement in Node API)
3. **Microcontrolleur-Poubelle link** via reference string in array
4. **Geospatial queries** now use `poubelles.location` instead of `mapPoints.position`

### Useful Queries

**Find all bins with active alerts:**
```javascript
db.poubelles.find({ "activeAlerts.hasIssue": true })
```

**Find bins near a point:**
```javascript
db.poubelles.find({
    location: {
        $near: {
            $geometry: { type: "Point", coordinates: [6.0240, 47.2378] },
            $maxDistance: 1000  // meters
        }
    }
})
```

**Find bins by microcontroller:**
```javascript
db.poubelles.findOne({ "hardwareConfig.microcontroller": "MC-001" })
```

**Find bins without microcontroller (AI-detected):**
```javascript
db.poubelles.find({ 
    "hardwareConfig.microcontroller": { $size: 0 },
    certaintyLevel: { $lt: 1.0 }
})
```

---

## Development Guidelines

### Code Structure

```
src/main/java/fr/smart_waste/sapue/
├── Main.java                    # Entry point
├── config/
│   └── ServerConfig.java        # YAML configuration
├── core/
│   ├── SmartWasteServer.java   # Main server
│   ├── ClientHandler.java       # Per-client thread
│   └── ServerMetrics.java       # Metrics tracking
├── dataaccess/
│   ├── DataDriver.java          # Interface
│   └── MongoDataDriver.java     # MongoDB implementation
├── model/
│   ├── Poubelles.java
│   ├── Microcontrolleur.java
│   ├── Releves.java
│   ├── Signalements.java
│   ├── AnalyseMedia.java
│   └── SensorConfig.java
└── protocol/
    ├── ProtocolParser.java      # Request parser
    ├── ProtocolRequest.java     # Parsed request object
    ├── ProtocolException.java   # Protocol errors
    └── CommandHandler.java      # Command execution
```

### Best Practices

1. **Always validate input** before database operations
2. **Use try-catch** for all database operations
3. **Log errors with context** (client, command, parameters)
4. **Update metrics** for all operations
5. **Close resources** in finally blocks
6. **Test edge cases** thoroughly

### Performance Considerations

- Thread pool (future): Replace thread-per-client with thread pool
- Connection pooling: MongoDB driver handles this automatically
- Batch operations: Consider batching for bulk inserts
- Index usage: Verify with `explain()` in MongoDB
- Memory management: Monitor metrics for memory leaks

---

## Troubleshooting

### Common Issues

**1. "ERR_DEVICE_NOT_FOUND"**
- Check if microcontroller exists in database
- Verify reference format (3-50 chars, alphanumeric + hyphens/underscores)

**2. "ERR_ALREADY_REGISTERED"**
- Device is already connected
- Disconnect first or wait for timeout

**3. "ERR_DATABASE_ERROR"**
- Check MongoDB is running
- Verify connection string in config.yml
- Check database logs

**4. Port already in use**
```bash
# Find process using port
lsof -i :8888
# Kill process
kill -9 <PID>
```

**5. Connection timeout**
- Increase `socketTimeout` in config.yml
- Check network latency
- Verify firewall rules

### Logging

**Enable verbose logging:**
```yaml
logging:
  verbose: true
```

**View metrics:**
```
Printed automatically every 60 seconds when enableMetrics: true
```

---

## License & Contact

**Project:** Smart Waste - SAÉ S5 Development - Serveur Java TCP Centralisation des données

**Authors:** BELHADJ Quentin, BERNARD Elena, RÖTHLIN Gaël, SOLTNER Audrick

**Institution:** IUT Besançon - UMLP

---

## Appendix

### Maven Dependencies

```xml
<dependencies>
    <!-- MongoDB Java Driver -->
    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongodb-driver-sync</artifactId>
        <version>4.11.1</version>
    </dependency>
    
    <!-- SnakeYAML -->
    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
        <version>2.0</version>
    </dependency>
    
    <!-- Cucumber Testing -->
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java</artifactId>
        <version>7.28.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Configuration Options

```yaml
server:
  port: 8888                    # TCP listen port
  maxConnections: 100           # Max concurrent clients
  socketTimeout: 30000          # Socket timeout (ms)

mongodb:
  connectionString: "mongodb://localhost:27017"
  databaseName: "smartwaste_dev"
  environment: "dev"            # "dev" or "prod"

logging:
  enableMetrics: true           # Enable metrics tracking
  verbose: false                # Verbose request/response logging
```