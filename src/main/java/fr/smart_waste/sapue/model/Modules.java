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
    private String name;                    // required
    private String key;                     // UUID unique identifier (required)
    private String uc;                      // Microcontroller type
    private List<ChipsetReference> chipsets; // Embedded chipset references
    private String ipAddress;
    private String firmwareVersion;
    private Date commissioningDate;
    private Boolean isEnabled;

    /**
     * Nested class for embedded chipset references within Module
     */
    public static class ChipsetReference {
        private ObjectId _id;
        private String type;
        private String links;

        public ChipsetReference() {
        }

        public ChipsetReference(ObjectId _id, String type, String links) {
            this._id = _id;
            this.type = type;
            this.links = links;
        }

        public ObjectId get_id() {
            return _id;
        }

        public void set_id(ObjectId _id) {
            this._id = _id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLinks() {
            return links;
        }

        public void setLinks(String links) {
            this.links = links;
        }

        @Override
        public String toString() {
            return "ChipsetReference{" +
                    "_id=" + _id +
                    ", type='" + type + '\'' +
                    ", links='" + links + '\'' +
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

    public Modules(String name, String key, String uc, List<ChipsetReference> chipsets, 
                   String ipAddress, String firmwareVersion, Date commissioningDate, Boolean isEnabled) {
        this.name = name;
        this.key = key;
        this.uc = uc;
        this.chipsets = chipsets;
        this.ipAddress = ipAddress;
        this.firmwareVersion = firmwareVersion;
        this.commissioningDate = commissioningDate;
        this.isEnabled = isEnabled;
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

    public List<ChipsetReference> getChipsets() {
        return chipsets;
    }

    public void setChipsets(List<ChipsetReference> chipsets) {
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
                '}';
    }
}
