# Smart Waste TCP Protocol Documentation

## Overview

The Smart Waste TCP protocol is a **space-delimited, text-based protocol** for communication between microcontrollers and the central server. All commands require the microcontroller reference to be included.

## Protocol Format

```
COMMAND <reference> [parameters...]
```

- **COMMAND**: Uppercase command name
- **reference**: Microcontroller identifier (3-50 chars, alphanumeric + hyphens/underscores)
- **parameters**: Space-separated key:value pairs or specific arguments

## Response Format

All responses follow a consistent format:

- **Success**: `OK` or `OK [data]`
- **Error**: `ERR_CODE` (from Liste Code.md)

## Commands

### 1. REGISTER

Register a microcontroller with the server.

**Format:**
```
REGISTER <reference> <ipAddress>
```

**Parameters:**
- `reference`: Microcontroller identifier (e.g., MC-001)
- `ipAddress`: IPv4 address (e.g., 192.168.1.100)

**Example:**
```
→ REGISTER MC-001 192.168.1.100
← OK
```

**Responses:**
- `OK` - Registration successful
- `ERR_ALREADY_REGISTERED` - Device already connected
- `ERR_DEVICE_NOT_FOUND` - Device not in database
- `ERR_MISSING_PARAMS` - Missing required parameters
- `ERR_INVALID_VALUE` - Invalid reference or IP format

**Notes:**
- Microcontroller must exist in database before registration
- Duplicate connections are rejected
- IP address is updated in database if changed

---

### 2. DATA

Send sensor readings to be stored in the database.

**Format:**
```
DATA <reference> <sensorType> <key>:<value> [<key>:<value> ...]
```

**Parameters:**
- `reference`: Microcontroller identifier
- `sensorType`: Type of sensor (see supported sensors below)
- `key:value`: Sensor reading pairs (numeric or string values)

**Supported Sensor Types:**
- `BME280` - Temperature, humidity, pressure sensor
- `HX711` - Weight sensor
- `HCSR04` - Ultrasonic distance/proximity sensor
- `MQ135` - Air quality sensor
- `REED` - Reed switch (door open/close)
- `OV2640` - Camera module
- `DHT22` - Temperature and humidity sensor
- `BATTERY` - Battery level monitoring

**Example:**
```
→ DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25
← OK

→ DATA MC-002 HX711 weight:45.3 unit:kg
← OK

→ DATA MC-003 HCSR04 distance:78 fillLevel:65
← OK
```

**Responses:**
- `OK` - Data stored successfully
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered with server
- `ERR_DEVICE_NOT_FOUND` - Device not in database
- `ERR_SENSOR_NOT_FOUND` - Unknown or unsupported sensor type
- `ERR_MISSING_PARAMS` - Missing required parameters
- `ERR_INVALID_FORMAT` - Invalid key:value pair format
- `ERR_DATABASE_ERROR` - Failed to store in database

**Notes:**
- Values are automatically parsed as numbers if possible
- Non-numeric values are stored as strings
- Data is stored in the `releves` collection with timestamp
- Sensor type is validated against microcontroller configuration

---

### 3. CONFIG_GET

Retrieve current sensor configuration from database.

**Format:**
```
CONFIG_GET <reference>
```

**Parameters:**
- `reference`: Microcontroller identifier

**Example:**
```
→ CONFIG_GET MC-001
← OK sensorType:BME280 enabled:true samplingInterval:300 tempOffset:0.5 pressureUnit:hPa
```

**Responses:**
- `OK [config_data]` - Configuration returned (space-separated key:value pairs)
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered with server
- `ERR_DEVICE_NOT_FOUND` - Device not in database

**Notes:**
- Returns all sensor configuration including custom parameters
- If no configuration exists: `OK sensorType:none enabled:false`

---

### 4. CONFIG_UPDATE

Update sensor configuration in database.

**Format:**
```
CONFIG_UPDATE <reference> <key>:<value> [<key>:<value> ...]
```

**Parameters:**
- `reference`: Microcontroller identifier
- `key:value`: Configuration pairs to update

**Standard Configuration Keys:**
- `sensorType` - Type of sensor
- `enabled` - true/false
- `samplingInterval` - Interval in seconds
- Custom keys are stored in parameters

**Example:**
```
→ CONFIG_UPDATE MC-001 samplingInterval:600 enabled:true tempOffset:1.0
← OK

→ CONFIG_UPDATE MC-002 enabled:false
← OK
```

**Responses:**
- `OK` - Configuration updated successfully
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered with server
- `ERR_DEVICE_NOT_FOUND` - Device not in database
- `ERR_INVALID_VALUE` - Invalid value format (e.g., non-numeric samplingInterval)
- `ERR_DATABASE_ERROR` - Failed to update database

**Notes:**
- Only specified fields are updated (partial update)
- Values are automatically parsed as appropriate types
- Configuration persists across reconnections

---

### 5. STATUS

Report microcontroller status information (battery, uptime, memory, etc.).

**Format:**
```
STATUS <reference> <key>:<value> [<key>:<value> ...]
```

**Parameters:**
- `reference`: Microcontroller identifier
- `key:value`: Status information pairs

**Common Status Keys:**
- `battery` - Battery level (0-100)
- `uptime` - Uptime in seconds
- `freeMemory` - Free memory in bytes
- `rssi` - WiFi signal strength (dBm)
- `voltage` - Battery voltage (V)

**Example:**
```
→ STATUS MC-001 battery:87 uptime:3600 freeMemory:45000 rssi:-65
← OK

→ STATUS MC-002 battery:42 voltage:3.7 temperature:28
← OK
```

**Responses:**
- `OK` - Status stored successfully
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered with server
- `ERR_DEVICE_NOT_FOUND` - Device not in database
- `ERR_MISSING_PARAMS` - No status parameters provided
- `ERR_DATABASE_ERROR` - Failed to store status

**Notes:**
- Status is stored as a special reading with sensorType "STATUS"
- Stored with timestamp for historical tracking
- Can include any custom status fields

---

### 6. PING

Simple keep-alive check.

**Format:**
```
PING <reference>
```

**Parameters:**
- `reference`: Microcontroller identifier

**Example:**
```
→ PING MC-001
← OK
```

**Responses:**
- `OK` - Server is responsive
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered with server

**Notes:**
- Used to verify connection is still active
- No database operations performed

---

### 7. DISCONNECT

Gracefully disconnect from server.

**Format:**
```
DISCONNECT <reference>
```

**Parameters:**
- `reference`: Microcontroller identifier

**Example:**
```
→ DISCONNECT MC-001
← OK
```

**Responses:**
- `OK` - Disconnect acknowledged, connection will close

**Notes:**
- Server cleanly unregisters the client
- Connection closes after response is sent
- Recommended over abrupt disconnection

---

## Error Codes

All error responses follow the format from `Liste Code.md`:

### Client Errors (4xx)
- `ERR_INVALID_FORMAT` - Malformed request
- `ERR_MISSING_PARAMS` - Required parameters missing
- `ERR_INVALID_VALUE` - Value out of range or wrong type
- `ERR_INVALID_COMMAND` - Unknown command
- `ERR_MALFORMED_REQUEST` - Request corrupted or incomplete
- `ERR_DEVICE_NOT_REGISTERED` - Device not registered with server (must REGISTER first)
- `ERR_DEVICE_NOT_FOUND` - Device ID not in database
- `ERR_SENSOR_NOT_FOUND` - Unsupported sensor type
- `ERR_ALREADY_REGISTERED` - Device already connected

### Server Errors (5xx)
- `ERR_INTERNAL_ERROR` - Generic server error
- `ERR_DATABASE_ERROR` - Database operation failed
- `ERR_API_UNAVAILABLE` - API Node service unavailable

---

## Validation Rules

### Reference Format
- Length: 3-50 characters
- Allowed: alphanumeric, hyphens (-), underscores (_)
- Example: `MC-001`, `BIN_SENSOR_42`, `ESP32_A1B2C3`

### IP Address Format
- Standard IPv4 format: `xxx.xxx.xxx.xxx`
- Each octet: 0-255
- Example: `192.168.1.100`

### Key:Value Pairs
- Format: `key:value` (colon separator, no spaces)
- Key: alphanumeric + underscores
- Value: any string (numeric values auto-parsed)
- Example: `temperature:22.5`, `enabled:true`, `status:online`

---

## Usage Examples

### Typical Session Flow

```
1. Connect to server (TCP)
2. Register microcontroller
   → REGISTER MC-001 192.168.1.100
   ← OK

3. Get current configuration
   → CONFIG_GET MC-001
   ← OK sensorType:BME280 enabled:true samplingInterval:300

4. Send sensor data periodically
   → DATA MC-001 BME280 temperature:22.5 humidity:65.0
   ← OK
   
   [wait samplingInterval seconds]
   
   → DATA MC-001 BME280 temperature:23.1 humidity:62.5
   ← OK

5. Send status occasionally
   → STATUS MC-001 battery:85 uptime:1800
   ← OK

6. Ping to keep alive
   → PING MC-001
   ← OK

7. Gracefully disconnect
   → DISCONNECT MC-001
   ← OK

8. Close connection
```

### Multiple Sensor Types

```
→ REGISTER MC-MULTI 192.168.1.101
← OK

→ DATA MC-MULTI BME280 temperature:22.5 humidity:65.0
← OK

→ DATA MC-MULTI HX711 weight:45.3
← OK

→ DATA MC-MULTI HCSR04 distance:78 fillLevel:65
← OK
```

### Configuration Management

```
→ CONFIG_GET MC-001
← OK sensorType:BME280 enabled:true samplingInterval:300

→ CONFIG_UPDATE MC-001 samplingInterval:600
← OK

→ CONFIG_GET MC-001
← OK sensorType:BME280 enabled:true samplingInterval:600
```

---

## Best Practices

1. **Always REGISTER first** - Cannot send data before registration
2. **Include reference in every command** - Stateless protocol design
3. **Use appropriate sensor types** - Only use supported sensor types
4. **Handle errors gracefully** - Check for ERR_ responses
5. **Ping periodically** - Maintain connection with keep-alive
6. **Send STATUS regularly** - Track device health (battery, memory, etc.)
7. **DISCONNECT before closing** - Clean shutdown
8. **Validate locally** - Check format before sending to reduce errors

---

## Future Extensions

Potential future commands (not yet implemented):

- `BINARY_DATA` - Transfer binary data (images, audio)
- `FIRMWARE_VERSION` - Report firmware version
- `OTA_UPDATE` - Over-the-air firmware update
- `ALERT` - Send critical alerts
- `BATCH_DATA` - Send multiple readings at once