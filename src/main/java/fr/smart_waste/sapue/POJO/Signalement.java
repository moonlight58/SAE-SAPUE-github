package com.smartwaste.server.model;

import org.bson.types.ObjectId;
import java.util.List;

/**
 * POJO for Signalements collection
 */
public class Signalement {
    private ObjectId id;
    private String type;
    private ObjectId utilisateur; // reference to User
    private List<Double> position; // [longitude, latitude]
    private String imagePath;

    // Constructors
    public Signalement() {
    }

    public Signalement(String type, ObjectId utilisateur, List<Double> position, String imagePath) {
        this.type = type;
        this.utilisateur = utilisateur;
        this.position = position;
        this.imagePath = imagePath;
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

    public ObjectId getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(ObjectId utilisateur) {
        this.utilisateur = utilisateur;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Signalement{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", utilisateur=" + utilisateur +
                ", position=" + position +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
