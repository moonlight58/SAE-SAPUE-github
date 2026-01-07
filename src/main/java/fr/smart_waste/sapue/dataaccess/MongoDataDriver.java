package fr.smart_waste.sapue.dataaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import fr.smart_waste.sapue.model.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import static com.mongodb.client.model.Filters.*;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB implementation of DataDriver interface
 * Provides direct database access for Smart Waste application
 */
public class MongoDataDriver implements DataDriver {

    private final String mongoURL;
    private final String databaseName;
    private final CodecProvider pojoCodecProvider;
    private final CodecRegistry pojoCodecRegistry;

    private String mediaServerHost;
    private int mediaServerPort;

    private MongoClient mongoClient;
    private MongoDatabase database;

    // Collections
    private MongoCollection<Modules> modules;
    private MongoCollection<Chipsets> chipsets;
    private MongoCollection<Users> users;
    private MongoCollection<Reports> reports;
    private MongoCollection<MapPoints> mapPoints;
    private MongoCollection<Measurements> measurements;
    private MongoCollection<AnalyseMedia> analyseMedias;

    /**
     * Constructor
     * @param mongoURL MongoDB connection string
     * @param databaseName Database name
     */
    public MongoDataDriver(String mongoURL, String databaseName) {
        this(mongoURL, databaseName, "localhost", 50060);
    }

    /**
     * Constructor with media server config
     * @param mongoURL MongoDB connection string
     * @param databaseName Database name
     * @param mediaServerHost Media analysis server host
     * @param mediaServerPort Media analysis server port
     */
    public MongoDataDriver(String mongoURL, String databaseName, String mediaServerHost, int mediaServerPort) {
        this.mongoURL = mongoURL;
        this.databaseName = databaseName;
        this.mediaServerHost = mediaServerHost;
        this.mediaServerPort = mediaServerPort;

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
            modules = database.getCollection("Modules", Modules.class);
            chipsets = database.getCollection("Chipsets", Chipsets.class);
            users = database.getCollection("Users", Users.class);
            reports = database.getCollection("Reports", Reports.class);
            mapPoints = database.getCollection("MapPoints", MapPoints.class);
            measurements = database.getCollection("Measurements", Measurements.class);
            analyseMedias = database.getCollection("analyseMedias", AnalyseMedia.class);

            System.out.println("[MongoDataDriver] Connected to database: " + databaseName);
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("[MongoDataDriver] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            return false;
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



    // ========== Measurements Operations ==========

    @Override
    public synchronized ObjectId insertMeasurement(Measurements measurement) {
        if (measurement == null) return null;
        try {
            InsertOneResult result = measurements.insertOne(measurement);
            return result.getInsertedId() != null
                    ? result.getInsertedId().asObjectId().getValue()
                    : measurement.getId();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error inserting measurement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Measurements findMeasurementById(ObjectId id) {
        if (id == null) return null;
        try {
            return measurements.find(eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding measurement: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Measurements> findMeasurementsByController(ObjectId idController) {
        if (idController == null) return new ArrayList<>();
        try {
            return measurements.find(eq("id_Controller", idController)).into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding measurements by controller: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateMeasurement(Measurements measurement) {
        if (measurement == null || measurement.getId() == null) return false;
        try {
            return measurements.replaceOne(eq("_id", measurement.getId()), measurement).getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error updating measurement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMeasurement(ObjectId id) {
        if (id == null) return false;
        try {
            return measurements.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error deleting measurement: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Measurements> findAllMeasurements() {
        try {
            return measurements.find().into(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("[MongoDataDriver] Error finding all measurements: " + e.getMessage());
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

    @Override
    public String analyzeImage(String imageBase64) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return null;
        }

        try (Socket socket = new Socket(mediaServerHost, mediaServerPort);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // The protocol with the analysis server: send the base64, get the result
            // Format: ANALYSE <imageBase64>
            out.println("ANALYSE " + imageBase64);

            String response = in.readLine();
            if (response != null && response.startsWith("OK type:")) {
                return response.substring("OK type:".length()).trim();
            } else {
                System.err.println("[MongoDataDriver] Media server returned error or unexpected response: " + response);
                return null;
            }

        } catch (IOException e) {
            System.err.println("[MongoDataDriver] Error communicating with media analysis server: " + e.getMessage());
            return null;
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
