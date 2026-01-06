package fr.smart_waste.sapue.protocol;

import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.*;
import fr.smart_waste.sapue.core.SmartWasteServer;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

/**
 * Handles execution of parsed protocol commands
 * Interacts with database and server components
 * Now includes automatic Poubelle lastMeasurement updates
 */
public class CommandHandler {

    private final DataDriver dataDriver;
    private final SmartWasteServer server;

    public CommandHandler(DataDriver dataDriver, SmartWasteServer server) {
        this.dataDriver = dataDriver;
        this.server = server;
    }

    /**
     * Execute a parsed protocol request
     * @param request Parsed ProtocolRequest
     * @return Response string
     */
    public String execute(ProtocolRequest request) {
        try {
            String commandName = request.getCommand().toUpperCase();

            switch (commandName) {
                case "REGISTER":
                    return handleRegister(request);

                case "DATA":
                    return handleData(request);

                case "CONFIG_GET":
                    return handleConfigGet(request);

                case "CONFIG_UPDATE":
                    return handleConfigUpdate(request);

                case "STATUS":
                    return handleStatus(request);

                case "PING":
                    return handlePing(request);

                case "DISCONNECT":
                    return handleDisconnect(request);

                case "HELP":
                    return handleHelp(request);

                default:
                    return "ERR_INVALID_COMMAND";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERR_INTERNAL_ERROR";
        }
    }

    /**
     * Handle REGISTER command
     * Checks if Module exists in database, validates registration
     */
    private String handleRegister(ProtocolRequest request) {
        String reference = request.getReference();
        String ipAddress = request.getParameter("ipAddress");

        // Validate reference format
        if (reference == null || !reference.matches("^[a-zA-Z0-9_-]+$")) {
            return "ERR_INVALID_VALUE";
        }
        
        // Validate IP format
        if (ipAddress == null || !ipAddress.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
             return "ERR_INVALID_VALUE";
        }

        // Check if already connected
        if (server.isClientRegistered(reference)) {
            log("Registration denied: " + reference + " already connected");
            return "ERR_ALREADY_REGISTERED";
        }

        log("Registering device: " + reference + " from IP " + ipAddress);

        // Check if Module exists in database
        Modules module = dataDriver.findModuleByKey(reference);

        if (module == null) {
            log("Registration denied: " + reference + " not found in database");
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Update IP address if changed
        if (!ipAddress.equals(module.getIpAddress())) {
            module.setIpAddress(ipAddress);
            dataDriver.updateModule(module);
            log("Updated IP for " + reference + ": " + ipAddress);
        }

        log("Registration successful: " + reference);
        return "OK";
    }

    /**
     * Handle DATA command
     * Store sensor readings in Releve collection
     * AND update lastMeasurement in Poubelle collection
     */
    private String handleData(ProtocolRequest request) {
        String reference = request.getReference();
        String sensorType = request.getParameter("sensorType");
        
        if (sensorType == null || sensorType.trim().isEmpty()) {
            return "ERR_MISSING_PARAMS";
        }

        // Verify Module is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Module from database
        Modules module = dataDriver.findModuleByKey(reference);
        if (module == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Find Poubelle by module key
        Poubelles poubelle = dataDriver.findPoubelleByModule(reference);
        if (poubelle == null) {
            log("ERROR: No Poubelle found for microcontroller " + reference);
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Note: Module configuration is now more complex with chipsets
        // For backward compatibility, we'll skip sensor type verification
        // TODO: Update this logic to work with Chipsets collection

        // Build measurements object
        Releves.Measurements measurements = new Releves.Measurements();

        // Parse parameters and populate measurements
        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key == null || key.trim().isEmpty()) {
                return "ERR_INVALID_VALUE";
            }

            // Skip sensorType
            if ("sensorType".equals(key)) {
                continue;
            }

            String lowerKey = key.toLowerCase();
            
            // Handle String values
            if (lowerKey.equals("wastetype") || lowerKey.equals("waste_type")) {
                measurements.setWasteType(value);
                continue;
            }

            try {
                Double doubleValue = Double.parseDouble(value);

                switch (lowerKey) {
                    case "filllevel":
                    case "fill_level":
                        measurements.setFillLevel(doubleValue);
                        break;
                    case "weight":
                        measurements.setWeight(doubleValue);
                        break;
                    case "temperature":
                        measurements.setTemperature(doubleValue);
                        break;
                    case "humidity":
                        measurements.setHumidity(doubleValue);
                        break;
                    case "airquality":
                    case "air_quality":
                        measurements.setAirQuality(doubleValue);
                        break;
                    case "batterylevel":
                    case "battery_level":
                    case "battery":
                        measurements.setBatteryLevel(doubleValue);
                        break;
                    case "confidence":
                        measurements.setConfidence(doubleValue);
                        break;
                    default:
                        log("WARNING: Unknown measurement key: " + key);
                        break;
                }
            } catch (NumberFormatException e) {
                log("WARNING: Invalid number format for " + key + ": " + value);
                return "ERR_INVALID_VALUE";
            }
        }

        // Create and store Releve
        Date measurementDate = new Date();
        Releves releve = new Releves();
        releve.setIdPoubelle(poubelle.getId());
        releve.setTimestamp(measurementDate);
        releve.setMeasurements(measurements);

        ObjectId releveId = dataDriver.insertReleve(releve);

        if (releveId == null) {
            log("ERROR: Failed to store data for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Data stored in Releves for " + reference + " (" + sensorType + "): " + measurements);

        // ========== UPDATE POUBELLE lastMeasurement ==========

        // Build document for lastMeasurement
        Document measurementDoc = new Document();
        measurementDoc.append("sensorType", sensorType);

        if (measurements.getFillLevel() != null) {
            measurementDoc.append("fillLevel", measurements.getFillLevel());
        }
        if (measurements.getWeight() != null) {
            measurementDoc.append("weight", measurements.getWeight());
        }
        if (measurements.getTemperature() != null) {
            measurementDoc.append("temperature", measurements.getTemperature());
        }
        if (measurements.getHumidity() != null) {
            measurementDoc.append("humidity", measurements.getHumidity());
        }
        if (measurements.getAirQuality() != null) {
            measurementDoc.append("airQuality", measurements.getAirQuality());
        }
        if (measurements.getBatteryLevel() != null) {
            measurementDoc.append("batteryLevel", measurements.getBatteryLevel());
        }

        // Create LastMeasurement object
        Poubelles.LastMeasurement lastMeasurement = new Poubelles.LastMeasurement();
        lastMeasurement.setDate(measurementDate);
        lastMeasurement.setMeasurement(measurementDoc);

        // Update Poubelle with last measurement
        boolean updated = dataDriver.updateLastMeasurement(poubelle.getId(), lastMeasurement);

        if (updated) {
            log("Updated lastMeasurement in Poubelle for " + reference);
        } else {
            log("WARNING: Failed to update lastMeasurement in Poubelle for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        return "OK";
    }

    /**
     * Handle CONFIG_GET command
     * Return current sensor configuration
     */
    private String handleConfigGet(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Module is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Module from database
        Modules module = dataDriver.findModuleByKey(reference);

        if (module == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Note: Configuration is now managed through Chipsets
        // For backward compatibility, return empty config
        // TODO: Update this to query Chipsets collection
        return "OK sensorType:none enabled:false";
    }

    /**
     * Handle CONFIG_UPDATE command
     * Update sensor configuration in database
     */
    private String handleConfigUpdate(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Module is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Module from database
        Modules module = dataDriver.findModuleByKey(reference);

        if (module == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Note: Configuration is now managed through Chipsets
        // For backward compatibility, we'll skip configuration updates
        // TODO: Update this to modify Chipsets collection
        log("Config update received for " + reference + " (not applied - needs Chipsets integration)");
        return "OK";
    }

    /**
     * Handle STATUS command
     * Store Module status information
     */
    private String handleStatus(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Module is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Module from database
        Modules module = dataDriver.findModuleByKey(reference);
        if (module == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Find Poubelle by module key
        Poubelles poubelle = dataDriver.findPoubelleByModule(reference);
        if (poubelle == null) {
            log("ERROR: No Poubelle found for microcontroller " + reference);
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Build status measurements
        Releves.Measurements statusMeasurements = new Releves.Measurements();

        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                Double doubleValue = Double.parseDouble(value);

                if (key.equalsIgnoreCase("battery") || key.equalsIgnoreCase("batteryLevel")) {
                    statusMeasurements.setBatteryLevel(doubleValue);
                }
            } catch (NumberFormatException e) {
                log("WARNING: Invalid number format for status " + key + ": " + value);
            }
        }

        // Store as a Releve
        Releves statusReleve = new Releves();
        statusReleve.setIdPoubelle(poubelle.getId());
        statusReleve.setTimestamp(new Date());
        statusReleve.setMeasurements(statusMeasurements);

        ObjectId releveId = dataDriver.insertReleve(statusReleve);

        if (releveId == null) {
            log("ERROR: Failed to store status for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Status stored for " + reference + ": " + statusMeasurements);
        return "OK";
    }

    /**
     * Handle PING command
     * Simple keep-alive response
     */
    private String handlePing(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Module is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        return "OK";
    }

    private String handleHelp(ProtocolRequest request) {
        String command = request.getParameters().get("command");

        if (command != null && !command.isEmpty()) {
            switch (command.toUpperCase()) {
                case "REGISTER":
                    return "\nusage: Used to register a microcontroller with the server\nformat: REGISTER [µC] [IP]\nexample: REGISTER MC-001 192.168.1.100\n";
                case "DATA":
                    return "\nusage: Used to add new sensor data to the server collections\nformat: DATA [µC] [SENSOR] [DATA]\nexample: DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25\n";
                case "CONFIG_GET":
                    return "\nusage: Used to retrieve current sensor configuration\nformat: CONFIG_GET [µC]\nexample: CONFIG_GET MC-001\n";
                case "CONFIG_UPDATE":
                    return "\nusage: Used to update sensor configuration\nformat: CONFIG_UPDATE [µC] [CONFIG]\nexample: CONFIG_UPDATE MC-001 samplingInterval:600 enabled:true\n";
                case "STATUS":
                    return "\nusage: Used to send device status information\nformat: STATUS [µC] [STATUS]\nexample: STATUS MC-001 batteryLevel:87 uptime:3600 freeMemory:45000\n";
                case "PING":
                    return "\nusage: Used to see current status of microcontroller\nformat: PING [µC]\nexample: PING MC-001\n";
                case "DISCONNECT":
                    return "\nusage: Used to gracefully disconnect from the server\nformat: DISCONNECT [µC]\nexample: DISCONNECT MC-001\n";
                case "HELP":
                    return "\nusage: Show this help message\nformat: HELP [COMMAND]\nexample: HELP REGISTER\n";
                default:
                    return "\nUnknown command for help: " + command;
            }
        }

        return "\nAvailable commands:\n" +
            "REGISTER - Register the device with the server\n" +
            "DATA - Send sensor data to the server collections\n" +
            "CONFIG_GET - Retrieve current sensor configuration\n" +
            "CONFIG_UPDATE - Update sensor configuration\n" +
            "STATUS - Send device status information\n" +
            "PING - Keep-alive signal\n" +
            "DISCONNECT - Graceful disconnect from server\n" +
            "HELP - Show this help message\n";
    }


    /**
     * Handle DISCONNECT command
     * Graceful disconnect signal
     */
    private String handleDisconnect(ProtocolRequest request) {
        String reference = request.getReference();

        log("Disconnect requested by " + reference);
        return "OK";
    }

    /**
     * Log helper method
     */
    private void log(String message) {
        System.out.println("[CommandHandler] " + message);
    }
}