package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.List;

/**
 * POJO for MapPoints collection
 */
public class MapPoint {
    private ObjectId pointId;
    private String type;
    private List<Double> position; // [longitude, latitude]
    private Boolean isSAPUE;
    private Double niveauConfiance;
    private Integer taille;

    // Constructors
    public MapPoint() {
    }

    public MapPoint(String type, List<Double> position, Boolean isSAPUE) {
        this.type = type;
        this.position = position;
        this.isSAPUE = isSAPUE;
    }

    // Getters and Setters
    public ObjectId getId() {
        return pointId;
    }

    public void setId(ObjectId id) {
        this.pointId = pointId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public Double getLongitude() {
        return position != null && !position.isEmpty() ? position.get(0) : null;
    }

    public Double getLatitude() {
        return position != null && position.size() > 1 ? position.get(1) : null;
    }

    public Boolean getIsSAPUE() {
        return isSAPUE;
    }

    public void setIsSAPUE(Boolean isSAPUE) {
        this.isSAPUE = isSAPUE;
    }

    @Override
    public String toString() {
        return "MapPoint{" +
                "pointId=" + pointId +
                ", type='" + type + '\'' +
                ", position=" + position +
                ", isSAPUE=" + isSAPUE +
                '}';
    }
}
