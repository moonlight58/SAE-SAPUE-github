package fr.smart_waste.sapue.model;

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
    public void setWasteDetected(Boolean wasteDetected) {
        if (resultat == null) {
            resultat = new Document();
        }
        resultat.put("wasteDetected", wasteDetected);
    }
    
    public Boolean getWasteDetected() {
        return resultat != null ? resultat.getBoolean("wasteDetected") : null;
    }

    public void setWasteType(String wasteType) {
        if (resultat == null) {
            resultat = new Document();
        }
        resultat.put("wasteType", wasteType);
    }

    public String getWasteType() {
        return resultat != null ? resultat.getString("wasteType") : null;
    }

    public void setConfidence(Double confidence) {
        if (resultat == null) {
            resultat = new Document();
        }
        resultat.put("confidence", confidence);
    }

    public Double getConfidence() {
        return resultat != null ? resultat.getDouble("confidence") : null;
    }

    public void setReportId(ObjectId reportId) {
        if (resultat == null) {
            resultat = new Document();
        }
        resultat.put("reportId", reportId);
    }

    public ObjectId getReportId() {
        return resultat != null ? (ObjectId) resultat.get("reportId") : null;
    }

    public void setAnalysisStatus(String analysisStatus) {
        if (resultat == null) {
            resultat = new Document();
        }
        resultat.put("analysisStatus", analysisStatus);
    }

    public String getAnalysisStatus() {
        return resultat != null ? resultat.getString("analysisStatus") : null;
    }

    public void setFailureReason(String failureReason) {
        if (resultat == null) {
            resultat = new Document();
        }
        resultat.put("failureReason", failureReason);
    }

    public String getFailureReason() {
        return resultat != null ? resultat.getString("failureReason") : null;
    }

    @Override
    public String toString() {
        return "AnalyseMedia{" +
                "id=" + id +
                ", resultat=" + resultat +
                '}';
    }
}
