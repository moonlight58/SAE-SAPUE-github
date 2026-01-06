package fr.smart_waste.sapue.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * POJO for Chipsets collection
 * Represents sensors/chipsets within a Module
 */
public class Chipsets {
    private ObjectId id;
    private String name;           // required
    private String description;
    private List<String> links;    // connection links
    private List<String> caps;     // capabilities (required)
    private Document config;       // flexible configuration object
    private ObjectId moduleID;     // reference to parent Module

    // Constructors
    public Chipsets() {
    }

    public Chipsets(String name, List<String> caps) {
        this.name = name;
        this.caps = caps;
    }

    public Chipsets(String name, String description, List<String> links, List<String> caps, Document config, ObjectId moduleID) {
        this.name = name;
        this.description = description;
        this.links = links;
        this.caps = caps;
        this.config = config;
        this.moduleID = moduleID;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public List<String> getCaps() {
        return caps;
    }

    public void setCaps(List<String> caps) {
        this.caps = caps;
    }

    public Document getConfig() {
        return config;
    }

    public void setConfig(Document config) {
        this.config = config;
    }

    public ObjectId getModuleID() {
        return moduleID;
    }

    public void setModuleID(ObjectId moduleID) {
        this.moduleID = moduleID;
    }

    @Override
    public String toString() {
        return "Chipsets{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", links=" + links +
                ", caps=" + caps +
                ", config=" + config +
                ", moduleID=" + moduleID +
                '}';
    }
}
