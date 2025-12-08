package fr.smart_waste.sapue.protocol;

import fr.smart_waste.sapue.dataaccess.MongoDataDriver;
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

    private final MongoDataDriver dataDriver;
    private final SmartWasteServer server;

    public CommandHandler(MongoDataDriver dataDriver, SmartWasteServer server) {
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

        log("Registering device: " + reference + " from IP " + ipAddress);

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
     * AND update lastMeasurement in Poubelle collection
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

        // Find Poubelle by microcontroller reference
        Poubelles poubelle = dataDriver.findPoubelleByMicrocontroller(reference);
        if (poubelle == null) {
            log("ERROR: No Poubelle found for microcontroller " + reference);
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

        // Build measurements object
        Releves.Measurements measurements = new Releves.Measurements();

        // Parse parameters and populate measurements
        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Skip sensorType
            if ("sensorType".equals(key)) {
                continue;
            }

            try {
                Double doubleValue = Double.parseDouble(value);

                switch (key.toLowerCase()) {
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
        }

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

        // Find Poubelle by microcontroller reference
        Poubelles poubelle = dataDriver.findPoubelleByMicrocontroller(reference);
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