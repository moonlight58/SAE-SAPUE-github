package fr.smart_waste.sapue.POJO;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * POJO for Analyse Média collection
 */
public class AnalyseMedia {
    private ObjectId id;
    private Document resultat; // flexible structure for analysis results
    
    // Constructors
    public AnalyseMedia() {
    }

    public AnalyseMedia(Document resultat) {
        this.resultat = resultat;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Document getResultat() {
        return resultat;
    }

    public void setResultat(Document resultat) {
        this.resultat = resultat;
    }

    // Helper methods for common result fields
    public Boolean getWasteDetected() {
        return resultat != null ? resultat.getBoolean("wasteDetected") : null;
    }

    public String getWasteType() {
        return resultat != null ? resultat.getString("wasteType") : null;
    }

    public Double getConfidence() {
        return resultat != null ? resultat.getDouble("confidence") : null;
    }

    @Override
    public String toString() {
        return "AnalyseMedia{" +
                "id=" + id +
                ", resultat=" + resultat +
                '}';
    }
}
