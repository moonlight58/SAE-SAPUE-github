package fr.smart_waste.sapue.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Smart Waste TCP protocol
 *
 * Protocol Format (space-delimited):
 * - REGISTER <reference> <ipAddress>
 * - DATA <reference> <sensorType> <key>:<value> [<key>:<value> ...]
 * - CONFIG_GET <reference>
 * - CONFIG_UPDATE <reference> <key>:<value> [<key>:<value> ...]
 * - STATUS <reference> <key>:<value> [<key>:<value> ...]
 */
public class ProtocolParser {

    /**
     * Parse raw request string into ProtocolRequest object
     * @param rawRequest Raw request string
     * @return ProtocolRequest object
     * @throws ProtocolException if parsing fails
     */
    public static ProtocolRequest parse(String rawRequest) throws ProtocolException {
        if (rawRequest == null || rawRequest.trim().isEmpty()) {
            throw new ProtocolException("ERR_MALFORMED_REQUEST", "Request is empty");
        }

        String[] parts = rawRequest.trim().split("\\s+");

        if (parts.length == 0) {
            throw new ProtocolException("ERR_MALFORMED_REQUEST", "Request is empty");
        }

        String command = parts[0].toUpperCase();

        // Route to appropriate parser based on command
        switch (command) {
            case "REGISTER":
                return parseRegister(parts, rawRequest);

            case "DATA":
                return parseData(parts, rawRequest);

            case "CONFIG_GET":
                return parseConfigGet(parts, rawRequest);

            case "CONFIG_UPDATE":
                return parseConfigUpdate(parts, rawRequest);

            case "STATUS":
                return parseStatus(parts, rawRequest);

            case "PING":
                return parsePing(parts, rawRequest);

            case "DISCONNECT":
                return parseDisconnect(parts, rawRequest);

            default:
                throw new ProtocolException("ERR_INVALID_COMMAND", "Unknown command: " + command);
        }
    }

    /**
     * Parse REGISTER command
     * Format: REGISTER <reference> <ipAddress>
     */
    private static ProtocolRequest parseRegister(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 3) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "REGISTER requires: reference, ipAddress");
        }

        String reference = parts[1];
        String ipAddress = parts[2];

        // Validate reference
        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        // Validate IP address format
        if (!isValidIpAddress(ipAddress)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid IP address: " + ipAddress);
        }

        Map<String, String> params = new HashMap<>();
        params.put("ipAddress", ipAddress);

        return new ProtocolRequest("REGISTER", reference, params, rawRequest);
    }

    /**
     * Parse DATA command
     * Format: DATA <reference> <sensorType> <key>:<value> [<key>:<value> ...]
     * Example: DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25
     */
    private static ProtocolRequest parseData(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 4) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "DATA requires: reference, sensorType, at least one data pair");
        }

        String reference = parts[1];
        String sensorType = parts[2];

        // Validate reference
        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        // Validate sensor type
        if (!isValidSensorType(sensorType)) {
            throw new ProtocolException("ERR_SENSOR_NOT_FOUND", "Unknown sensor type: " + sensorType);
        }

        // Parse key:value pairs
        Map<String, String> params = new HashMap<>();
        params.put("sensorType", sensorType);

        for (int i = 3; i < parts.length; i++) {
            String pair = parts[i];
            String[] keyValue = pair.split(":", 2);

            if (keyValue.length != 2) {
                throw new ProtocolException("ERR_INVALID_FORMAT", "Invalid data pair format: " + pair + " (expected key:value)");
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.isEmpty() || value.isEmpty()) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Empty key or value in pair: " + pair);
            }

            params.put(key, value);
        }

        return new ProtocolRequest("DATA", reference, params, rawRequest);
    }

    /**
     * Parse CONFIG_GET command
     * Format: CONFIG_GET <reference>
     */
    private static ProtocolRequest parseConfigGet(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 2) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "CONFIG_GET requires: reference");
        }

        String reference = parts[1];

        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        return new ProtocolRequest("CONFIG_GET", reference, new HashMap<>(), rawRequest);
    }

    /**
     * Parse CONFIG_UPDATE command
     * Format: CONFIG_UPDATE <reference> <key>:<value> [<key>:<value> ...]
     * Example: CONFIG_UPDATE MC-001 samplingInterval:600 enabled:true
     */
    private static ProtocolRequest parseConfigUpdate(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 3) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "CONFIG_UPDATE requires: reference, at least one config pair");
        }

        String reference = parts[1];

        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        // Parse key:value pairs
        Map<String, String> params = new HashMap<>();

        for (int i = 2; i < parts.length; i++) {
            String pair = parts[i];
            String[] keyValue = pair.split(":", 2);

            if (keyValue.length != 2) {
                throw new ProtocolException("ERR_INVALID_FORMAT", "Invalid config pair format: " + pair + " (expected key:value)");
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.isEmpty() || value.isEmpty()) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Empty key or value in pair: " + pair);
            }

            params.put(key, value);
        }

        return new ProtocolRequest("CONFIG_UPDATE", reference, params, rawRequest);
    }

    /**
     * Parse STATUS command
     * Format: STATUS <reference> <key>:<value> [<key>:<value> ...]
     * Example: STATUS MC-001 battery:87 uptime:3600 freeMemory:45000
     */
    private static ProtocolRequest parseStatus(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 3) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "STATUS requires: reference, at least one status pair");
        }

        String reference = parts[1];

        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        // Parse key:value pairs
        Map<String, String> params = new HashMap<>();

        for (int i = 2; i < parts.length; i++) {
            String pair = parts[i];
            String[] keyValue = pair.split(":", 2);

            if (keyValue.length != 2) {
                throw new ProtocolException("ERR_INVALID_FORMAT", "Invalid status pair format: " + pair + " (expected key:value)");
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.isEmpty() || value.isEmpty()) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Empty key or value in pair: " + pair);
            }

            params.put(key, value);
        }

        return new ProtocolRequest("STATUS", reference, params, rawRequest);
    }

    /**
     * Parse PING command
     * Format: PING <reference>
     */
    private static ProtocolRequest parsePing(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 2) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "PING requires: reference");
        }

        String reference = parts[1];

        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        return new ProtocolRequest("PING", reference, new HashMap<>(), rawRequest);
    }

    /**
     * Parse DISCONNECT command
     * Format: DISCONNECT <reference>
     */
    private static ProtocolRequest parseDisconnect(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 2) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "DISCONNECT requires: reference");
        }

        String reference = parts[1];

        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        return new ProtocolRequest("DISCONNECT", reference, new HashMap<>(), rawRequest);
    }

    // ========== Validation Helpers ==========

    /**
     * Validate reference format
     * Rules: alphanumeric, hyphens, underscores, 3-50 chars
     */
    private static boolean isValidReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            return false;
        }
        return reference.matches("^[a-zA-Z0-9_-]{3,50}$");
    }

    /**
     * Validate IP address format (simple validation)
     */
    private static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // Simple IPv4 validation
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate sensor type
     * Known sensor types for Smart Waste project
     */
    private static boolean isValidSensorType(String sensorType) {
        if (sensorType == null || sensorType.isEmpty()) {
            return false;
        }

        // List of supported sensor types
        String[] validTypes = {
                "BME280",      // Temperature, humidity, pressure
                "HX711",       // Weight sensor
                "HCSR04",      // Ultrasonic distance/proximity
                "MQ135",       // Air quality sensor
                "REED",        // Reed switch (door open/close)
                "OV2640",      // Camera module
                "DHT22",       // Temperature and humidity
                "BATTERY"      // Battery level
        };

        for (String valid : validTypes) {
            if (valid.equalsIgnoreCase(sensorType)) {
                return true;
            }
        }

        return false;
    }
}