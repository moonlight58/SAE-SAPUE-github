package fr.smart_waste.sapue.model;

import org.bson.types.ObjectId;
import java.util.Date;

public class Ticket {
    private ObjectId id;
    private String type;
    private String title;
    private String description;
    private String status;
    private ObjectId user;
    private Date created_at;
    private Date updated_at;
    private ObjectId chat_history;

    // Constructors
    public Ticket() {}

    public Ticket(ObjectId id, String type, String title, String description, String status, ObjectId user) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = status;
        this.user = user;
        this.created_at = new Date();
        this.updated_at = new Date();
        this.chat_history = null;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public ObjectId getUser() { return user; }
    public void setUser(ObjectId user) { this.user = user; }
    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }
    public ObjectId getChat_history() { return chat_history; }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", user=" + user +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                ", chat_history=" + chat_history +
                '}';
    }
}
