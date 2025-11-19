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
            switch (request.getCommand()) {
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
     * Checks if Microcontrolleur exists in database, validates registration
     */
    private String handleRegister(ProtocolRequest request) {
        String reference = request.getReference();
        String ipAddress = request.getParameter("ipAddress");

        // Check if already connected
        if (server.isClientRegistered(reference)) {
            log("Registration denied: " + reference + " already connected");
            return "ERR_ALREADY_REGISTERED";
        }

        // Check if Microcontrolleur exists in database
        Microcontrolleur mc = dataDriver.findMicrocontrolleurByReference(reference);

        if (mc == null) {
            log("Registration denied: " + reference + " not found in database");
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Update IP address if changed
        if (!ipAddress.equals(mc.getIpAddress())) {
            mc.setIpAddress(ipAddress);
            dataDriver.updateMicrocontrolleur(mc);
            log("Updated IP for " + reference + ": " + ipAddress);
        }

        log("Registration successful: " + reference);
        return "OK";
    }

    /**
     * Handle DATA command
     * Store sensor readings in Releve collection
     */
    private String handleData(ProtocolRequest request) {
        String reference = request.getReference();
        String sensorType = request.getParameter("sensorType");

        // Verify Microcontrolleur is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Microcontrolleur from database
        Microcontrolleur mc = dataDriver.findMicrocontrolleurByReference(reference);

        if (mc == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Verify sensor type matches configuration
        if (mc.getConfigSensor() != null &&
                !sensorType.equals(mc.getConfigSensor().getSensorType())) {
            log("WARNING: Sensor type mismatch for " + reference +
                    ". Expected: " + mc.getConfigSensor().getSensorType() +
                    ", Got: " + sensorType);
            return "ERR_SENSOR_NOT_FOUND";
        }

        // Build sensor reading document
        Document reading = new Document();
        reading.append("sensorType", sensorType);

        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Skip sensorType as it's already added
            if ("sensorType".equals(key)) {
                continue;
            }

            // Try to parse as number, otherwise store as string
            try {
                if (value.contains(".")) {
                    reading.append(key, Double.parseDouble(value));
                } else {
                    reading.append(key, Integer.parseInt(value));
                }
            } catch (NumberFormatException e) {
                reading.append(key, value);
            }
        }

        // Create and store Releve
        Releve releve = new Releve();
        releve.setIdControlleur(mc.getId());
        releve.setDate(new Date());
        releve.setReleve(reading);

        ObjectId releveId = dataDriver.insertReleve(releve);

        if (releveId == null) {
            log("ERROR: Failed to store data for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Data stored for " + reference + " (" + sensorType + "): " + reading.toJson());
        return "OK";
    }

    /**
     * Handle CONFIG_GET command
     * Return current sensor configuration
     */
    private String handleConfigGet(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Microcontrolleur is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Microcontrolleur from database
        Microcontrolleur mc = dataDriver.findMicrocontrolleurByReference(reference);

        if (mc == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        SensorConfig config = mc.getConfigSensor();

        if (config == null) {
            return "OK sensorType:none enabled:false";
        }

        // Build response with config data
        StringBuilder response = new StringBuilder("OK");
        response.append(" sensorType:").append(config.getSensorType());
        response.append(" enabled:").append(config.getEnabled());
        response.append(" samplingInterval:").append(config.getSamplingInterval());

        // Add parameters if available
        if (config.getParameters() != null && !config.getParameters().isEmpty()) {
            for (Map.Entry<String, Object> entry : config.getParameters().entrySet()) {
                response.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
            }
        }

        log("Config sent to " + reference + ": " + response.toString());
        return response.toString();
    }

    /**
     * Handle CONFIG_UPDATE command
     * Update sensor configuration in database
     */
    private String handleConfigUpdate(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Microcontrolleur is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Microcontrolleur from database
        Microcontrolleur mc = dataDriver.findMicrocontrolleurByReference(reference);

        if (mc == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        SensorConfig config = mc.getConfigSensor();

        if (config == null) {
            config = new SensorConfig();
            config.setParameters(new Document());
        }

        // Update configuration fields
        Map<String, String> params = request.getParameters();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "sensorType":
                    config.setSensorType(value);
                    break;

                case "enabled":
                    config.setEnabled(Boolean.parseBoolean(value));
                    break;

                case "samplingInterval":
                    try {
                        config.setSamplingInterval(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        return "ERR_INVALID_VALUE";
                    }
                    break;

                default:
                    // Add to parameters document
                    Document parameters = config.getParameters();
                    if (parameters == null) {
                        parameters = new Document();
                        config.setParameters(parameters);
                    }

                    // Try to parse as number
                    try {
                        if (value.contains(".")) {
                            parameters.append(key, Double.parseDouble(value));
                        } else {
                            parameters.append(key, Integer.parseInt(value));
                        }
                    } catch (NumberFormatException e) {
                        parameters.append(key, value);
                    }
                    break;
            }
        }

        // Update in database
        mc.setConfigSensor(config);
        boolean updated = dataDriver.updateMicrocontrolleur(mc);

        if (!updated) {
            log("ERROR: Failed to update config for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Config updated for " + reference);
        return "OK";
    }

    /**
     * Handle STATUS command
     * Store Microcontrolleur status information
     * Can be used for battery level, uptime, memory, etc.
     */
    private String handleStatus(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Microcontrolleur is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        // Get Microcontrolleur from database
        Microcontrolleur mc = dataDriver.findMicrocontrolleurByReference(reference);

        if (mc == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Build status document
        Document statusDoc = new Document();
        statusDoc.append("timestamp", new Date());

        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Try to parse as number
            try {
                if (value.contains(".")) {
                    statusDoc.append(key, Double.parseDouble(value));
                } else {
                    statusDoc.append(key, Integer.parseInt(value));
                }
            } catch (NumberFormatException e) {
                statusDoc.append(key, value);
            }
        }

        // Store as a special Releve with sensorType "STATUS"
        Releve statusReleve = new Releve();
        statusReleve.setIdControlleur(mc.getId());
        statusReleve.setDate(new Date());

        Document statusReading = new Document();
        statusReading.append("sensorType", "STATUS");
        statusReading.append("status", statusDoc);
        statusReleve.setReleve(statusReading);

        ObjectId releveId = dataDriver.insertReleve(statusReleve);

        if (releveId == null) {
            log("ERROR: Failed to store status for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Status stored for " + reference + ": " + statusDoc.toJson());
        return "OK";
    }

    /**
     * Handle PING command
     * Simple keep-alive response
     */
    private String handlePing(ProtocolRequest request) {
        String reference = request.getReference();

        // Verify Microcontrolleur is registered
        if (!server.isClientRegistered(reference)) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }

        return "OK";
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