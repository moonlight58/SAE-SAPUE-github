package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * POJO for Signalements collection
 * Represents a report made by a user (waste deposit or bin issue)
 */
public class Signalements {
    private ObjectId id;
    private UserInfo author;
    private UserInfo cleaner; // Only for waste deposits, null for bin issues
    private Date createdAt;
    private Date updatedAt;
    private String status; // "Ouvert", "En cours", "Résolu"
    private ObjectId idPoubelle; // Link to Poubelle if bin issue, null if waste deposit
    private String type; // "Depot sauvage" or "Problème poubelle"
    private String issueType; // "Poubelle pleine", "Problème technique", "Débordement", etc.
    private Location location;
    private Photo photo;
    private List<HistoryEntry> history;

    // Constructors
    public Signalements() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.history = new ArrayList<>();
    }

    public Signalements(UserInfo author, String type, String issueType, Location location) {
        this();
        this.author = author;
        this.type = type;
        this.issueType = issueType;
        this.location = location;
        this.status = "Ouvert";
    }

    // Nested classes
    public static class UserInfo {
        private ObjectId idUser;
        private String name;
        private String role; // "user", "agent", "admin"

        public UserInfo() {
        }

        public UserInfo(ObjectId idUser, String name, String role) {
            this.idUser = idUser;
            this.name = name;
            this.role = role;
        }

        public ObjectId getIdUser() {
            return idUser;
        }

        public void setIdUser(ObjectId idUser) {
            this.idUser = idUser;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "idUser=" + idUser +
                    ", name='" + name + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }

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

    public static class Photo {
        private String initialPhoto;
        private String finalPhoto;

        public Photo() {
        }

        public Photo(String initialPhoto, String finalPhoto) {
            this.initialPhoto = initialPhoto;
            this.finalPhoto = finalPhoto;
        }

        public String getInitialPhoto() {
            return initialPhoto;
        }

        public void setInitialPhoto(String initialPhoto) {
            this.initialPhoto = initialPhoto;
        }

        public String getFinalPhoto() {
            return finalPhoto;
        }

        public void setFinalPhoto(String finalPhoto) {
            this.finalPhoto = finalPhoto;
        }

        @Override
        public String toString() {
            return "Photo{" +
                    "initialPhoto='" + initialPhoto + '\'' +
                    ", finalPhoto='" + finalPhoto + '\'' +
                    '}';
        }
    }

    public static class HistoryEntry {
        private Date date;
        private String status;
        private ObjectId byUser;

        public HistoryEntry() {
            this.date = new Date();
        }

        public HistoryEntry(String status, ObjectId byUser) {
            this();
            this.status = status;
            this.byUser = byUser;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public ObjectId getByUser() {
            return byUser;
        }

        public void setByUser(ObjectId byUser) {
            this.byUser = byUser;
        }

        @Override
        public String toString() {
            return "HistoryEntry{" +
                    "date=" + date +
                    ", status='" + status + '\'' +
                    ", byUser=" + byUser +
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

    public UserInfo getAuthor() {
        return author;
    }

    public void setAuthor(UserInfo author) {
        this.author = author;
    }

    public UserInfo getCleaner() {
        return cleaner;
    }

    public void setCleaner(UserInfo cleaner) {
        this.cleaner = cleaner;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public ObjectId getIdPoubelle() {
        return idPoubelle;
    }

    public void setIdPoubelle(ObjectId idPoubelle) {
        this.idPoubelle = idPoubelle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryEntry> history) {
        this.history = history;
    }

    // Helper methods

    /**
     * Add a history entry and update status
     * @param newStatus New status
     * @param byUser User performing the change
     */
    public void addHistoryEntry(String newStatus, ObjectId byUser) {
        HistoryEntry entry = new HistoryEntry(newStatus, byUser);
        if (this.history == null) {
            this.history = new ArrayList<>();
        }
        this.history.add(entry);
        this.setStatus(newStatus);
    }

    /**
     * Check if this is a waste deposit report
     * @return true if type is "Depot sauvage"
     */
    public boolean isWasteDeposit() {
        return "Depot sauvage".equalsIgnoreCase(this.type);
    }

    /**
     * Check if this is a bin issue report
     * @return true if type is "Problème poubelle"
     */
    public boolean isBinIssue() {
        return "Problème poubelle".equalsIgnoreCase(this.type) ||
                "Probleme poubelle".equalsIgnoreCase(this.type);
    }

    /**
     * Check if report is resolved
     * @return true if status is "Résolu"
     */
    public boolean isResolved() {
        return "Résolu".equalsIgnoreCase(this.status) ||
                "Resolu".equalsIgnoreCase(this.status);
    }

    /**
     * Check if report is in progress
     * @return true if status is "En cours"
     */
    public boolean isInProgress() {
        return "En cours".equalsIgnoreCase(this.status);
    }

    /**
     * Check if report is open
     * @return true if status is "Ouvert"
     */
    public boolean isOpen() {
        return "Ouvert".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "Signalements{" +
                "id=" + id +
                ", author=" + author +
                ", cleaner=" + cleaner +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", status='" + status + '\'' +
                ", idPoubelle=" + idPoubelle +
                ", type='" + type + '\'' +
                ", issueType='" + issueType + '\'' +
                ", location=" + location +
                ", photo=" + photo +
                ", history=" + history +
                '}';
    }
}