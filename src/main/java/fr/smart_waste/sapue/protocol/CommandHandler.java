package fr.smart_waste.sapue.protocol;

import java.util.List;
import java.util.ArrayList;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.*;
import fr.smart_waste.sapue.core.SmartWasteServer;
import fr.smart_waste.sapue.client.MediaAnalysisClient;
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
    private final MediaAnalysisClient mediaAnalysisClient;

    public CommandHandler(DataDriver dataDriver, SmartWasteServer server, MediaAnalysisClient mediaAnalysisClient) {
        this.dataDriver = dataDriver;
        this.server = server;
        this.mediaAnalysisClient = mediaAnalysisClient;
    }

    /**
     * Execute a parsed protocol request
     * 
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

                case "MEASUREMENT":
                    return handleMeasurement(request);

                case "IMAGE_DATABASE":
                    return handleImageDatabase(request);

                case "IMAGE_UPDATE":
                    return handleImageUpdate(request);

                case "IMAGE_ANALYSE":
                    return handleImageAnalyse(request);

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
     * Format success response according to microcontroller requirements:
     * OK\n[interval]\nNAMES:Temp:Air:Dist:Poids
     * Note: trailing newline is added by ClientHandler.println()
     */
    /**
     * Format success response according to microcontroller requirements:
     * OK\n[interval]\nNAMES:Name1:Name2:...
     */
    private String formatSuccessResponse(Modules module) {
        // Default values
        int samplingInterval = 300;

        // Fetch sampling interval from module config if available
        if (module != null && module.getConfig() != null && module.getConfig().getMeasurementInterval() != null) {
            samplingInterval = module.getConfig().getMeasurementInterval();
        }

        StringBuilder namesBuilder = new StringBuilder("NAMES");

        if (module != null && module.getChipsets() != null && !module.getChipsets().isEmpty()) {
            for (ObjectId chipsetId : module.getChipsets()) {
                Chipsets chipset = dataDriver.findChipsetById(chipsetId);
                if (chipset != null && chipset.getName() != null) {
                    namesBuilder.append(":").append(chipset.getName());
                }
            }
        } else {
            // Fallback if no chipsets found
            namesBuilder.append(":Temp:Air:Dist:Poids");
        }

        return "OK\nSLEEP:" + samplingInterval + "\n" + namesBuilder.toString();
    }

    private String formatSuccessResponseAnalyse(String ColorPoubelle) {
        return "OK\n" + ColorPoubelle + "\n";
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
        return formatSuccessResponse(module);
    }

    /**
     * Handle DATA command
     * Store sensor readings in Releve collection
     * AND update lastMeasurement in Poubelle collection
     */
    private String handleData(ProtocolRequest request) {
        String reference = request.getReference();

        // Get Module from database
        Modules module = dataDriver.findModuleByKey(reference);
        if (module == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Verify Module is registered (relaxed: allow if found in DB)
        if (!server.isClientRegistered(reference)) {
            log("Accepting data from implicitly identified device: " + reference);
        }

        // Find MapPoint by module key
        MapPoints mapPoint = dataDriver.findMapPointByModule(reference);
        if (mapPoint == null) {
            log("ERROR: No MapPoint found for microcontroller " + reference);
            return "ERR_DEVICE_NOT_FOUND";
        }

        if (request.hasMultiSensorData()) {
            log("Processing multi-sensor data for " + reference);
            for (ProtocolRequest.SensorData sensorData : request.getMultiSensorData()) {
                String result = processSensorData(module, mapPoint, sensorData.getSensorType(),
                        sensorData.getParameters());
                if (result.startsWith("ERR")) {
                    return result;
                }
            }
            return formatSuccessResponse(module);
        } else {
            String sensorType = request.getParameter("sensorType");
            if (sensorType == null || sensorType.trim().isEmpty()) {
                return "ERR_MISSING_PARAMS";
            }
            String result = processSensorData(module, mapPoint, sensorType, request.getParameters());
            if (result.startsWith("ERR")) {
                return result;
            }
            return formatSuccessResponse(module);
        }
    }

    /**
     * Process data for a single sensor and update database
     */
    private String processSensorData(Modules module, MapPoints mapPoint, String sensorType,
            Map<String, String> parameters) {
        String reference = module.getKey();

        // Build measurements object
        Measurements.Measurement measurementData = new Measurements.Measurement();

        // Parse parameters and populate measurements
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
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
                measurementData.setWasteType(value);
                continue;
            }

            try {
                Double doubleValue = Double.parseDouble(value);

                switch (lowerKey) {
                    case "filllevel":
                    case "fill_level":
                        measurementData.setFillLevel(doubleValue);
                        break;
                    case "weight":
                        measurementData.setWeight(doubleValue);
                        break;
                    case "temperature":
                        measurementData.setTemperature(doubleValue);
                        break;
                    case "humidity":
                        measurementData.setHumidity(doubleValue);
                        break;
                    case "pressure":
                        measurementData.setPressure(doubleValue);
                        break;
                    case "airquality":
                    case "air_quality":
                    case "airqual":
                        measurementData.setAirQuality(doubleValue);
                        break;
                    case "batterylevel":
                    case "battery_level":
                    case "battery":
                        measurementData.setBatteryLevel(doubleValue);
                        break;
                    case "confidence":
                        measurementData.setConfidence(doubleValue);
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

        // Create and store Measurements
        Date measurementDate = new Date();
        Measurements measurement = new Measurements();
        measurement.setId_Controller(module.getId());
        measurement.setDate(measurementDate);
        measurement.setMeasurement(measurementData);

        ObjectId measurementId = dataDriver.insertMeasurement(measurement);

        if (measurementId == null) {
            log("ERROR: Failed to store data for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Data stored in Measurements for " + reference + " (" + sensorType + "): " + measurementData);

        // ========== UPDATE MapPoint lastMeasurement ==========

        // Build document for lastMeasurement
        Document measurementDoc = new Document();
        measurementDoc.append("sensorType", sensorType);

        if (measurementData.getFillLevel() != null) {
            measurementDoc.append("fillLevel", measurementData.getFillLevel());
        }
        if (measurementData.getWeight() != null) {
            measurementDoc.append("weight", measurementData.getWeight());
        }
        if (measurementData.getTemperature() != null) {
            measurementDoc.append("temperature", measurementData.getTemperature());
        }
        if (measurementData.getHumidity() != null) {
            measurementDoc.append("humidity", measurementData.getHumidity());
        }
        if (measurementData.getAirQuality() != null) {
            measurementDoc.append("airQuality", measurementData.getAirQuality());
        }
        if (measurementData.getBatteryLevel() != null) {
            measurementDoc.append("batteryLevel", measurementData.getBatteryLevel());
        }

        // Create LastMeasurement object
        MapPoints.LastMeasurement lastMeasurement = new MapPoints.LastMeasurement();
        lastMeasurement.setDate(measurementDate);
        lastMeasurement.setMeasurement(measurementDoc);

        // Update MapPoint with last measurement
        boolean updated = dataDriver.addMapPointMeasurement(mapPoint.getId(), lastMeasurement);

        if (updated) {
            log("Updated lastMeasurement in MapPoint for " + reference);
        } else {
            log("WARNING: Failed to update lastMeasurement in MapPoint for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        return formatSuccessResponse(module);
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
        // For backward compatibility, return formatted config
        return formatSuccessResponse(module);
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
        return formatSuccessResponse(module);
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

        // Find MapPoint by module key
        MapPoints mapPoint = dataDriver.findMapPointByModule(reference);
        if (mapPoint == null) {
            log("ERROR: No MapPoint found for microcontroller " + reference);
            return "ERR_DEVICE_NOT_FOUND";
        }

        // Build status measurements
        Measurements.Measurement statusMeasurementData = new Measurements.Measurement();

        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                Double doubleValue = Double.parseDouble(value);

                if (key.equalsIgnoreCase("battery") || key.equalsIgnoreCase("batteryLevel")) {
                    statusMeasurementData.setBatteryLevel(doubleValue);
                }
            } catch (NumberFormatException e) {
                log("WARNING: Invalid number format for status " + key + ": " + value);
            }
        }

        // Store as a Measurement
        Measurements statusMeasurement = new Measurements();
        statusMeasurement.setId_Controller(module.getId());
        statusMeasurement.setDate(new Date());
        statusMeasurement.setMeasurement(statusMeasurementData);

        ObjectId measurementId = dataDriver.insertMeasurement(statusMeasurement);

        if (measurementId == null) {
            log("ERROR: Failed to store status for " + reference);
            return "ERR_DATABASE_ERROR";
        }

        log("Status stored for " + reference + ": " + statusMeasurementData);
        return formatSuccessResponse(module);
    }

    /**
     * Handle MEASUREMENT command
     * Retrieve measurements from database for a specific module or all modules
     * Use '*' as reference to retrieve ALL measurements
     * Optionally filter by date range (startDate and endDate)
     * Format: MEASUREMENT <reference|*> [startDate:<date>] [endDate:<date>]
     */
    private String handleMeasurement(ProtocolRequest request) {
        String reference = request.getReference();
        boolean fetchAllModules = "*".equals(reference);

        log("Retrieving measurements for: " + (fetchAllModules ? "ALL modules" : "module " + reference));

        // Parse date parameters if provided
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        long startDate = 0;
        long endDate = Long.MAX_VALUE;

        // Parse startDate if provided
        if (startDateStr != null && !startDateStr.isEmpty()) {
            try {
                startDate = parseISODateToMillis(startDateStr);
                log("Start date: " + startDateStr + " (" + startDate + " ms)");
            } catch (Exception e) {
                log("ERROR: Invalid startDate format: " + startDateStr);
                return "ERR_INVALID_VALUE";
            }
        }

        // Parse endDate if provided
        if (endDateStr != null && !endDateStr.isEmpty()) {
            try {
                endDate = parseISODateToMillis(endDateStr);
                log("End date: " + endDateStr + " (" + endDate + " ms)");
            } catch (Exception e) {
                log("ERROR: Invalid endDate format: " + endDateStr);
                return "ERR_INVALID_VALUE";
            }
        }

        // Validate date range
        if (startDate > endDate) {
            log("ERROR: startDate cannot be after endDate");
            return "ERR_INVALID_VALUE";
        }

        // Retrieve measurements from database
        try {
            java.util.List<Measurements> measurements;

            if (fetchAllModules) {
                // Fetch ALL measurements within the date range
                log("Fetching ALL measurements in date range");
                measurements = dataDriver.findMeasurementsByDateRange(
                        new Date(startDate),
                        new Date(endDate));
            } else {
                // Fetch measurements for a specific module
                Modules module = dataDriver.findModuleByKey(reference);
                if (module == null) {
                    return "ERR_DEVICE_NOT_FOUND";
                }

                measurements = dataDriver.findMeasurementsByModuleId(
                        module.getId(),
                        new Date(startDate),
                        new Date(endDate));
            }

            if (measurements == null || measurements.isEmpty()) {
                log("No measurements found for: " + (fetchAllModules ? "all modules" : reference));
                return "ERR_NO_DATA";
            }

            log("Found " + measurements.size() + " measurements");

            // Format response: send measurements to media analysis server
            return formatMeasurementsResponse(measurements, reference);

        } catch (Exception e) {
            log("ERROR: Failed to retrieve measurements: " + e.getMessage());
            e.printStackTrace();
            return "ERR_DATABASE_ERROR";
        }
    }

    /**
     * Format measurements response
     * Returns JSON array of measurements for media analysis
     */
    private String formatMeasurementsResponse(java.util.List<Measurements> measurements, String reference) {
        StringBuilder response = new StringBuilder();
        response.append("OK\n");
        response.append("[");

        for (int i = 0; i < measurements.size(); i++) {
            Measurements measurement = measurements.get(i);

            // Create JSON object for each measurement
            response.append("{");
            response.append("\"id\":\"").append(measurement.getId()).append("\",");
            response.append("\"date\":\"").append(measurement.getDate()).append("\",");
            response.append("\"sensorType\":\"").append(measurement.getMeasurement() != null ? "Unknown" : "")
                    .append("\",");

            if (measurement.getMeasurement() != null) {
                Measurements.Measurement m = measurement.getMeasurement();
                response.append("\"data\":{");

                if (m.getFillLevel() != null) {
                    response.append("\"fillLevel\":").append(m.getFillLevel()).append(",");
                }
                if (m.getWeight() != null) {
                    response.append("\"weight\":").append(m.getWeight()).append(",");
                }
                if (m.getTemperature() != null) {
                    response.append("\"temperature\":").append(m.getTemperature()).append(",");
                }
                if (m.getHumidity() != null) {
                    response.append("\"humidity\":").append(m.getHumidity()).append(",");
                }
                if (m.getAirQuality() != null) {
                    response.append("\"airQuality\":").append(m.getAirQuality()).append(",");
                }
                if (m.getBatteryLevel() != null) {
                    response.append("\"batteryLevel\":").append(m.getBatteryLevel()).append(",");
                }
                if (m.getWasteType() != null) {
                    response.append("\"wasteType\":\"").append(m.getWasteType()).append("\",");
                }

                // Remove trailing comma if exists
                if (response.charAt(response.length() - 1) == ',') {
                    response.setLength(response.length() - 1);
                }
                response.append("}");
            }

            response.append("}");

            if (i < measurements.size() - 1) {
                response.append(",");
            }
        }

        response.append("]\n");
        log("Measurements response formatted for " + reference);
        return response.toString();
    }

    /**
     * Parse ISO 8601 date string to milliseconds
     * Supports formats: yyyy-MM-ddTHH:mm:ss or Unix timestamp
     */
    private long parseISODateToMillis(String dateStr) throws Exception {
        // Check if it's a Unix timestamp
        if (dateStr.matches("^\\d{10,13}$")) {
            long timestamp = Long.parseLong(dateStr);
            // If 10 digits, it's in seconds
            if (dateStr.length() == 10) {
                return timestamp * 1000;
            }
            return timestamp;
        }

        // Parse ISO 8601
        String[] parts = dateStr.split("T");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid ISO 8601 format");
        }

        String[] dateParts = parts[0].split("-");
        String[] timeParts = parts[1].split(":");

        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        int second = Integer.parseInt(timeParts[2].split("\\.")[0]);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);

        return cal.getTimeInMillis();
    }

    /**
     * Handle IMAGE DATABASE command
     * Creates a new Report (DepotSauvage) with initial photo
     * Format: IMAGE DATABASE <userId> <longitude>:<latitude> <nb_bboxes>
     * <bbox_data> <image_base64>
     */
    private String handleImageDatabase(ProtocolRequest request) {
        String userId = request.getParameter("userId");
        String longitude = request.getParameter("longitude");
        String latitude = request.getParameter("latitude");
        String nbBboxesStr = request.getParameter("nbBboxes");
        String bboxData = request.getParameter("bboxData");
        String imageBase64 = request.getParameter("imageBase64");

        if (userId == null) {
            log("ERROR: Missing userId for IMAGE DATABASE");
            return "ERR_MISSING_PARAMS";
        }

        if (longitude == null || latitude == null) {
            log("ERROR: Missing longitude or latitude for IMAGE DATABASE");
            return "ERR_MISSING_PARAMS";
        }

        if (nbBboxesStr == null || bboxData == null || imageBase64 == null) {
            log("ERROR: Missing nbBboxes, bboxData or imageBase64 for IMAGE DATABASE");
            return "ERR_MISSING_PARAMS";
        }

        log("Processing IMAGE DATABASE for userId: " + userId);

        try {
            // 1. Find User by ID
            ObjectId userObjectId = new ObjectId(userId);
            Users author = dataDriver.findUserById(userObjectId);

            if (author == null) {
                log("ERROR: User not found: " + userId);
                return "ERR_USER_NOT_FOUND";
            }

            // 2. Parse bounding boxes (flat array)
            List<Double> coordinates = new ArrayList<>();
            int nbBboxes = Integer.parseInt(nbBboxesStr);

            if (nbBboxes > 0) {
                String[] bboxes = bboxData.split(";");
                for (String bbox : bboxes) {
                    String[] coords = bbox.split(",");
                    for (String coord : coords) {
                        coordinates.add(Double.parseDouble(coord));
                    }
                }
            }

            // 3. Create Reports.UserInfo for author
            Reports.UserInfo authorInfo = new Reports.UserInfo();
            authorInfo.setIdUser(author.getId());
            authorInfo.setName(author.getName());
            authorInfo.setRole(author.getRole());

            // 4. Create Reports.Location
            Reports.Location location = new Reports.Location();
            List<Double> coords = new ArrayList<>();
            coords.add(Double.parseDouble(longitude));
            coords.add(Double.parseDouble(latitude));
            location.setCoordinates(coords);

            // 5. Create Reports.Photo with coordinates
            Reports.Photo photo = new Reports.Photo();
            photo.setInitialPhoto(imageBase64);
            photo.setCoordinates(coordinates);
            photo.setFinalPhoto(null);

            // 6. Create initial history entry
            Reports.HistoryEntry initialHistory = new Reports.HistoryEntry();
            initialHistory.setDate(new Date());
            initialHistory.setStatus("Ouvert");
            initialHistory.setByUser(author.getId());

            List<Reports.HistoryEntry> history = new ArrayList<>();
            history.add(initialHistory);

            // 7. Create Reports document
            Reports report = new Reports();
            report.setAuthor(authorInfo);
            report.setCleaner(null);
            report.setStatus("Ouvert");
            report.setMapPoint(null); // Depot sauvage = no mapPoint
            report.setType("DepotSauvage");
            report.setIssueType(null); // Only for poubelles
            report.setLocation(location);
            report.setPhoto(photo);
            report.setHistory(history);

            // 8. Insert into database
            ObjectId reportId = dataDriver.insertReport(report);

            if (reportId == null) {
                log("ERROR: Failed to insert Report");
                return "ERR_DATABASE_ERROR";
            }

            log("Report created successfully: " + reportId);
            return formatSuccessResponse(null);

        } catch (IllegalArgumentException e) {
            log("ERROR: Invalid ObjectId format: " + e.getMessage());
            return "ERR_INVALID_VALUE";
        } catch (Exception e) {
            log("ERROR: Exception in handleImageDatabase: " + e.getMessage());
            e.printStackTrace();
            return "ERR_INTERNAL_ERROR";
        }
    }

    /**
     * Handle IMAGE UPDATE command
     * Updates an existing Report with final photo (cleaner confirmation)
     * Format: IMAGE UPDATE <cleanerId> <reportId> <image_base64>
     */
    private String handleImageUpdate(ProtocolRequest request) {
        String cleanerId = request.getParameter("cleanerId");
        String reportId = request.getParameter("reportId");
        String imageBase64 = request.getParameter("imageBase64");

        log("Processing IMAGE UPDATE for reportId: " + reportId + " by cleanerId: " + cleanerId);

        try {
            // 1. Find Cleaner by ID
            ObjectId cleanerObjectId = new ObjectId(cleanerId);
            Users cleaner = dataDriver.findUserById(cleanerObjectId);

            if (cleaner == null) {
                log("ERROR: Cleaner not found: " + cleanerId);
                return "ERR_USER_NOT_FOUND";
            }

            // 2. Find Report by ID
            ObjectId reportObjectId = new ObjectId(reportId);
            Reports report = dataDriver.findReportById(reportObjectId);

            if (report == null) {
                log("ERROR: Report not found: " + reportId);
                return "ERR_REPORT_NOT_FOUND";
            }

            // 3. Create Reports.UserInfo for cleaner
            Reports.UserInfo cleanerInfo = new Reports.UserInfo();
            cleanerInfo.setIdUser(cleaner.getId());
            cleanerInfo.setName(cleaner.getName());
            cleanerInfo.setRole(cleaner.getRole());

            // 4. Update report fields
            report.setCleaner(cleanerInfo);
            report.setStatus("Resolu");

            // 5. Update photo.finalPhoto
            if (report.getPhoto() == null) {
                report.setPhoto(new Reports.Photo());
            }
            report.getPhoto().setFinalPhoto(imageBase64);

            // 6. Add history entry
            Reports.HistoryEntry historyEntry = new Reports.HistoryEntry();
            historyEntry.setDate(new Date());
            historyEntry.setStatus("Resolu");
            historyEntry.setByUser(cleaner.getId());

            if (report.getHistory() == null) {
                report.setHistory(new ArrayList<>());
            }
            report.getHistory().add(historyEntry);

            // 7. Update in database
            boolean updated = dataDriver.updateReport(report);

            if (!updated) {
                log("ERROR: Failed to update Report");
                return "ERR_DATABASE_ERROR";
            }

            log("Report updated successfully: " + reportId);
            return formatSuccessResponse(null);

        } catch (IllegalArgumentException e) {
            log("ERROR: Invalid ObjectId format: " + e.getMessage());
            return "ERR_INVALID_VALUE";
        } catch (Exception e) {
            log("ERROR: Exception in handleImageUpdate: " + e.getMessage());
            e.printStackTrace();
            return "ERR_INTERNAL_ERROR";
        }
    }

    /**
     * Handle IMAGE ANALYSE command
     * Sends image to external analysis service
     * Format: IMAGE ANALYSE <reference> <image_base64>
     */
    private String handleImageAnalyse(ProtocolRequest request) {
        String reference = request.getReference();
        String imageBase64 = request.getParameter("imageBase64");

        if (imageBase64 == null || imageBase64.isEmpty()) {
            log("ERROR: Missing image data for IMAGE ANALYSE");
            return "ERR_MISSING_PARAMS";
        }

        // Get Module from database
        Modules module = dataDriver.findModuleByKey(reference);
        if (module == null) {
            return "ERR_DEVICE_NOT_FOUND";
        }

        log("Performing image analysis for reference: " + reference);
        String analysisResult = mediaAnalysisClient.analyzeImage(imageBase64);
        log("Image analysis result: " + analysisResult);

        if (analysisResult == null || analysisResult.isEmpty()) {
            log("ERROR: Image analysis failed");
            return "ERR_ANALYSIS_FAILED";
        }

        // Extract bin color from analysis result
        // Expected format: "OK JAUNE", "OK VERTE", etc.
        String binColor = extractColorFromAnalysisResult(analysisResult);
        if (binColor == null || binColor.isEmpty()) {
            log("ERROR: Could not extract color from analysis result: " + analysisResult);
            return "ERR_INVALID_ANALYSIS_RESULT";
        }

        log("Detected bin color: " + binColor);

        // Get hexadecimal image for the bin color
        String hexImage = getBinHexImage(module, binColor);
        if (hexImage.isEmpty()) {
            log("WARNING: Could not retrieve hex image for color " + binColor);
            return "ERR_HEX_IMAGE_NOT_FOUND";
        }

        // Get latest distance measurement
        String distance = getConfigDistance(module);

        // Build response: OK\n + couleur\n + distance\n + icon hexadécimale\n
        StringBuilder response = new StringBuilder();
        response.append("OK\n");
        response.append(binColor).append("\n");
        response.append(distance).append("\n");
        response.append(hexImage).append("\n");

        log("Image analysis successful. Response ready for " + reference);
        return response.toString();
    }

    /**
     * Extract bin color from media analysis result
     * Expected format: "OK JAUNE", "OK VERTE", "JAUNE", "VERTE", etc.
     * 
     * @param analysisResult The result string from media analysis
     * @return The color name or null if not found
     */
    private String extractColorFromAnalysisResult(String analysisResult) {
        if (analysisResult == null || analysisResult.isEmpty()) {
            return null;
        }

        String result = analysisResult.toUpperCase().trim();

        // Remove "OK" prefix if present
        if (result.startsWith("OK ")) {
            result = result.substring(3).trim();
        }

        // Validate that it's one of the known colors
        if (result.equals("JAUNE") || result.equals("VERTE") ||
                result.equals("MARRON") || result.equals("BRUN") || result.equals("BROWN") ||
                result.equals("GRISE") || result.equals("GRIS") ||
                result.equals("GREY") || result.equals("GRAY")) {
            return result;
        }

        return null;
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

        Modules module = dataDriver.findModuleByKey(reference);
        return formatSuccessResponse(module);
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
        Modules module = dataDriver.findModuleByKey(reference);
        return formatSuccessResponse(module);
    }

    /**
     * Retrieve hexadecimal image for a bin color from OLED Chipset configuration
     * 
     * @param module   The module containing the chipsets
     * @param binColor The color of the bin (JAUNE, VERTE, MARRON, GRISE)
     * @return Hexadecimal image string or empty string if not found
     */
    private String getBinHexImage(Modules module, String binColor) {
        if (module == null || binColor == null || binColor.isEmpty()) {
            return "";
        }

        try {
            // Find all chipsets for this module
            java.util.List<Chipsets> chipsets = dataDriver.findChipsetsByModuleId(module.getId());

            if (chipsets == null || chipsets.isEmpty()) {
                log("WARNING: No chipsets found for module " + module.getKey());
                return "";
            }

            // Look for OLED display chipset (contains Display capability)
            for (Chipsets chipset : chipsets) {
                if (chipset.getCaps() != null && chipset.getCaps().contains("Display")) {
                    Document config = chipset.getConfig();
                    if (config == null) {
                        log("WARNING: OLED chipset has no config");
                        continue;
                    }

                    // Map color to config key
                    String colorKey = mapColorToConfigKey(binColor);
                    if (colorKey == null) {
                        log("WARNING: Unknown bin color: " + binColor);
                        return "";
                    }

                    // Extract hexadecimal image
                    String hexImage = config.getString(colorKey);
                    if (hexImage != null && !hexImage.isEmpty()) {
                        log("Found hex image for color " + binColor);
                        return hexImage;
                    }
                }
            }

            log("WARNING: No OLED display chipset found with color " + binColor);
            return "";
        } catch (Exception e) {
            log("ERROR: Failed to retrieve hex image: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Map bin color name to configuration key
     * 
     * @param binColor The color name returned by media analysis (e.g., "JAUNE",
     *                 "VERTE")
     * @return The configuration key (e.g., "yellow_bin", "green_bin")
     */
    private String mapColorToConfigKey(String binColor) {
        if (binColor == null) {
            return null;
        }

        String colorUpper = binColor.toUpperCase().trim();

        switch (colorUpper) {
            case "JAUNE":
                return "yellow_bin";
            case "VERTE":
                return "green_bin";
            case "MARRON":
            case "BRUN":
            case "BROWN":
                return "brown_bin";
            case "GRISE":
            case "GRIS":
            case "GREY":
            case "GRAY":
                return "grey_bin";
            default:
                return null;
        }
    }

    /**
     * Get the config detection distance of the chipset camera HC-SR04 sensor
     * 
     * @param the module µC name
     * @return The config detection distance as string, or empty string if not found
     */
    private String getConfigDistance(Modules module) {
        if (module == null) {
            return "";
        }

        try {
            // Find all chipsets for this module
            java.util.List<Chipsets> chipsets = dataDriver.findChipsetsByModuleId(module.getId());

            if (chipsets == null || chipsets.isEmpty()) {
                log("WARNING: No chipsets found for module " + module.getKey());
                return "";
            }

            // Look for HC-SR04 chipset (contains DistanceSensor capability)
            for (Chipsets chipset : chipsets) {
                if (chipset.getCaps() != null && chipset.getCaps().contains("DistanceSensor")) {
                    Document config = chipset.getConfig();
                    if (config == null) {
                        log("WARNING: HC-SR04 chipset has no config");
                        continue;
                    }

                    Double distance = config.getDouble("detection_distance");
                    if (distance != null) {
                        log("Found detection distance: " + distance);
                        return distance.toString();
                    }
                }
            }

            log("WARNING: No HC-SR04 chipset found for module " + module.getKey());
            return "";
        } catch (Exception e) {
            log("ERROR: Failed to retrieve detection distance: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Log helper method
     */
    private void log(String message) {
        System.out.println("[CommandHandler] " + message);
    }
}