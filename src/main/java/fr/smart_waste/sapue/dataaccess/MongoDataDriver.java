package fr.smart_waste.sapue.dataaccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.smart_waste.sapue.Chipset;
import fr.smart_waste.sapue.Measure;
import fr.smart_waste.sapue.Module;
import fr.smart_waste.sapue.model.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import static com.mongodb.client.model.Filters.eq;

public class MongoDataDriver implements DataDriver {

    private final String mongoURL;
    private final String databaseName;
    private final CodecProvider pojoCodecProvider;
    private final CodecRegistry pojoCodecRegistry;
    private MongoClient mongoClient;
    private MongoDatabase database;
    MongoCollection<Measure> measures;
    MongoCollection<Module> modules;
    MongoCollection<Chipset> chipsets;
    MongoCollection<User> users;
    MongoCollection<MapPoint> mapPoints;

    public MongoDataDriver(String mongoURL, String databaseName) {
        this.mongoURL = mongoURL;
        this.databaseName = databaseName;
        pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    }

    public boolean init()  {
        mongoClient = MongoClients.create(mongoURL);
        try {
            database =  mongoClient.getDatabase(databaseName);
            measures = database.getCollection("measures", Measure.class);
            modules = database.getCollection("modules", Module.class);
            chipsets = database.getCollection("chipsets", Chipset.class);
            users = database.getCollection("users", User.class);
            mapPoints = database.getCollection("mapPoints", MapPoint.class);
        }
        catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private ObjectId getModuleId(String moduleKey) {
        Module module = modules.find(eq("key",moduleKey)).first();
        if (module != null) {
            System.out.println(module.getKey()+ " -> "+module.getId());

            return module.getId();
        }
        return null;
    }

    private ObjectId getChipsetId(String chipsetName) {
        Chipset chipset = chipsets.find(eq("name",chipsetName)).first();
        if (chipset != null) {
            System.out.println(chipset.getName()+ " -> "+chipset.getId());

            return chipset.getId();
        }
        return null;
    }

    public synchronized  String autoRegisterModule(String uc, List<String> chipsets) {
        List<ObjectId> lst = new ArrayList<>();
        for(String chipset : chipsets) {
            ObjectId id = getChipsetId(chipset);
            if (id != null) {
                lst.add(id);
            }
        }
        // must generate an unique key
        UUID key = UUID.randomUUID();
        boolean stop = false;
        while(!stop) {
            ObjectId id = getModuleId(key.toString());
            if (id == null) {
                stop = true;
            }
            else {
                key = UUID.randomUUID();
            }
        }
        long nb = modules.estimatedDocumentCount()+1;
        String name = "module "+nb;
        String shortName = "mod"+nb;
        Module m = new Module(name, shortName, key.toString(), uc, lst);
        modules.insertOne(m);
        return "OK "+m.getName()+","+m.getShortName()+","+m.getKey();
    }

    public synchronized String saveMeasure(String type, String date, String value, String moduleKey) {

        ObjectId idModule = getModuleId(moduleKey);
        if (idModule == null) {
            return "ERR invalid module key";
        }
        Measure m = new Measure(type, LocalDateTime.parse(date), value, idModule);
        measures.insertOne(m);
        return "OK";
    }

    public synchronized String saveAnalysis(String type, String date, String value) {
        Measure m = new Measure(type, LocalDateTime.parse(date), value, null);
        measures.insertOne(m);
        return "OK";
    }

    @Override
    public synchronized ObjectId insertUser(User user) {
        if (user == null) return null;
        MongoCollection<User> col = users != null ? users :
                (database != null ? database.getCollection("users", User.class) : null);
        if (col == null) return null;
        try {
            InsertOneResult res = col.insertOne(user);
            if (res.getInsertedId() != null) {
                return res.getInsertedId().asObjectId().getValue();
            }
            try { return user.getId(); } catch (Exception ignored) { return null; }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User findUserById(ObjectId id) {
        if (id == null) return null;
        MongoCollection<User> col = users != null ? users :
                (database != null ? database.getCollection("users", User.class) : null);
        if (col == null) return null;
        try {
            return col.find(eq("_id", id)).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User findUserByPseudo(String pseudo) {
        if (pseudo == null) return null;
        MongoCollection<User> col = users != null ? users :
                (database != null ? database.getCollection("users", User.class) : null);
        if (col == null) return null;
        try {
            return col.find(eq("pseudo", pseudo)).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null) return false;
        MongoCollection<User> col = users != null ? users :
                (database != null ? database.getCollection("users", User.class) : null);
        if (col == null) return false;
        try {
            com.mongodb.client.result.UpdateResult ur = col.replaceOne(eq("_id", user.getId()), user);
            return ur.getMatchedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteUser(ObjectId id) {
        if (id == null) return false;
        MongoCollection<User> col = users != null ? users :
                (database != null ? database.getCollection("users", User.class) : null);
        if (col == null) return false;
        try {
            com.mongodb.client.result.DeleteResult dr = col.deleteOne(eq("_id", id));
            return dr.getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<User> findAllUsers() {
        MongoCollection<User> col = users != null ? users :
                (database != null ? database.getCollection("users", User.class) : null);
        if (col == null) return new ArrayList<>();
        try {
            return col.find().into(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public synchronized ObjectId insertMapPoint(MapPoint mapPoint) {
        if (mapPoint == null || database == null) return null;
        try {
            com.mongodb.client.MongoCollection<MapPoint> col = database.getCollection("mapPoints", MapPoint.class);
            com.mongodb.client.result.InsertOneResult r = col.insertOne(mapPoint);
            if (r.getInsertedId() != null) return r.getInsertedId().asObjectId().getValue();
            try { return mapPoint.getId(); } catch (Exception ignored) { return null; }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    @Override
    public MapPoint findMapPointById(ObjectId id) {
        if (id == null || database == null) return null;
        return database.getCollection("mapPoints", MapPoint.class).find(eq("_id", id)).first();
    }

    @Override
    public boolean updateMapPoint(MapPoint mapPoint) {
        if (mapPoint == null || database == null) return false;
        try {
            com.mongodb.client.result.UpdateResult ur = database.getCollection("mapPoints", MapPoint.class)
                    .replaceOne(eq("_id", mapPoint.getId()), mapPoint);
            return ur.getModifiedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteMapPoint(ObjectId id) {
        if (id == null || database == null) return false;
        try {
            com.mongodb.client.result.DeleteResult dr = database.getCollection("mapPoints", MapPoint.class)
                    .deleteOne(eq("_id", id));
            return dr.getDeletedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public List<MapPoint> findAllMapPoints() {
        if (database == null) return new ArrayList<>();
        return database.getCollection("mapPoints", MapPoint.class).find().into(new ArrayList<>());
    }

    @Override
    public synchronized ObjectId insertMicrocontrolleur(Microcontrolleur m) {
        if (m == null || database == null) return null;
        try {
            com.mongodb.client.result.InsertOneResult r = database.getCollection("microcontrolleurs", Microcontrolleur.class)
                    .insertOne(m);
            if (r.getInsertedId() != null) return r.getInsertedId().asObjectId().getValue();
            try { return m.getId(); } catch (Exception ignored) { return null; }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    @Override
    public Microcontrolleur findMicrocontrolleurById(ObjectId id) {
        if (id == null || database == null) return null;
        return database.getCollection("microcontrolleurs", Microcontrolleur.class).find(eq("_id", id)).first();
    }

    @Override
    public Microcontrolleur findMicrocontrolleurByReference(String reference) {
        if (reference == null || database == null) return null;
        return database.getCollection("microcontrolleurs", Microcontrolleur.class).find(eq("reference", reference)).first();
    }

    @Override
    public boolean updateMicrocontrolleur(Microcontrolleur m) {
        if (m == null || database == null) return false;
        try {
            com.mongodb.client.result.UpdateResult ur = database.getCollection("microcontrolleurs", Microcontrolleur.class)
                    .replaceOne(eq("_id", m.getId()), m);
            return ur != null && ur.getModifiedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteMicrocontrolleur(ObjectId id) {
        if (id == null || database == null) return false;
        try {
            com.mongodb.client.result.DeleteResult dr = database.getCollection("microcontrolleurs", Microcontrolleur.class)
                    .deleteOne(eq("_id", id));
            return dr.getDeletedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public List<Microcontrolleur> findAllMicrocontrolleurs() {
        if (database == null) return new ArrayList<>();
        return database.getCollection("microcontrolleurs", Microcontrolleur.class).find().into(new ArrayList<>());
    }

    @Override
    public synchronized ObjectId insertSignalement(Signalement s) {
        if (s == null || database == null) return null;
        try {
            com.mongodb.client.result.InsertOneResult r = database.getCollection("signalements", Signalement.class).insertOne(s);
            if (r.getInsertedId() != null) return r.getInsertedId().asObjectId().getValue();
            try { return s.getId(); } catch (Exception ignored) { return null; }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    @Override
    public Signalement findSignalementById(ObjectId id) {
        if (id == null || database == null) return null;
        return database.getCollection("signalements", Signalement.class).find(eq("_id", id)).first();
    }

    @Override
    public boolean updateSignalement(Signalement s) {
        if (s == null || database == null) return false;
        try {
            com.mongodb.client.result.UpdateResult ur = database.getCollection("signalements", Signalement.class)
                    .replaceOne(eq("_id", s.getId()), s);
            return ur != null && ur.getModifiedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteSignalement(ObjectId id) {
        if (id == null || database == null) return false;
        try {
            com.mongodb.client.result.DeleteResult dr = database.getCollection("signalements", Signalement.class)
                    .deleteOne(eq("_id", id));
            return dr.getDeletedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public List<Signalement> findAllSignalements() {
        if (database == null) return new ArrayList<>();
        return database.getCollection("signalements", Signalement.class).find().into(new ArrayList<>());
    }

    @Override
    public synchronized ObjectId insertReleve(Releve r) {
        if (r == null || database == null) return null;
        try {
            com.mongodb.client.result.InsertOneResult res = database.getCollection("releves", Releve.class).insertOne(r);
            if (res.getInsertedId() != null) return res.getInsertedId().asObjectId().getValue();
            try { return r.getId(); } catch (Exception ignored) { return null; }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    @Override
    public Releve findReleveById(ObjectId id) {
        if (id == null || database == null) return null;
        return database.getCollection("releves", Releve.class).find(eq("_id", id)).first();
    }

    @Override
    public List<Releve> findRelevesByControlleur(ObjectId idControlleur) {
        if (idControlleur == null || database == null) return new ArrayList<>();
        return database.getCollection("releves", Releve.class).find(eq("microcontrolleurId", idControlleur)).into(new ArrayList<>());
    }

    @Override
    public boolean updateReleve(Releve r) {
        if (r == null || database == null) return false;
        try {
            com.mongodb.client.result.UpdateResult ur = database.getCollection("releves", Releve.class)
                    .replaceOne(eq("_id", r.getId()), r);
            return ur.getModifiedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteReleve(ObjectId id) {
        if (id == null || database == null) return false;
        try {
            com.mongodb.client.result.DeleteResult dr = database.getCollection("releves", Releve.class)
                    .deleteOne(eq("_id", id));
            return dr.getDeletedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public List<Releve> findAllReleves() {
        if (database == null) return new ArrayList<>();
        return database.getCollection("releves", Releve.class).find().into(new ArrayList<>());
    }

    @Override
    public synchronized ObjectId insertAnalyseMedia(AnalyseMedia a) {
        if (a == null || database == null) return null;
        try {
            com.mongodb.client.result.InsertOneResult r = database.getCollection("analyseMedias", AnalyseMedia.class).insertOne(a);
            if (r.getInsertedId() != null) return r.getInsertedId().asObjectId().getValue();
            try { return a.getId(); } catch (Exception ignored) { return null; }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    @Override
    public AnalyseMedia findAnalyseMediaById(ObjectId id) {
        if (id == null || database == null) return null;
        return database.getCollection("analyseMedias", AnalyseMedia.class).find(eq("_id", id)).first();
    }

    @Override
    public boolean updateAnalyseMedia(AnalyseMedia a) {
        if (a == null || database == null) return false;
        try {
            com.mongodb.client.result.UpdateResult ur = database.getCollection("analyseMedias", AnalyseMedia.class)
                    .replaceOne(eq("_id", a.getId()), a);
            return ur.getModifiedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteAnalyseMedia(ObjectId id) {
        if (id == null || database == null) return false;
        try {
            com.mongodb.client.result.DeleteResult dr = database.getCollection("analyseMedias", AnalyseMedia.class)
                    .deleteOne(eq("_id", id));
            return dr.getDeletedCount() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public List<AnalyseMedia> findAllAnalyseMedias() {
        if (database == null) return new ArrayList<>();
        return database.getCollection("analyseMedias", AnalyseMedia.class).find().into(new ArrayList<>());
    }

    @Override
    public void close() {
        try {
            if (mongoClient != null) mongoClient.close();
        } catch (Exception ignored) {}
    }

}