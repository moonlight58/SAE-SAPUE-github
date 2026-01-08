package fr.smart_waste.sapue.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a parsed protocol request
 */
public class ProtocolRequest {

    private final String command;
    private final String reference;
    private final Map<String, String> parameters;
    private final List<SensorData> multiSensorData;
    private final String rawRequest;

    /**
     * Represents data for a single sensor
     */
    public static class SensorData {
        private final String sensorType;
        private final Map<String, String> parameters;

        public SensorData(String sensorType, Map<String, String> parameters) {
            this.sensorType = sensorType;
            this.parameters = parameters != null ? parameters : new HashMap<>();
        }

        public String getSensorType() {
            return sensorType;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "SensorData{" +
                    "sensorType='" + sensorType + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }

    public ProtocolRequest(String command, String reference, Map<String, String> parameters, String rawRequest) {
        this(command, reference, parameters, null, rawRequest);
    }

    public ProtocolRequest(String command, String reference, Map<String, String> parameters, List<SensorData> multiSensorData, String rawRequest) {
        this.command = command;
        this.reference = reference;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.multiSensorData = multiSensorData != null ? multiSensorData : new ArrayList<>();
        this.rawRequest = rawRequest;
    }

    public String getCommand() {
        return command;
    }

    public String getReference() {
        return reference;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    public List<SensorData> getMultiSensorData() {
        return multiSensorData;
    }

    public boolean hasMultiSensorData() {
        return !multiSensorData.isEmpty();
    }

    public String getRawRequest() {
        return rawRequest;
    }

    @Override
    public String toString() {
        return "ProtocolRequest{" +
                "command='" + command + '\'' +
                ", reference='" + reference + '\'' +
                ", parameters=" + parameters +
                ", multiSensorData=" + multiSensorData +
                '}';
    }
}