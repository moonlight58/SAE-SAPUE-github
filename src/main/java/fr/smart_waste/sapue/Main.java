package fr.smart_waste.sapue;

import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.core.SmartWasteServer;

public class Main {

    public static void main(String[] args) {
        try {
            // Load configuration
            String configFile = args.length > 0 ? args[0] : "config.yml";
            ServerConfig config = ServerConfig.loadFromFile(configFile);
            config.validate();

            System.out.println("Configuration loaded:");
            System.out.println(config);

            // Create and start server
            SmartWasteServer server = new SmartWasteServer(config);

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