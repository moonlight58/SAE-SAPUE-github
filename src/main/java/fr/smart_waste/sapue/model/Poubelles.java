package fr.smart_waste.sapue.model;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

/**
 * POJO for Poubelles collection
 * Represents a smart bin with hardware configuration and status
 */
public class Poubelles {
    private ObjectId id;
    private String type;
    private Boolean isSapue;
    private Double certaintyLevel;
    private Location location;
    private String adress;
    private HardwareConfig hardwareConfig;
    private LastMeasurement lastMeasurement;
    private ActiveAlerts activeAlerts;

    // Constructors
    public Poubelles() {
    }

    public Poubelles(String type, Boolean isSapue, Location location) {
        this.type = type;
        this.isSapue = isSapue;
        this.location = location;
    }

    // Nested classes
    public static class Location {
        private String type = "Point";
        private List<Double> coordinates; // [longitude, latitude]

        public Location() {
        }

        public Location(List<Double> coordinates) {
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }

        public Double getLongitude() {
            return coordinates != null && !coordinates.isEmpty() ? coordinates.get(0) : null;
        }

        public Double getLatitude() {
            return coordinates != null && coordinates.size() > 1 ? coordinates.get(1) : null;
        }

        @Override
        public String toString() {
            return "location{" +
                    "type='" + type + '\'' +
                    ", coordinates=" + coordinates +
                    '}';
        }
    }

    public static class HardwareConfig {
        private String ipAddress;
        private List<String> microcontroller; // References like ["MC-001", "MC-002"]
        private List<String> sensors; // Sensor types like ["BME280", "HX711"]

        public HardwareConfig() {
        }

        public HardwareConfig(String ipAddress, List<String> microcontroller, List<String> sensors) {
            this.ipAddress = ipAddress;
            this.microcontroller = microcontroller;
            this.sensors = sensors;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public List<String> getMicrocontroller() {
            return microcontroller;
        }

        public void setMicrocontroller(List<String> microcontroller) {
            this.microcontroller = microcontroller;
        }

        public List<String> getSensors() {
            return sensors;
        }

        public void setSensors(List<String> sensors) {
            this.sensors = sensors;
        }

        @Override
        public String toString() {
            return "hardwareConfig{" +
                    "ipAddress='" + ipAddress + '\'' +
                    ", microcontroller=" + microcontroller +
                    ", sensors=" + sensors +
                    '}';
        }
    }

    public static class LastMeasurement {
        private Date date;
        private Document measurement; // Flexible structure from Releves

        public LastMeasurement() {
        }

        public LastMeasurement(Date date, Document measurement) {
            this.date = date;
            this.measurement = measurement;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Document getMeasurement() {
            return measurement;
        }

        public void setMeasurement(Document measurement) {
            this.measurement = measurement;
        }

        @Override
        public String toString() {
            return "lastMeasurement{" +
                    "date=" + date +
                    ", measurement=" + measurement +
                    '}';
        }
    }

    public static class ActiveAlerts {
        private Boolean hasIssue;
        private String issueType;
        private ObjectId idSignalement;

        public ActiveAlerts() {
        }

        public ActiveAlerts(Boolean hasIssue, String issueType, ObjectId idSignalement) {
            this.hasIssue = hasIssue;
            this.issueType = issueType;
            this.idSignalement = idSignalement;
        }

        public Boolean getHasIssue() {
            return hasIssue;
        }

        public void setHasIssue(Boolean hasIssue) {
            this.hasIssue = hasIssue;
        }

        public String getIssueType() {
            return issueType;
        }

        public void setIssueType(String issueType) {
            this.issueType = issueType;
        }

        public ObjectId getIdSignalement() {
            return idSignalement;
        }

        public void setIdSignalement(ObjectId idSignalement) {
            this.idSignalement = idSignalement;
        }

        @Override
        public String toString() {
            return "activeAlerts{" +
                    "hasIssue=" + hasIssue +
                    ", issueType='" + issueType + '\'' +
                    ", idSignalement=" + idSignalement +
                    '}';
        }
    }

    // Main class getters and setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsSapue() {
        return isSapue;
    }

    public void setIsSapue(Boolean isSapue) {
        this.isSapue = isSapue;
    }

    public Double getCertaintyLevel() {
        return certaintyLevel;
    }

    public void setCertaintyLevel(Double certaintyLevel) {
        this.certaintyLevel = certaintyLevel;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public HardwareConfig getHardwareConfig() {
        return hardwareConfig;
    }

    public void setHardwareConfig(HardwareConfig hardwareConfig) {
        this.hardwareConfig = hardwareConfig;
    }

    public LastMeasurement getLastMeasurement() {
        return lastMeasurement;
    }

    public void setLastMeasurement(LastMeasurement lastMeasurement) {
        this.lastMeasurement = lastMeasurement;
    }

    public ActiveAlerts getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(ActiveAlerts activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    @Override
    public String toString() {
        return "Poubelles{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", isSapue=" + isSapue +
                ", certaintyLevel=" + certaintyLevel +
                ", location=" + location +
                ", adress='" + adress + '\'' +
                ", hardwareConfig=" + hardwareConfig +
                ", lastMeasurement=" + lastMeasurement +
                ", activeAlerts=" + activeAlerts +
                '}';
    }
}