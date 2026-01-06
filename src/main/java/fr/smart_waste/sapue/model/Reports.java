package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * POJO for Reports collection (formerly Signalements)
 * Represents a report made by a user (waste deposit or bin issue)
 */
public class Reports {
    private ObjectId id;
    private UserInfo author;
    private UserInfo cleaner; // Only for waste deposits, null for bin issues
    private String status; // "Ouvert", "Affecte", "EnCours", "Resolu", "Rejete"
    private ObjectId mapPoint; // CHANGED: Reference to MapPoints (formerly idPoubelle)
    private String type; // "DepotSauvage" or "ProblemePoubelle"
    private String issueType; // "Poubelle pleine", "Problème technique", etc.
    private Location location;
    private Photo photo;
    private List<HistoryEntry> history;

    // Constructors
    public Reports() {
        this.history = new ArrayList<>();
    }

    public Reports(UserInfo author, String type, String issueType, Location location) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ObjectId getMapPoint() {
        return mapPoint;
    }

    public void setMapPoint(ObjectId mapPoint) {
        this.mapPoint = mapPoint;
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
     * @return true if type is "DepotSauvage"
     */
    public boolean isWasteDeposit() {
        return "DepotSauvage".equalsIgnoreCase(this.type);
    }

    /**
     * Check if this is a bin issue report
     * @return true if type is "ProblemePoubelle"
     */
    public boolean isBinIssue() {
        return "ProblemePoubelle".equalsIgnoreCase(this.type);
    }

    /**
     * Check if report is resolved
     * @return true if status is "Resolu"
     */
    public boolean isResolved() {
        return "Resolu".equalsIgnoreCase(this.status);
    }

    /**
     * Check if report is in progress
     * @return true if status is "EnCours"
     */
    public boolean isInProgress() {
        return "EnCours".equalsIgnoreCase(this.status);
    }

    /**
     * Check if report is open
     * @return true if status is "Ouvert"
     */
    public boolean isOpen() {
        return "Ouvert".equalsIgnoreCase(this.status);
    }

    /**
     * Check if report is assigned
     * @return true if status is "Affecte"
     */
    public boolean isAssigned() {
        return "Affecte".equalsIgnoreCase(this.status);
    }

    /**
     * Check if report is rejected
     * @return true if status is "Rejete"
     */
    public boolean isRejected() {
        return "Rejete".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "Reports{" +
                "id=" + id +
                ", author=" + author +
                ", cleaner=" + cleaner +
                ", status='" + status + '\'' +
                ", mapPoint=" + mapPoint +
                ", type='" + type + '\'' +
                ", issueType='" + issueType + '\'' +
                ", location=" + location +
                ", photo=" + photo +
                ", history=" + history +
                '}';
    }
}
