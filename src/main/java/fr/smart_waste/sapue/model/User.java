package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;

/**
 * POJO for Utilisateurs collection
 */
public class User {
    private ObjectId id;
    private String pseudo;
    private String motDePasse;
    private String mail;
    private String numeroTelephone;
    private String role;
    private Double niveauConfiance;

    // Constructors
    public User() {
    }

    public User(String pseudo, String motDePasse, String mail, String numeroTelephone, 
                String role, Double niveauConfiance) {
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.mail = mail;
        this.numeroTelephone = numeroTelephone;
        this.role = role;
        this.niveauConfiance = niveauConfiance;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getNiveauConfiance() {
        return niveauConfiance;
    }

    public void setNiveauConfiance(Double niveauConfiance) {
        this.niveauConfiance = niveauConfiance;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", mail='" + mail + '\'' +
                ", numeroTelephone='" + numeroTelephone + '\'' +
                ", role='" + role + '\'' +
                ", niveauConfiance=" + niveauConfiance +
                '}';
    }
}
