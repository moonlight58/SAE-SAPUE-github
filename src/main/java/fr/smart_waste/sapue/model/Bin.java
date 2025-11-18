package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.List;

/**
 * POJO for Poubelles collection
 */
public class Bin {
    private ObjectId id;
    private String type;
    private List<Double> position; // [longitude, latitude]
    private Boolean isSAPUE;

    // Constructors
    public Bin() {
    }

    public Bin(String type, List<Double> position, Boolean isSAPUE) {
        this.type = type;
        this.position = position;
        this.isSAPUE = isSAPUE;
    }

    // Getters and Setters
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

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public Double getLongitude() {
        return position != null && position.size() > 0 ? position.get(0) : null;
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
        return "Bin{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", position=" + position +
                ", isSAPUE=" + isSAPUE +
                '}';
    }
}
