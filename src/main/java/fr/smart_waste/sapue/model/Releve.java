package fr.smart_waste.sapue.model;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

/**
 * POJO for Relevés collection
 */
public class Releve {
    private ObjectId id;
    private ObjectId idControlleur; // reference to Microcontroller
    private Date date;
    private Document releve; // flexible structure for different sensor readings

    // Constructors
    public Releve() {
        this.date = new Date(); // default to current time
    }

    public Releve(ObjectId idControlleur, Date date, Document releve) {
        this.idControlleur = idControlleur;
        this.date = date != null ? date : new Date();
        this.releve = releve;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getIdControlleur() {
        return idControlleur;
    }

    public void setIdControlleur(ObjectId idControlleur) {
        this.idControlleur = idControlleur;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Document getReleve() {
        return releve;
    }

    public void setReleve(Document releve) {
        this.releve = releve;
    }

    @Override
    public String toString() {
        return "Releve{" +
                "id=" + id +
                ", idControlleur=" + idControlleur +
                ", date=" + date +
                ", releve=" + releve +
                '}';
    }
}
