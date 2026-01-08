package fr.smart_waste.sapue.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Smart Waste TCP protocol
 *
 * Protocol Format (space-delimited):
 * - REGISTER <reference> <ipAddress>
 * - DATA <reference> <sensorType> <key>:<value> [<key>:<value> ...]
 * - CONFIG_GET <reference>
 * - CONFIG_UPDATE <reference> <key>:<value> [<key>:<value> ...]
 * - STATUS <reference> <key>:<value> [<key>:<value> ...]
 * - MEASUREMENT <reference> [startDate:<date>] [endDate:<date>]
 * - IMAGE DATABASE <userId> <longitude>:<latitude> <nb_bboxes> <bbox_data> <image_base64>
 * - IMAGE UPDATE <cleanerId> <reportId> <image_base64>
 * - IMAGE ANALYSE <image_base64>
 * - PING <reference>
 * - HELP [<command>]
 * - DISCONNECT <reference>
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

            case "MEASUREMENT":
                return parseMeasurement(parts, rawRequest);

            case "IMAGE":
                // Check subcommand (DATABASE or UPDATE)
                if (parts.length < 2) {
                    throw new ProtocolException("ERR_MISSING_PARAMS", "IMAGE requires subcommand (DATABASE or UPDATE)");
                }
                String subCommand = parts[1].toUpperCase();
                if ("DATABASE".equals(subCommand)) {
                    return parseImageDatabase(parts, rawRequest);
                } else if ("UPDATE".equals(subCommand)) {
                    return parseImageUpdate(parts, rawRequest);
                } else if ("ANALYSE".equals(subCommand)) {
                    return parseImageAnalyse(parts, rawRequest);
                } else {
                    throw new ProtocolException("ERR_INVALID_COMMAND", "Unknown IMAGE subcommand: " + subCommand);
                }

            case "HELP":
                return parseHelp(parts, rawRequest);

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
        if (parts.length < 3) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "DATA requires: reference, and either sensorType + pairs OR sensor dictionaries");
        }

        String reference = parts[1];

        // Validate reference
        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        // Check if it's the new multi-dictionary format
        if (rawRequest.contains("{")) {
            return parseMultiSensorData(reference, rawRequest);
        }

        // Traditional format: DATA <reference> <sensorType> <key>:<value> ...
        if (parts.length < 4) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "Traditional DATA requires: sensorType, at least one data pair");
        }

        String sensorType = parts[2];

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
     * Parse multi-sensor DATA format
     * Format: DATA <reference> {"Sensor": "key":val "key":val} {...}
     */
    private static ProtocolRequest parseMultiSensorData(String reference, String rawRequest) throws ProtocolException {
        List<ProtocolRequest.SensorData> multiSensorData = new ArrayList<>();
        
        // Find all blocks within curly braces
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(rawRequest);
        
        while (matcher.find()) {
            String content = matcher.group(1).trim();
            if (content.isEmpty()) continue;
            
            // Expected format: "SensorType": "key":val "key":val ...
            // or maybe just: "SensorType": key:val key:val ...
            // The user's example: {"BME280": "temperature":22.5 "humidity":65.0}
            
            int firstColon = content.indexOf(':');
            if (firstColon == -1) {
                throw new ProtocolException("ERR_INVALID_FORMAT", "Invalid sensor block format: " + content + " (expected SensorType: data)");
            }
            
            String sensorType = content.substring(0, firstColon).trim();
            // Remove quotes if present
            if (sensorType.startsWith("\"") && sensorType.endsWith("\"") && sensorType.length() > 1) {
                sensorType = sensorType.substring(1, sensorType.length() - 1);
            }
            
            if (!isValidSensorType(sensorType)) {
                 throw new ProtocolException("ERR_SENSOR_NOT_FOUND", "Unknown sensor type in block: " + sensorType);
            }
            
            String dataPart = content.substring(firstColon + 1).trim();
            Map<String, String> params = new HashMap<>();
            
            // Split dataPart by space, but handle potential quotes?
            // For now assume spaces between key:value pairs as in example
            String[] pairs = dataPart.split("\\s+");
            for (String pair : pairs) {
                if (pair.isEmpty()) continue;
                
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) {
                    // Could be a quoted string with spaces, but protocol is usually simple
                    continue; 
                }
                
                String key = keyValue[0].trim();
                if (key.startsWith("\"") && key.endsWith("\"") && key.length() > 1) key = key.substring(1, key.length() - 1);
                
                String value = keyValue[1].trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) value = value.substring(1, value.length() - 1);
                
                if (!key.isEmpty() && !value.isEmpty()) {
                    params.put(key, value);
                }
            }
            
            if (!params.isEmpty()) {
                multiSensorData.add(new ProtocolRequest.SensorData(sensorType, params));
            }
        }
        
        if (multiSensorData.isEmpty()) {
             throw new ProtocolException("ERR_INVALID_FORMAT", "No valid sensor data found in blocks");
        }
        
        return new ProtocolRequest("DATA", reference, new HashMap<>(), multiSensorData, rawRequest);
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
     * Parse MEASUREMENT command
     * Format: MEASUREMENT <reference> [startDate:<date>] [endDate:<date>]
     * Dates format: ISO 8601 (yyyy-MM-ddTHH:mm:ss) or Unix timestamp (milliseconds)
     * 
     * Examples:
     * - MEASUREMENT MC-001 (all measurements)
     * - MEASUREMENT MC-001 startDate:2026-01-01T00:00:00 endDate:2026-01-31T23:59:59
     * - MEASUREMENT MC-001 startDate:1735689600000 endDate:1738368000000
     */
    private static ProtocolRequest parseMeasurement(String[] parts, String rawRequest) throws ProtocolException {
        if (parts.length < 2) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "MEASUREMENT requires: reference");
        }

        String reference = parts[1];

        // Validate reference
        if (!isValidReference(reference)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reference format: " + reference);
        }

        // Parse optional date parameters
        Map<String, String> params = new HashMap<>();
        String startDate = null;
        String endDate = null;

        for (int i = 2; i < parts.length; i++) {
            String part = parts[i];
            
            if (part.startsWith("startDate:")) {
                startDate = part.substring("startDate:".length()).trim();
                if (startDate.isEmpty()) {
                    throw new ProtocolException("ERR_INVALID_VALUE", "startDate cannot be empty");
                }
                // Validate date format
                if (!isValidDateFormat(startDate)) {
                    throw new ProtocolException("ERR_INVALID_VALUE", "Invalid startDate format: " + startDate + " (expected ISO 8601 or Unix timestamp)");
                }
                params.put("startDate", startDate);
            } else if (part.startsWith("endDate:")) {
                endDate = part.substring("endDate:".length()).trim();
                if (endDate.isEmpty()) {
                    throw new ProtocolException("ERR_INVALID_VALUE", "endDate cannot be empty");
                }
                // Validate date format
                if (!isValidDateFormat(endDate)) {
                    throw new ProtocolException("ERR_INVALID_VALUE", "Invalid endDate format: " + endDate + " (expected ISO 8601 or Unix timestamp)");
                }
                params.put("endDate", endDate);
            } else {
                throw new ProtocolException("ERR_INVALID_FORMAT", "Unknown parameter: " + part + " (expected startDate: or endDate:)");
            }
        }

        // Validate date range if both are provided
        if (startDate != null && endDate != null) {
            long startTime = parseDateToMillis(startDate);
            long endTime = parseDateToMillis(endDate);
            
            if (startTime > endTime) {
                throw new ProtocolException("ERR_INVALID_VALUE", "startDate must be before endDate");
            }
        }

        return new ProtocolRequest("MEASUREMENT", reference, params, rawRequest);
    }

    /**
     * Validate date format (ISO 8601 or Unix timestamp)
     */
    private static boolean isValidDateFormat(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }

        // Check if it's a Unix timestamp (all digits, 10-13 digits)
        if (dateStr.matches("^\\d{10,13}$")) {
            return true;
        }

        // Check if it's ISO 8601 format (yyyy-MM-ddTHH:mm:ss)
        if (dateStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$")) {
            return true;
        }

        // Check if it's ISO 8601 with milliseconds (yyyy-MM-ddTHH:mm:ss.SSS)
        if (dateStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}$")) {
            return true;
        }

        return false;
    }

    /**
     * Parse date string to milliseconds
     * Supports ISO 8601 and Unix timestamp formats
     */
    private static long parseDateToMillis(String dateStr) throws ProtocolException {
        // If it's already a Unix timestamp
        if (dateStr.matches("^\\d{10,13}$")) {
            try {
                long timestamp = Long.parseLong(dateStr);
                // If 10 digits, it's in seconds, convert to millis
                if (dateStr.length() == 10) {
                    return timestamp * 1000;
                }
                return timestamp;
            } catch (NumberFormatException e) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Invalid timestamp: " + dateStr);
            }
        }

        // If it's ISO 8601
        try {
            // Parse ISO 8601 string to Date
            // Format: yyyy-MM-ddTHH:mm:ss or yyyy-MM-ddTHH:mm:ss.SSS
            String[] parts = dateStr.split("T");
            if (parts.length != 2) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Invalid ISO 8601 format: " + dateStr);
            }

            String[] dateParts = parts[0].split("-");
            String[] timeParts = parts[1].split(":");

            if (dateParts.length != 3 || timeParts.length != 3) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Invalid ISO 8601 format: " + dateStr);
            }

            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            String[] secondsMillis = timeParts[2].split("\\.");
            int second = Integer.parseInt(secondsMillis[0]);
            int millis = 0;
            if (secondsMillis.length > 1) {
                millis = Integer.parseInt(secondsMillis[1]);
            }

            // Simple validation
            if (month < 1 || month > 12 || day < 1 || day > 31 || 
                hour < 0 || hour > 23 || minute < 0 || minute > 59 || 
                second < 0 || second > 59) {
                throw new ProtocolException("ERR_INVALID_VALUE", "Invalid date/time values: " + dateStr);
            }

            // Create calendar and convert to millis
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month - 1, day, hour, minute, second);
            cal.set(java.util.Calendar.MILLISECOND, millis);

            return cal.getTimeInMillis();
        } catch (NumberFormatException e) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid ISO 8601 date: " + dateStr);
        }
    }

    /**
     * Parse IMAGE DATABASE command
     * Format: IMAGE DATABASE <userId> <longitude>:<latitude> <nb_bboxes> <bbox_data> <image_base64>
     * bbox_data format: x1,y1,x2,y2;x1,y1,x2,y2;...
     * 
     * Example: IMAGE DATABASE 507f1f77bcf86cd799439011 6.0240:47.2378 2 100,150,200,250;300,350,400,450 /9j/4AAQ...
     */
    private static ProtocolRequest parseImageDatabase(String[] parts, String rawRequest) throws ProtocolException {
        // IMAGE DATABASE <userId> <longitude>:<latitude> <nb_bboxes> <bbox_data> <image_base64>
        // parts[0] = IMAGE
        // parts[1] = DATABASE
        // parts[2] = userId
        // parts[3] = longitude:latitude
        // parts[4] = nb_bboxes
        // parts[5] = bbox_data
        // parts[6...] = image_base64 (peut contenir des espaces)
        
        if (parts.length < 7) {
            throw new ProtocolException("ERR_MISSING_PARAMS", 
                "IMAGE DATABASE requires: userId, longitude:latitude, nb_bboxes, bbox_data, image_base64");
        }

        String userId = parts[2];
        String coordsStr = parts[3];
        String nbBboxesStr = parts[4];
        String bboxData = parts[5];
        
        // Reconstruct image_base64 (can span multiple parts if contains spaces)
        StringBuilder imageBase64 = new StringBuilder();
        for (int i = 6; i < parts.length; i++) {
            if (i > 6) imageBase64.append(" ");
            imageBase64.append(parts[i]);
        }
        
        // Validate userId format (ObjectId = 24 hex chars)
        if (!isValidObjectId(userId)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid userId format: " + userId);
        }
        
        // Parse coordinates
        String[] coords = coordsStr.split(":");
        if (coords.length != 2) {
            throw new ProtocolException("ERR_INVALID_FORMAT", "Invalid coordinates format: " + coordsStr + " (expected longitude:latitude)");
        }
        
        double longitude, latitude;
        try {
            longitude = Double.parseDouble(coords[0]);
            latitude = Double.parseDouble(coords[1]);
        } catch (NumberFormatException e) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid coordinate values: " + coordsStr);
        }
        
        // Parse nb_bboxes
        int nbBboxes;
        try {
            nbBboxes = Integer.parseInt(nbBboxesStr);
            if (nbBboxes < 0) {
                throw new ProtocolException("ERR_INVALID_VALUE", "nb_bboxes must be >= 0");
            }
        } catch (NumberFormatException e) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid nb_bboxes: " + nbBboxesStr);
        }
        
        // Validate bbox_data format
        if (nbBboxes > 0) {
            String[] bboxes = bboxData.split(";");
            if (bboxes.length != nbBboxes) {
                throw new ProtocolException("ERR_INVALID_FORMAT", 
                    "bbox_data mismatch: expected " + nbBboxes + " bboxes, got " + bboxes.length);
            }
            
            // Validate each bbox format (x1,y1,x2,y2)
            for (String bbox : bboxes) {
                String[] coords_bbox = bbox.split(",");
                if (coords_bbox.length != 4) {
                    throw new ProtocolException("ERR_INVALID_FORMAT", 
                        "Invalid bbox format: " + bbox + " (expected x1,y1,x2,y2)");
                }
                // Validate that all are valid doubles
                try {
                    for (String coord : coords_bbox) {
                        Double.parseDouble(coord);
                    }
                } catch (NumberFormatException e) {
                    throw new ProtocolException("ERR_INVALID_VALUE", "Invalid bbox coordinates: " + bbox);
                }
            }
        }
        
        // Validate base64 (basic check - not empty and valid chars)
        String imageBase64Str = imageBase64.toString().trim();
        if (imageBase64Str.isEmpty()) {
            throw new ProtocolException("ERR_INVALID_VALUE", "image_base64 is empty");
        }
        
        // Store in parameters
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        params.put("nbBboxes", String.valueOf(nbBboxes));
        params.put("bboxData", bboxData);
        params.put("imageBase64", imageBase64Str);
        
        return new ProtocolRequest("IMAGE_DATABASE", "", params, rawRequest);
    }

    /**
     * Parse IMAGE UPDATE command
     * Format: IMAGE UPDATE <cleanerId> <reportId> <image_base64>
     * 
     * Example: IMAGE UPDATE 507f1f77bcf86cd799439011 507f191e810c19729de860ea /9j/4AAQ...
     */
    private static ProtocolRequest parseImageUpdate(String[] parts, String rawRequest) throws ProtocolException {
        // IMAGE UPDATE <cleanerId> <reportId> <image_base64>
        // parts[0] = IMAGE
        // parts[1] = UPDATE
        // parts[2] = cleanerId
        // parts[3] = reportId
        // parts[4...] = image_base64 (peut contenir des espaces)
        
        if (parts.length < 5) {
            throw new ProtocolException("ERR_MISSING_PARAMS", 
                "IMAGE UPDATE requires: cleanerId, reportId, image_base64");
        }

        String cleanerId = parts[2];
        String reportId = parts[3];
        
        // Reconstruct image_base64
        StringBuilder imageBase64 = new StringBuilder();
        for (int i = 4; i < parts.length; i++) {
            if (i > 4) imageBase64.append(" ");
            imageBase64.append(parts[i]);
        }
        
        // Validate cleanerId format (ObjectId)
        if (!isValidObjectId(cleanerId)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid cleanerId format: " + cleanerId);
        }
        
        // Validate reportId format (ObjectId)
        if (!isValidObjectId(reportId)) {
            throw new ProtocolException("ERR_INVALID_VALUE", "Invalid reportId format: " + reportId);
        }
        
        // Validate base64
        String imageBase64Str = imageBase64.toString().trim();
        if (imageBase64Str.isEmpty()) {
            throw new ProtocolException("ERR_INVALID_VALUE", "image_base64 is empty");
        }
        
        // Store in parameters
        Map<String, String> params = new HashMap<>();
        params.put("cleanerId", cleanerId);
        params.put("reportId", reportId);
        params.put("imageBase64", imageBase64Str);
        
        return new ProtocolRequest("IMAGE_UPDATE", "", params, rawRequest);
    }

    /**
     * Parse IMAGE ANALYSE command
     * Format: IMAGE ANALYSE <image_base64>
     * 
     * Example: IMAGE ANALYSE /9j/4AAQ...
     */
    private static ProtocolRequest parseImageAnalyse(String[] parts, String rawRequest) throws ProtocolException {
        // IMAGE ANALYSE <image_base64>
        // parts[0] = IMAGE
        // parts[1] = ANALYSE
        // parts[2...] = image_base64 (may contain spaces)
        
        if (parts.length < 3) {
            throw new ProtocolException("ERR_MISSING_PARAMS", 
                "IMAGE ANALYSE requires: image_base64");
        }
        
        // Reconstruct image_base64 from all parts after 'ANALYSE'
        StringBuilder imageBase64 = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            if (i > 2) imageBase64.append(" ");
            imageBase64.append(parts[i]);
        }
        
        // Validate base64
        String imageBase64Str = imageBase64.toString().trim();
        if (imageBase64Str.isEmpty()) {
            throw new ProtocolException("ERR_MISSING_PARAMS", "image_base64 is empty");
        }
        
        // Store in parameters
        Map<String, String> params = new HashMap<>();
        params.put("imageBase64", imageBase64Str);
        
        // Reference is not provided in command, use empty string
        return new ProtocolRequest("IMAGE_ANALYSE", "", params, rawRequest);
    }

    /**
     * Validate ObjectId format (24 hexadecimal characters)
     */
    private static boolean isValidObjectId(String objectId) {
        if (objectId == null || objectId.length() != 24) {
            return false;
        }
        return objectId.matches("^[a-fA-F0-9]{24}$");
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
     * Parse HELP command
     * Format: HELP
     */
    private static ProtocolRequest parseHelp(String[] parts, String rawRequest) throws ProtocolException {
        // HELP command do not take more than 1 argument (ex: HELP DATA is valid)
        if (parts.length > 2) {
            throw new ProtocolException("ERR_MALFORMED_REQUEST", "HELP command takes at most one argument");
        }
        String command = parts.length == 2 ? parts[1] : "";
        Map<String, String> params = new HashMap<>();
        params.put("command", command);
        return new ProtocolRequest("HELP", "", params, rawRequest);
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