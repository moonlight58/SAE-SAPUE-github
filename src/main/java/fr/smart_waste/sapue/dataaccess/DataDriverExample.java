package fr.smart_waste.sapue.dataaccess;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.dataaccess.MongoDataDriver;
import fr.smart_waste.sapue.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Example demonstrating how to use the DataDriver with MongoDB
 */
public class DataDriverExample {
    
    public static void main(String[] args) {
        // MongoDB connection string (adjust as needed)
        String connectionString = "mongodb://localhost:27017";
        String databaseName = "smartwaste_dev";
        
        // Create MongoDB client
        MongoClient mongoClient = MongoClients.create(connectionString);
        
        // Create DataDriver instance
        DataDriver dataDriver = new MongoDataDriver(mongoClient.toString(), databaseName);
        
        try {
            // Example 1: Insert a Bin
            System.out.println("=== Example 1: Insert a Bin ===");
            Bin bin = new Bin();
            bin.setType("recyclage");
            bin.setPosition(Arrays.asList(6.0240, 47.2378)); // Besançon coordinates
            bin.setIsSAPUE(true);
            
            ObjectId binId = dataDriver.insertBin(bin);
            System.out.println("Inserted Bin with ID: " + binId);
            
            // Example 2: Insert a Microcontrolleur
            System.out.println("\n=== Example 2: Insert a Microcontrolleur ===");
            
            // Create sensor configuration
            Document sensorParams = new Document()
                .append("tempOffset", 0.5)
                .append("pressureUnit", "hPa");
            
            SensorConfig sensorConfig = new SensorConfig();
            sensorConfig.setSensorType("BME280");
            sensorConfig.setEnabled(true);
            sensorConfig.setSamplingInterval(300); // 5 minutes
            sensorConfig.setParameters(sensorParams);
            
            Microcontrolleur mc = new Microcontrolleur();
            mc.setReference("MC-001");
            mc.setPoubelle(binId);
            mc.setIpAddress("192.168.1.100");
            mc.setConfigSensor(sensorConfig);
            
            ObjectId mcId = dataDriver.insertMicrocontrolleur(mc);
            System.out.println("Inserted Microcontrolleur with ID: " + mcId);
            
            // Example 3: Insert a Releve (sensor reading)
            System.out.println("\n=== Example 3: Insert a Releve ===");
            
            Document reading = new Document()
                .append("temperature", 22.5)
                .append("humidity", 65.0)
                .append("pressure", 1013.25);
            
            Releve releve = new Releve();
            releve.setIdControlleur(mcId);
            releve.setDate(new Date());
            releve.setReleve(reading);
            
            ObjectId releveId = dataDriver.insertReleve(releve);
            System.out.println("Inserted Releve with ID: " + releveId);
            
            // Example 4: Find Microcontrolleur by reference
            System.out.println("\n=== Example 4: Find Microcontrolleur ===");
            Microcontrolleur foundMc = dataDriver.findMicrocontrolleurByReference("MC-001");
            if (foundMc != null) {
                System.out.println("Found: " + foundMc);
            }
            
            // Example 5: Find all Releves for a Microcontrolleur
            System.out.println("\n=== Example 5: Find Releves by Microcontrolleur ===");
            List<Releve> releves = dataDriver.findRelevesByControlleur(mcId);
            System.out.println("Found " + releves.size() + " releves:");
            for (Releve r : releves) {
                System.out.println("  - " + r);
            }
            
            // Example 6: Update Microcontrolleur IP
            System.out.println("\n=== Example 6: Update Microcontrolleur ===");
            if (foundMc != null) {
                foundMc.setIpAddress("192.168.1.101");
                boolean updated = dataDriver.updateMicrocontrolleur(foundMc);
                System.out.println("Update successful: " + updated);
            }
            
            // Example 7: Insert a User
            System.out.println("\n=== Example 7: Insert a User ===");
            User user = new User();
            user.setPseudo("john_doe");
            user.setMotDePasse("hashed_password_here");
            user.setMail("john@example.com");
            user.setNumeroTelephone("+33612345678");
            user.setRole("citoyen");
            user.setNiveauConfiance(0.75);
            
            ObjectId userId = dataDriver.insertUser(user);
            System.out.println("Inserted User with ID: " + userId);
            
            // Example 8: Insert a Signalement
            System.out.println("\n=== Example 8: Insert a Signalement ===");
            Signalement signalement = new Signalement();
            signalement.setType("depot_sauvage");
            signalement.setUtilisateur(userId);
            signalement.setPosition(Arrays.asList(6.0250, 47.2380));
            signalement.setImagePath("/images/signalement_001.jpg");
            
            ObjectId signalementId = dataDriver.insertSignalement(signalement);
            System.out.println("Inserted Signalement with ID: " + signalementId);
            
            // Example 9: Insert an AnalyseMedia
            System.out.println("\n=== Example 9: Insert an AnalyseMedia ===");
            Document resultat = new Document()
                .append("wasteDetected", true)
                .append("wasteType", "plastic")
                .append("confidence", 0.95);
            
            AnalyseMedia analyse = new AnalyseMedia();
            analyse.setResultat(resultat);
            
            ObjectId analyseId = dataDriver.insertAnalyseMedia(analyse);
            System.out.println("Inserted AnalyseMedia with ID: " + analyseId);
            
            // Example 10: List all Bins
            System.out.println("\n=== Example 10: List all Bins ===");
            List<Bin> allBins = dataDriver.findAllBins();
            System.out.println("Total bins: " + allBins.size());
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cleanup
            dataDriver.close();
            mongoClient.close();
            System.out.println("\n=== Cleanup completed ===");
        }
    }
}
