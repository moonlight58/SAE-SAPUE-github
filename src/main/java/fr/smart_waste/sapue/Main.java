package fr.smart_waste.sapue;

import fr.smart_waste.sapue.client.MediaAnalysisClient;
import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.core.SmartWasteServer;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.dataaccess.MongoDataDriver;

public class Main {

    public static void main(String[] args) {
        try {
            // Load configuration
            String configFile = args.length > 0 ? args[0] : "config.yml";
            ServerConfig config = ServerConfig.loadFromFile(configFile);
            config.validate();

            System.out.println("Configuration loaded:");
            System.out.println(config);

            // Initialize DataDriver
            DataDriver dataDriver = new MongoDataDriver(
                    config.getMongoConnectionString(),
                    config.getDatabaseName()
            );

            // Initialize MediaAnalysisClient
            MediaAnalysisClient mediaAnalysisClient = new MediaAnalysisClient(
                    config.getMediaServerHost(),
                    config.getMediaServerPort()
            );

            // Create and start server
            SmartWasteServer server = new SmartWasteServer(config, dataDriver, mediaAnalysisClient);
            
            System.out.println("Server initialized on port " + config.getServerPort());

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received");
                server.stop();
            }));

            // Start server (blocking call)
            server.start();

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


}