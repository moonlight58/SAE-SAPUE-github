package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;

/**
 * POJO for Micro-controlleur collection
 * Updated to reference Poubelles instead of MapPoint
 */
public class Microcontrolleur {
    private ObjectId id;
    private String reference;
    private ObjectId idPoubelle; // reference to Poubelles (replaces mapPoint)
    private String ipAddress;
    private SensorConfig configSensor;

    // Constructors
    public Microcontrolleur() {
    }

    public Microcontrolleur(String reference, ObjectId idPoubelle, String ipAddress, SensorConfig configSensor) {
        this.reference = reference;
        this.idPoubelle = idPoubelle;
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

    public ObjectId getIdPoubelle() {
        return idPoubelle;
    }

    public void setIdPoubelle(ObjectId idPoubelle) {
        this.idPoubelle = idPoubelle;
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
        return "Microcontrolleurs{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", idPoubelle=" + idPoubelle +
                ", ipAddress='" + ipAddress + '\'' +
                ", configSensor=" + configSensor +
                '}';
    }
}