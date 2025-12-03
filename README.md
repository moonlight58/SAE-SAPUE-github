# Smart Waste TCP Server - Architecture Guide

## 🏗️ Architecture Overview

### 1. Main Server (`SmartWasteServer`)
- **Responsibilities:**
  - Accept incoming TCP connections
  - Manage server lifecycle (start/stop/restart)
  - Maintain registry of connected clients
  - Initialize MongoDB connection
  - Track server metrics
  
- **Key Features:**
  - Thread-per-client model (future: thread pool)
  - Connection limit enforcement
  - Graceful shutdown with cleanup
  - Client registry for duplicate prevention
  - Broadcasting capability

### 2. Client Handler (`ClientHandler`)
- **Responsibilities:**
  - Handle communication with one microcontroller
  - Parse and route commands
  - Execute database operations
  - Track per-connection metrics
  
- **Protocol Commands (Initial):**
  - `REGISTER <reference> <ipAddress>` - Register microcontroller
  - `DATA <key>=<value> ...` - Send sensor readings
  - `PING` - Keep-alive check
  - `DISCONNECT` - Graceful disconnect

### 3. Configuration (`ServerConfig`)
- **YAML-based configuration:**
  - Server settings (port, limits, timeouts)
  - MongoDB connection (connection string, database)
  - Logging preferences (metrics, verbose)
  
- **Environment support:** dev/prod modes

### 4. Data Access (`DataDriver`)
- **Pattern:** Interface + Implementation
- **Benefits:**
  - Easy to add API-based implementation later
  - Shared instance across all threads (thread-safe)
  - Clean separation of concerns
  
- **Full CRUD operations** for all collections

### 5. Metrics (`ServerMetrics`)
- **Thread-safe tracking:**
  - Active/total connections
  - Total requests/errors
  - Data transfer (bytes in/out)
  - Server uptime
  
- **Auto-printing:** Every 60 seconds (configurable)

## 🚀 How to Run

### 1. Dependencies (Maven)
```xml
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
```

### 2. Configure
Create `config.yml` in project root (see example in artifacts)

### 3. Start MongoDB
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
# or use existing MongoDB instance
```

### 4. Run Server
```bash
# With default config.yml
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.core.SmartWasteServer"

# With custom config
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.core.SmartWasteServer" -Dexec.args="/path/to/config.yml"
```

## 📊 Example Output

```
[SmartWasteServer] Connecting to MongoDB: mongodb://localhost:27017
[SmartWasteServer] Server initialized in dev mode
[SmartWasteServer] Server started on port 8888
[SmartWasteServer] Max connections: 100
[SmartWasteServer] Waiting for client connections...
[ClientHandler] Client connected: 127.0.0.1:54321
[ClientHandler] Microcontroller registered: MC-001 (192.168.1.100)
[SmartWasteServer] Client registered: MC-001 (Total: 1)

========== Server Metrics ==========
Uptime: 0d 0h 1m 23s
Active Connections: 1
Total Connections: 1
Total Requests: 4
Total Errors: 0
Data Received: 156 B
Data Sent: 12 B
====================================
```
