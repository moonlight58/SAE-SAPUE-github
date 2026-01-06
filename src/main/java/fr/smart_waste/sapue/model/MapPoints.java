package fr.smart_waste.sapue.model;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

/**
 * POJO for MapPoints collection (formerly Poubelles)
 * Represents a point of interest (smart bin or location)
 */
public class MapPoints {
    private ObjectId id;
    private String type;
    private Boolean isSapue;
    private Double certaintyLevel;
    private Location location;
    private String address; // FIXED: was "adress"
    private HardwareConfig hardwareConfig;
    private LastMeasurement lastMeasurement;
    private ActiveAlerts activeAlerts;

    // Constructors
    public MapPoints() {
    }

    public MapPoints(String type, Boolean isSapue, Location location) {
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
            return "Location{" +
                    "type='" + type + '\'' +
                    ", coordinates=" + coordinates +
                    '}';
        }
    }

    public static class HardwareConfig {
        private String ipAddress;
        private List<ObjectId> modules; // CHANGED: was List<String> microcontroller
        private List<String> sensors; // Sensor types like ["BME280", "HX711"]

        public HardwareConfig() {
        }

        public HardwareConfig(String ipAddress, List<ObjectId> modules, List<String> sensors) {
            this.ipAddress = ipAddress;
            this.modules = modules;
            this.sensors = sensors;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public List<ObjectId> getModules() {
            return modules;
        }

        public void setModules(List<ObjectId> modules) {
            this.modules = modules;
        }

        public List<String> getSensors() {
            return sensors;
        }

        public void setSensors(List<String> sensors) {
            this.sensors = sensors;
        }

        @Override
        public String toString() {
            return "HardwareConfig{" +
                    "ipAddress='" + ipAddress + '\'' +
                    ", modules=" + modules +
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
            return "LastMeasurement{" +
                    "date=" + date +
                    ", measurement=" + measurement +
                    '}';
        }
    }

    public static class ActiveAlerts {
        private Boolean hasIssue;
        private String issueType;
        private ObjectId idReport; // CHANGED: was idSignalement

        public ActiveAlerts() {
        }

        public ActiveAlerts(Boolean hasIssue, String issueType, ObjectId idReport) {
            this.hasIssue = hasIssue;
            this.issueType = issueType;
            this.idReport = idReport;
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

        public ObjectId getIdReport() {
            return idReport;
        }

        public void setIdReport(ObjectId idReport) {
            this.idReport = idReport;
        }

        @Override
        public String toString() {
            return "ActiveAlerts{" +
                    "hasIssue=" + hasIssue +
                    ", issueType='" + issueType + '\'' +
                    ", idReport=" + idReport +
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        return "MapPoints{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", isSapue=" + isSapue +
                ", certaintyLevel=" + certaintyLevel +
                ", location=" + location +
                ", address='" + address + '\'' +
                ", hardwareConfig=" + hardwareConfig +
                ", lastMeasurement=" + lastMeasurement +
                ", activeAlerts=" + activeAlerts +
                '}';
    }
}
