package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;

/**
 * POJO for Users collection
 * Represents a user in the Smart Waste system
 */
public class Users {
    private ObjectId id;
    private String name;
    private String password; // hashed password
    private String mail;
    private String phone;
    private String role; // "user", "agent", "admin"
    private Double levelOfTrust;

    // Constructors
    public Users() {
    }

    public Users(String name, String mail, String role) {
        this.name = name;
        this.mail = mail;
        this.role = role;
    }

    public Users(ObjectId id, String name, String mail, String phone, String role, Double levelOfTrust) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.role = role;
        this.levelOfTrust = levelOfTrust;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getLevelOfTrust() {
        return levelOfTrust;
    }

    public void setLevelOfTrust(Double levelOfTrust) {
        this.levelOfTrust = levelOfTrust;
    }

    // Helper methods
    
    /**
     * Check if user is an admin
     * @return true if role is "admin"
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }

    /**
     * Check if user is an agent
     * @return true if role is "agent"
     */
    public boolean isAgent() {
        return "agent".equalsIgnoreCase(this.role);
    }

    /**
     * Check if user is a regular user
     * @return true if role is "user"
     */
    public boolean isUser() {
        return "user".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mail='" + mail + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", levelOfTrust=" + levelOfTrust +
                '}';
    }
}