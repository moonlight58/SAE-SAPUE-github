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
#### Étape 1 : Créer les données initiales
##### 1.1 Créer un Microcontrolleur

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

// Créer un microcontrôleur
db.microcontrolleurs.insertOne({
    reference: "MC-001",
    mapPoint: null,  // Plus utilisé avec nouvelle structure
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

##### 1.2 Créer une Poubelle liée au Microcontrolleur

```javascript
// Créer une poubelle avec le microcontrôleur MC-001
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 1.0,
    location: {
        type: "Point",
        coordinates: [6.0240, 47.2378]  // [longitude, latitude] - Besançon
    },
    adress: "1 Rue de la République, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.100",
        microcontroller: ["MC-001"],  // Référence au MC
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

##### 1.3 Créer plusieurs Poubelles

```javascript
// Poubelle 1 - Sans alerte
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 1.0,
    location: {
        type: "Point",
        coordinates: [6.0245, 47.2385]
    },
    adress: "Place de la Révolution, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.101",
        microcontroller: ["MC-002"],
        sensors: ["BME280", "HX711"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "HX711",
            weight: 45.5,
            fillLevel: 75
        }
    },
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idSignalement: null
    }
})

// Poubelle 2 - Avec alerte (pleine)
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 0.95,
    location: {
        type: "Point",
        coordinates: [6.0250, 47.2390]
    },
    adress: "Avenue Foch, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.102",
        microcontroller: ["MC-003"],
        sensors: ["HCSR04", "MQ135"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "HCSR04",
            distance: 10,
            fillLevel: 92
        }
    },
    activeAlerts: {
        hasIssue: true,
        issueType: "Plein",
        idSignalement: null  // Sera ajouté quand un signalement est créé
    }
})

// Bac de recyclage - Pas SAPUE
db.poubelles.insertOne({
    type: "Bac",
    isSapue: false,
    certaintyLevel: 0.8,
    location: {
        type: "Point",
        coordinates: [6.0255, 47.2395]
    },
    adress: "Rue Pasteur, 25000 Besançon",
    hardwareConfig: {
        ipAddress: null,
        microcontroller: [],
        sensors: []
    },
    lastMeasurement: null,
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idSignalement: null
    }
})
```
#### Étape 2 : Créer les Microcontrolleurs correspondants
```javascript
// MC-002
db.microcontrolleurs.insertOne({
    reference: "MC-002",
    mapPoint: null,
    ipAddress: "192.168.1.101",
    configSensor: {
        sensorType: "HX711",
        enabled: true,
        samplingInterval: 600,
        parameters: {
            calibrationFactor: 2000
        }
    }
})

// MC-003
db.microcontrolleurs.insertOne({
    reference: "MC-003",
    mapPoint: null,
    ipAddress: "192.168.1.102",
    configSensor: {
        sensorType: "HCSR04",
        enabled: true,
        samplingInterval: 300,
        parameters: {
            triggerPin: 5,
            echoPin: 18
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