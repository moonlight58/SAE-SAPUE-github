package fr.smart_waste.sapue.POJO;

import org.bson.Document;

/**
 * POJO for sensor configuration nested object
 */
public class SensorConfig {
    private String sensorType;
    private Boolean enabled;
    private Integer samplingInterval; // in seconds
    private Document parameters; // flexible structure for sensor-specific config

    // Constructors
    public SensorConfig() {
    }

    public SensorConfig(String sensorType, Boolean enabled, Integer samplingInterval, Document parameters) {
        this.sensorType = sensorType;
        this.enabled = enabled;
        this.samplingInterval = samplingInterval;
        this.parameters = parameters;
    }

    // Getters and Setters
    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSamplingInterval() {
        return samplingInterval;
    }

    public void setSamplingInterval(Integer samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public Document getParameters() {
        return parameters;
    }

    public void setParameters(Document parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "SensorConfig{" +
                "sensorType='" + sensorType + '\'' +
                ", enabled=" + enabled +
                ", samplingInterval=" + samplingInterval +
                ", parameters=" + parameters +
                '}';
    }
}
