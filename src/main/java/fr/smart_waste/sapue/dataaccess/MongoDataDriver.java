package fr.smart_waste.sapue.dataaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fr.smart_waste.sapue.model.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

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
    private MongoCollection<Modules> modules;
    private MongoCollection<Chipsets> chipsets;
    private MongoCollection<Users> users;
    private MongoCollection<Signalements> signalements;
    private MongoCollection<Reports> reports;
    private MongoCollection<MapPoints> mapPoints;
    private MongoCollection<Releves> releves;
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
            modules = database.getCollection("Modules", Modules.class);
            chipsets = database.getCollection("Chipsets", Chipsets.class);
            users = database.getCollection("Users", Users.class);
            signalements = database.getCollection("signalements", Signalements.class);
            reports = database.getCollection("Reports", Reports.class);
            mapPoints = database.getCollection("MapPoints", MapPoints.class);
            releves = database.getCollection("releves", Releves.class);
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
    public Poubelles findPoubelleByModule(String moduleKey) {
        if (moduleKey == null || moduleKey.isEmpty()) return null;
        try {
            return poubelles.find(eq("hardwareConfig.microcontroller", moduleKey)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding poubelle by module: " + e.getMessage());
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

    // ========== Module Operations ==========

    @Override
    public synchronized ObjectId insertModule(Modules module) {
        if (module == null) return null;
        try {
            InsertOneResult result = modules.insertOne(module);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : module.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting module: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Modules findModuleById(ObjectId id) {
        if (id == null) return null;
        try {
            return modules.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding module by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Modules findModuleByKey(String key) {
        if (key == null || key.isEmpty()) return null;
        try {
            return modules.find(eq("key", key)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding module by key: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateModule(Modules module) {
        if (module == null || module.getId() == null) return false;
        try {
            return modules.replaceOne(eq("_id", module.getId()), module).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating module: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteModule(ObjectId id) {
        if (id == null) return false;
        try {
            return modules.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting module: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Modules> findAllModules() {
        try {
            return modules.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all modules: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Chipsets Operations ==========

    @Override
    public synchronized ObjectId insertChipset(Chipsets chipset) {
        if (chipset == null) return null;
        try {
            InsertOneResult result = chipsets.insertOne(chipset);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : chipset.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting chipset: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Chipsets findChipsetById(ObjectId id) {
        if (id == null) return null;
        try {
            return chipsets.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding chipset by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Chipsets> findChipsetsByModuleId(ObjectId moduleId) {
        if (moduleId == null) return new ArrayList<>();
        try {
            return chipsets.find(eq("moduleID", moduleId)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding chipsets by module ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateChipset(Chipsets chipset) {
        if (chipset == null || chipset.getId() == null) return false;
        try {
            return chipsets.replaceOne(eq("_id", chipset.getId()), chipset).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating chipset: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteChipset(ObjectId id) {
        if (id == null) return false;
        try {
            return chipsets.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting chipset: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Chipsets> findAllChipsets() {
        try {
            return chipsets.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all chipsets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Users Operations Implementation ==========

    @Override
    public Users findUserById(ObjectId id) {
        if (id == null) return null;
        try {
            return users.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding user by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Users findUserByMail(String mail) {
        if (mail == null || mail.isEmpty()) return null;
        try {
            return users.find(eq("mail", mail)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding user by mail: " + e.getMessage());
            return null;
        }
    }

    @Override
    public synchronized ObjectId insertUser(Users user) {
        if (user == null) return null;
        try {
            InsertOneResult result = users.insertOne(user);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : user.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateUser(Users user) {
        if (user == null || user.getId() == null) return false;
        try {
            return users.replaceOne(eq("_id", user.getId()), user).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteUser(ObjectId id) {
        if (id == null) return false;
        try {
            return users.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Users> findAllUsers() {
        try {
            return users.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Signalement Operations ==========

    @Override
    public synchronized ObjectId insertSignalements(Signalements s) {
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
    public Signalements findSignalementsById(ObjectId id) {
        if (id == null) return null;
        try {
            return signalements.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding signalement: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateSignalements(Signalements s) {
        if (s == null || s.getId() == null) return false;
        try {
            return signalements.replaceOne(eq("_id", s.getId()), s).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating signalement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSignalements(ObjectId id) {
        if (id == null) return false;
        try {
            return signalements.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting signalement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Signalements> findAllSignalements() {
        try {
            return signalements.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all signalements: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Releve Operations ==========

    @Override
    public synchronized ObjectId insertReleve(Releves r) {
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
    public Releves findReleveById(ObjectId id) {
        if (id == null) return null;
        try {
            return releves.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding releve: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Releves> findRelevesByPoubelle(ObjectId idPoubelle) {
        if (idPoubelle == null) return new ArrayList<>();
        try {
            return releves.find(eq("idPoubelle", idPoubelle)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding releves by poubelle: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find recent releves for a poubelle
     * @param idPoubelle Poubelle ObjectId
     * @param limit Maximum number of results
     * @return List of recent releves, sorted by timestamp descending
     */
    public List<Releves> findRecentRelevesByPoubelle(ObjectId idPoubelle, int limit) {
        if (idPoubelle == null || limit <= 0) return new ArrayList<>();
        try {
            return releves.find(eq("idPoubelle", idPoubelle))
                    .sort(new Document("timestamp", -1))
                    .limit(limit)
                    .into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding recent releves: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find releves within date range for a poubelle
     * @param idPoubelle Poubelle ObjectId
     * @param startDate Start date
     * @param endDate End date
     * @return List of releves within the date range
     */
    public List<Releves> findRelevesByDateRange(ObjectId idPoubelle, Date startDate, Date endDate) {
        if (idPoubelle == null || startDate == null || endDate == null) return new ArrayList<>();
        try {
            return releves.find(
                    and(
                            eq("idPoubelle", idPoubelle),
                            gte("timestamp", startDate),
                            lte("timestamp", endDate)
                    )
            ).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding releves by date range: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get latest releve for a poubelle
     * @param idPoubelle Poubelle ObjectId
     * @return Latest Releve or null
     */
    public Releves findLatestReleveByPoubelle(ObjectId idPoubelle) {
        if (idPoubelle == null) return null;
        try {
            return releves.find(eq("idPoubelle", idPoubelle))
                    .sort(new Document("timestamp", -1))
                    .first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding latest releve: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateReleve(Releves r) {
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
    public List<Releves> findAllReleves() {
        try {
            return releves.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all releves: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Delete old releves before a certain date (for data cleanup)
     * @param beforeDate Delete releves before this date
     * @return Number of deleted documents
     */
    public long deleteRelevesBefore(Date beforeDate) {
        if (beforeDate == null) return 0;
        try {
            return releves.deleteMany(lt("timestamp", beforeDate)).getDeletedCount();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting old releves: " + e.getMessage());
            return 0;
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
    // ==========================================
    // Reports Operations
    // ==========================================

    @Override
    public synchronized ObjectId insertReport(Reports report) {
        if (report == null) return null;
        try {
            InsertOneResult result = reports.insertOne(report);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : report.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Reports findReportById(ObjectId id) {
        if (id == null) return null;
        try {
            return reports.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding report by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Reports> findReportsByStatus(String status) {
        if (status == null ||status.isEmpty()) return new ArrayList<>();
        try {
            return reports.find(eq("status", status)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding reports by status: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Reports> findReportsByMapPoint(ObjectId mapPointId) {
        if (mapPointId == null) return new ArrayList<>();
        try {
            return reports.find(eq("mapPoint", mapPointId)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding reports by map point: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateReport(Reports report) {
        if (report == null || report.getId() == null) return false;
        try {
            return reports.replaceOne(eq("_id", report.getId()), report).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating report: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteReport(ObjectId id) {
        if (id == null) return false;
        try {
            return reports.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting report: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Reports> findAllReports() {
        try {
            return reports.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all reports: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==========================================
    // MapPoints Operations
    // ==========================================

    @Override
    public synchronized ObjectId insertMapPoint(MapPoints mapPoint) {
        if (mapPoint == null) return null;
        try {
            InsertOneResult result = mapPoints.insertOne(mapPoint);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : mapPoint.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting map point: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MapPoints findMapPointById(ObjectId id) {
        if (id == null) return null;
        try {
            return mapPoints.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding map point by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<MapPoints> findMapPointsByType(String type) {
        if (type == null || type.isEmpty()) return new ArrayList<>();
        try {
            return mapPoints.find(eq("type", type)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding map points by type: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<MapPoints> findMapPointsNear(double longitude, double latitude, double maxDistanceMeters) {
        try {
            // GeoJSON query for nearby points
            Document geoNearQuery = new Document("location",
                    new Document("$near", new Document("$geometry",
                            new Document("type", "Point")
                                    .append("coordinates", Arrays.asList(longitude, latitude)))
                            .append("$maxDistance", maxDistanceMeters)));
            
            return mapPoints.find(geoNearQuery).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding nearby map points: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public MapPoints findMapPointByModule(String moduleKey) {
        if (moduleKey == null || moduleKey.isEmpty()) return null;
        try {
            // Find MapPoint where hardwareConfig.modules array contains a module with this key
            // Note: This requires looking up the module ObjectId first
            Modules module = findModuleByKey(moduleKey);
            if (module == null || module.getId() == null) return null;
            
            return mapPoints.find(eq("hardwareConfig.modules", module.getId())).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding map point by module: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateMapPointLastMeasurement(ObjectId mapPointId, MapPoints.LastMeasurement lastMeasurement) {
        if (mapPointId == null || lastMeasurement == null) return false;
        try {
            Document updateDoc = new Document("$set", new Document("lastMeasurement", lastMeasurement));
            return mapPoints.updateOne(eq("_id", mapPointId), updateDoc).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating map point last measurement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateMapPoint(MapPoints mapPoint) {
        if (mapPoint == null || mapPoint.getId() == null) return false;
        try {
            return mapPoints.replaceOne(eq("_id", mapPoint.getId()), mapPoint).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating map point: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMapPoint(ObjectId id) {
        if (id == null) return false;
        try {
            return mapPoints.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting map point: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<MapPoints> findAllMapPoints() {
        try {
            return mapPoints.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all map points: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<MapPoints> findMapPointsWithActiveAlerts() {
        try {
            return mapPoints.find(eq("activeAlerts.hasIssue", true)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding map points with active alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
