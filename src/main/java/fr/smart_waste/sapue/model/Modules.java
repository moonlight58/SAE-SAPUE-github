package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * POJO for Modules collection (formerly Microcontrolleur)
 * Represents IoT modules with embedded chipset references
 */
public class Modules {
    private ObjectId id;
    private String name; // required
    private String key; // UUID unique identifier (required)
    private String uc; // Microcontroller type
    private List<ObjectId> chipsets; // Embedded chipset references (IDs)
    private String ipAddress;
    private String firmwareVersion;
    private Date commissioningDate;
    private Boolean isEnabled;
    private Config config; // Configuration object

    // Nested Config class
    public static class Config {
        private Integer measurementInterval;

        public Config() {
        }

        public Config(Integer measurementInterval) {
            this.measurementInterval = measurementInterval;
        }

        public Integer getMeasurementInterval() {
            return measurementInterval;
        }

        public void setMeasurementInterval(Integer measurementInterval) {
            this.measurementInterval = measurementInterval;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "measurementInterval=" + measurementInterval +
                    '}';
        }
    }

    // Constructors
    public Modules() {
    }

    public Modules(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public Modules(String name, String key, String uc, List<ObjectId> chipsets,
            String ipAddress, String firmwareVersion, Date commissioningDate, Boolean isEnabled, Config config) {
        this.name = name;
        this.key = key;
        this.uc = uc;
        this.chipsets = chipsets;
        this.ipAddress = ipAddress;
        this.firmwareVersion = firmwareVersion;
        this.commissioningDate = commissioningDate;
        this.isEnabled = isEnabled;
        this.config = config;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUc() {
        return uc;
    }

    public void setUc(String uc) {
        this.uc = uc;
    }

    public List<ObjectId> getChipsets() {
        return chipsets;
    }

    public void setChipsets(List<ObjectId> chipsets) {
        this.chipsets = chipsets;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Date getCommissioningDate() {
        return commissioningDate;
    }

    public void setCommissioningDate(Date commissioningDate) {
        this.commissioningDate = commissioningDate;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "Modules{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", uc='" + uc + '\'' +
                ", chipsets=" + chipsets +
                ", ipAddress='" + ipAddress + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", commissioningDate=" + commissioningDate +
                ", isEnabled=" + isEnabled +
                ", config=" + config +
                '}';
    }
}
