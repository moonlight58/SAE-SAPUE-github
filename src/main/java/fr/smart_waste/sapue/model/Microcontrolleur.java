package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;

/**
 * POJO for Micro-controlleur collection
 */
public class Microcontrolleur {
    private ObjectId id;
    private String reference;
    private ObjectId mapPoint; // reference to MapPoint
    private String ipAddress;
    private SensorConfig configSensor;

    // Constructors
    public Microcontrolleur() {
    }

    public Microcontrolleur(String reference, ObjectId mapPoint, String ipAddress, SensorConfig configSensor) {
        this.reference = reference;
        this.mapPoint = mapPoint;
        this.ipAddress = ipAddress;
        this.configSensor = configSensor;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ObjectId getMapPoint() {
        return mapPoint;
    }

    public void setMapPoint(ObjectId mapPoint) {
        this.mapPoint = mapPoint;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public SensorConfig getConfigSensor() {
        return configSensor;
    }

    public void setConfigSensor(SensorConfig configSensor) {
        this.configSensor = configSensor;
    }

    @Override
    public String toString() {
        return "Microcontrolleur{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", mapPoint=" + mapPoint +
                ", ipAddress='" + ipAddress + '\'' +
                ", configSensor=" + configSensor +
                '}';
    }
}
