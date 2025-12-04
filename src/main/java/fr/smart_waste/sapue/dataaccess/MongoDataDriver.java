package fr.smart_waste.sapue.dataaccess;

import java.util.ArrayList;
import java.util.List;

import fr.smart_waste.sapue.model.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * MongoDB implementation of DataDriver interface
 * Provides direct database access for Smart Waste application
 */
public class MongoDataDriver implements DataDriver {

    private final String mongoURL;
    private final String databaseName;
    private final CodecProvider pojoCodecProvider;
    private final CodecRegistry pojoCodecRegistry;

    private MongoClient mongoClient;
    private MongoDatabase database;

    // Collections
    private MongoCollection<Poubelles> poubelles;
    private MongoCollection<Microcontrolleur> microcontrolleurs;
    private MongoCollection<Signalement> signalements;
    private MongoCollection<Releve> releves;
    private MongoCollection<AnalyseMedia> analyseMedias;

    /**
     * Constructor
     * @param mongoURL MongoDB connection string
     * @param databaseName Database name
     */
    public MongoDataDriver(String mongoURL, String databaseName) {
        this.mongoURL = mongoURL;
        this.databaseName = databaseName;

        // Setup POJO codec for automatic POJO mapping
        this.pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        this.pojoCodecRegistry = fromRegistries(
                getDefaultCodecRegistry(),
                fromProviders(pojoCodecProvider)
        );

        // Initialize connection and collections
        init();
    }

    /**
     * Initialize MongoDB connection and collections
     * @return true if successful, false otherwise
     */
    public boolean init() {
        try {
            mongoClient = MongoClients.create(mongoURL);
            database = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);

            // Initialize all collections with POJO codec
            poubelles = database.getCollection("poubelles", Poubelles.class);
            microcontrolleurs = database.getCollection("microcontrolleurs", Microcontrolleur.class);
            signalements = database.getCollection("signalements", Signalement.class);
            releves = database.getCollection("releves", Releve.class);
            analyseMedias = database.getCollection("analyseMedias", AnalyseMedia.class);

            System.out.println("[MongoDataDriver] Connected to database: " + databaseName);
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("[MongoDataDriver] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========== Poubelles Operations ==========

    @Override
    public synchronized ObjectId insertPoubelle(Poubelles poubelle) {
        if (poubelle == null) return null;
        try {
            InsertOneResult result = poubelles.insertOne(poubelle);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : poubelle.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting poubelle: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Poubelles findPoubelleById(ObjectId id) {
        if (id == null) return null;
        try {
            return poubelles.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding poubelle by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Poubelles findPoubelleByMicrocontroller(String mcReference) {
        if (mcReference == null || mcReference.isEmpty()) return null;
        try {
            return poubelles.find(eq("hardwareConfig.microcontroller", mcReference)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding poubelle by microcontroller: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updatePoubelle(Poubelles poubelle) {
        if (poubelle == null || poubelle.getId() == null) return false;
        try {
            return poubelles.replaceOne(eq("_id", poubelle.getId()), poubelle).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating poubelle: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateLastMeasurement(ObjectId id, Poubelles.LastMeasurement lastMeasurement) {
        if (id == null || lastMeasurement == null) return false;
        try {
            UpdateResult result = poubelles.updateOne(
                    eq("_id", id),
                    set("lastMeasurement", lastMeasurement)
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating last measurement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateActiveAlerts(ObjectId id, Poubelles.ActiveAlerts activeAlerts) {
        if (id == null || activeAlerts == null) return false;
        try {
            UpdateResult result = poubelles.updateOne(
                    eq("_id", id),
                    set("activeAlerts", activeAlerts)
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating active alerts: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deletePoubelle(ObjectId id) {
        if (id == null) return false;
        try {
            return poubelles.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting poubelle: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Poubelles> findAllPoubelles() {
        try {
            return poubelles.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all poubelles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Poubelles> findPoubellesWithActiveAlerts() {
        try {
            return poubelles.find(eq("activeAlerts.hasIssue", true)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding poubelles with active alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Microcontrolleur Operations ==========

    @Override
    public synchronized ObjectId insertMicrocontrolleur(Microcontrolleur mc) {
        if (mc == null) return null;
        try {
            InsertOneResult result = microcontrolleurs.insertOne(mc);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : mc.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting microcontrolleur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Microcontrolleur findMicrocontrolleurById(ObjectId id) {
        if (id == null) return null;
        try {
            return microcontrolleurs.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding microcontrolleur by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Microcontrolleur findMicrocontrolleurByReference(String reference) {
        if (reference == null || reference.isEmpty()) return null;
        try {
            return microcontrolleurs.find(eq("reference", reference)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding microcontrolleur by reference: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateMicrocontrolleur(Microcontrolleur mc) {
        if (mc == null || mc.getId() == null) return false;
        try {
            return microcontrolleurs.replaceOne(eq("_id", mc.getId()), mc).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating microcontrolleur: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMicrocontrolleur(ObjectId id) {
        if (id == null) return false;
        try {
            return microcontrolleurs.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting microcontrolleur: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Microcontrolleur> findAllMicrocontrolleurs() {
        try {
            return microcontrolleurs.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all microcontrolleurs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Signalement Operations ==========

    @Override
    public synchronized ObjectId insertSignalement(Signalement s) {
        if (s == null) return null;
        try {
            InsertOneResult result = signalements.insertOne(s);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : s.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting signalement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Signalement findSignalementById(ObjectId id) {
        if (id == null) return null;
        try {
            return signalements.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding signalement: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateSignalement(Signalement s) {
        if (s == null || s.getId() == null) return false;
        try {
            return signalements.replaceOne(eq("_id", s.getId()), s).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating signalement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSignalement(ObjectId id) {
        if (id == null) return false;
        try {
            return signalements.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting signalement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Signalement> findAllSignalements() {
        try {
            return signalements.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all signalements: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Releve Operations ==========

    @Override
    public synchronized ObjectId insertReleve(Releve r) {
        if (r == null) return null;
        try {
            InsertOneResult result = releves.insertOne(r);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : r.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting releve: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Releve findReleveById(ObjectId id) {
        if (id == null) return null;
        try {
            return releves.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding releve: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Releve> findRelevesByControlleur(ObjectId idControlleur) {
        if (idControlleur == null) return new ArrayList<>();
        try {
            return releves.find(eq("idControlleur", idControlleur)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding releves by controlleur: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateReleve(Releve r) {
        if (r == null || r.getId() == null) return false;
        try {
            return releves.replaceOne(eq("_id", r.getId()), r).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating releve: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteReleve(ObjectId id) {
        if (id == null) return false;
        try {
            return releves.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting releve: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Releve> findAllReleves() {
        try {
            return releves.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all releves: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== AnalyseMedia Operations ==========

    @Override
    public synchronized ObjectId insertAnalyseMedia(AnalyseMedia a) {
        if (a == null) return null;
        try {
            InsertOneResult result = analyseMedias.insertOne(a);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : a.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting analyseMedia: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public AnalyseMedia findAnalyseMediaById(ObjectId id) {
        if (id == null) return null;
        try {
            return analyseMedias.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding analyseMedia: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateAnalyseMedia(AnalyseMedia a) {
        if (a == null || a.getId() == null) return false;
        try {
            return analyseMedias.replaceOne(eq("_id", a.getId()), a).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating analyseMedia: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAnalyseMedia(ObjectId id) {
        if (id == null) return false;
        try {
            return analyseMedias.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting analyseMedia: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<AnalyseMedia> findAllAnalyseMedias() {
        try {
            return analyseMedias.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all analyseMedias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Connection Management ==========

    @Override
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                System.out.println("[MongoDataDriver] Connection closed");
            }
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error closing connection: " + e.getMessage());
        }
    }
}