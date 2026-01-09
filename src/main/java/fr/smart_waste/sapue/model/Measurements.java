package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.Date;

/**
 * POJO for Measurements collection
 * Stores sensor measurements from smart bins
 */
public class Measurements {
    private ObjectId id;
    private ObjectId id_Controller; // Lien vers Modules (microcontroleur)
    private Date date;
    private Measurement measurement; // structured measurements object

    // Constructors
    public Measurements() {
        this.date = new Date(); // default to current time
        this.measurement = new Measurement();
    }

    public Measurements(ObjectId id_Controller, Date date, Measurement measurement) {
        this.id_Controller = id_Controller;
        this.date = date != null ? date : new Date();
        this.measurement = measurement;
    }

    // Nested Measurement class
    public static class Measurement {
        private Double fillLevel; // Niveau de remplissage en pourcentage
        private Double weight; // Poids en kilogrammes
        private Double temperature; // Température en degrés Celsius
        private Double humidity; // Humidité en pourcentage
        private Double pressure; // Pression en hPa
        private Double airQuality; // Qualité de l'air
        private Double batteryLevel; // Niveau de batterie en pourcentage
        private String wasteType; // Type de déchet identifié
        private Double confidence; // Niveau de confiance de l'identification

        // Constructors
        public Measurement() {
        }

        public Measurement(Double fillLevel, Double weight, Double temperature,
                Double humidity, Double pressure, Double airQuality, Double batteryLevel) {
            this.fillLevel = fillLevel;
            this.weight = weight;
            this.temperature = temperature;
            this.humidity = humidity;
            this.pressure = pressure;
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

        public Double getPressure() {
            return pressure;
        }

        public void setPressure(Double pressure) {
            this.pressure = pressure;
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

        public String getWasteType() {
            return wasteType;
        }

        public void setWasteType(String wasteType) {
            this.wasteType = wasteType;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return "Measurement{" +
                    "fillLevel=" + fillLevel +
                    ", weight=" + weight +
                    ", temperature=" + temperature +
                    ", humidity=" + humidity +
                    ", pressure=" + pressure +
                    ", airQuality=" + airQuality +
                    ", batteryLevel=" + batteryLevel +
                    ", wasteType='" + wasteType + '\'' +
                    ", confidence=" + confidence +
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

    public ObjectId getId_Controller() {
        return id_Controller;
    }

    public void setId_Controller(ObjectId id_Controller) {
        this.id_Controller = id_Controller;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

    @Override
    public String toString() {
        return "Measurements{" +
                "id=" + id +
                ", id_Controller=" + id_Controller +
                ", date=" + date +
                ", measurement=" + measurement +
                '}';
    }
}
