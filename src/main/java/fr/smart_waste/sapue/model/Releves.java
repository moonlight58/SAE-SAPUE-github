package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.Date;

/**
 * POJO for Relevés collection
 * Stores sensor measurements from smart bins
 */
public class Releves {
    private ObjectId id;
    private ObjectId idPoubelle; // reference to Poubelles (changed from idControlleur)
    private Date timestamp; // renamed from date for clarity
    private Measurements measurements; // structured measurements object

    // Constructors
    public Releves() {
        this.timestamp = new Date(); // default to current time
        this.measurements = new Measurements();
    }

    public Releves(ObjectId idPoubelle, Date timestamp, Measurements measurements) {
        this.idPoubelle = idPoubelle;
        this.timestamp = timestamp != null ? timestamp : new Date();
        this.measurements = measurements;
    }

    // Nested Measurements class
    public static class Measurements {
        private Double fillLevel; // Niveau de remplissage en pourcentage
        private Double weight; // Poids en kilogrammes
        private Double temperature; // Température en degrés Celsius
        private Double humidity; // Humidité en pourcentage
        private Double airQuality; // Qualité de l'air
        private Double batteryLevel; // Niveau de batterie en pourcentage

        // Constructors
        public Measurements() {
        }

        public Measurements(Double fillLevel, Double weight, Double temperature,
                            Double humidity, Double airQuality, Double batteryLevel) {
            this.fillLevel = fillLevel;
            this.weight = weight;
            this.temperature = temperature;
            this.humidity = humidity;
            this.airQuality = airQuality;
            this.batteryLevel = batteryLevel;
        }

        // Getters and Setters
        public Double getFillLevel() {
            return fillLevel;
        }

        public void setFillLevel(Double fillLevel) {
            this.fillLevel = fillLevel;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getHumidity() {
            return humidity;
        }

        public void setHumidity(Double humidity) {
            this.humidity = humidity;
        }

        public Double getAirQuality() {
            return airQuality;
        }

        public void setAirQuality(Double airQuality) {
            this.airQuality = airQuality;
        }

        public Double getBatteryLevel() {
            return batteryLevel;
        }

        public void setBatteryLevel(Double batteryLevel) {
            this.batteryLevel = batteryLevel;
        }

        @Override
        public String toString() {
            return "measurements{" +
                    "fillLevel=" + fillLevel +
                    ", weight=" + weight +
                    ", temperature=" + temperature +
                    ", humidity=" + humidity +
                    ", airQuality=" + airQuality +
                    ", batteryLevel=" + batteryLevel +
                    '}';
        }
    }

    // Main class Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getIdPoubelle() {
        return idPoubelle;
    }

    public void setIdPoubelle(ObjectId idPoubelle) {
        this.idPoubelle = idPoubelle;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Measurements getMeasurements() {
        return measurements;
    }

    public void setMeasurements(Measurements measurements) {
        this.measurements = measurements;
    }

    @Override
    public String toString() {
        return "Releves{" +
                "id=" + id +
                ", idPoubelle=" + idPoubelle +
                ", timestamp=" + timestamp +
                ", measurements=" + measurements +
                '}';
    }
}