package fr.smart_waste.sapue.core;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.dataaccess.MongoDataDriver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main TCP Server for Smart Waste application
 * Orchestrates client connections, database access, and server lifecycle
 */
public class SmartWasteServer {

    private final ServerConfig config;
    private final ServerMetrics metrics;
    private final MongoDataDriver dataDriver;
    private final MongoClient mongoClient;

    private ServerSocket serverSocket;
    private volatile boolean running;

    // Registry of connected clients: reference -> ClientHandler
    private final Map<String, ClientHandler> connectedClients;

    /**
     * Constructor
     * @param config Server configuration
     */
    public SmartWasteServer(ServerConfig config) {
        this.config = config;
        this.metrics = new ServerMetrics();
        this.connectedClients = new ConcurrentHashMap<>();

        // Initialize MongoDB connection
        log("Connecting to MongoDB: " + config.getMongoConnectionString());
        this.mongoClient = MongoClients.create(config.getMongoConnectionString());

        // Pass the connection string, NOT mongoClient.toString()
        this.dataDriver = new MongoDataDriver(config.getMongoConnectionString(), config.getDatabaseName());
        if (!dataDriver.init()) {
            System.err.println("Error while initializing data driver");
            return;
        }

        log("Server initialized in " + config.getEnvironment() + " mode");
    }

    /**
     * Start the TCP server
     * Begins accepting client connections
     */
    public void start() {
        if (running) {
            log("Server is already running");
            return;
        }

        try {
            serverSocket = new ServerSocket(config.getServerPort());
            serverSocket.setSoTimeout(1000); // 1 second timeout for accept()
            running = true;

            log("Server started on port " + config.getServerPort());
            log("Max connections: " + config.getMaxConnections());
            log("Waiting for client connections...");

            // Start metrics printer thread if enabled
            if (config.isEnableMetrics()) {
                startMetricsPrinter();
            }

            // Main accept loop
            while (running) {
                try {
                    // Check connection limit
                    if (metrics.getActiveConnections() >= config.getMaxConnections()) {
                        log("WARNING: Max connections reached (" + config.getMaxConnections() + ")");
                        Thread.sleep(1000);
                        continue;
                    }

                    // Accept new connection (with timeout)
                    Socket clientSocket = serverSocket.accept();

                    // Create and start client handler thread
                    ClientHandler handler = new ClientHandler(
                        clientSocket, dataDriver, config, metrics, this
                    );
                    Thread clientThread = new Thread(handler);
                    clientThread.start();

                } catch (SocketTimeoutException e) {
                    // Normal timeout, continue loop
                    continue;
                } catch (IOException e) {
                    if (running) {
                        log("Error accepting connection: " + e.getMessage());
                        metrics.incrementErrors();
                    }
                }
            }

        } catch (Exception e) {
            log("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop the server gracefully
     * Closes all client connections and server socket
     */
    public void stop() {
        if (!running) {
            log("Server is not running");
            return;
        }

        log("Stopping server...");
        running = false;

        try {
            // Disconnect all clients
            log("Disconnecting " + connectedClients.size() + " clients...");
            for (ClientHandler handler : connectedClients.values()) {
                handler.disconnect();
            }
            connectedClients.clear();

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Print final metrics
            if (config.isEnableMetrics()) {
                metrics.printSummary();
            }

            // Close database connection
            dataDriver.close();
            mongoClient.close();

            log("Server stopped successfully");

        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Restart the server
     * Stops and then starts the server with current configuration
     */
    public void restart() {
        log("Restarting server...");
        stop();
        try {
            Thread.sleep(2000); // Wait for cleanup
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start();
    }

    /**
     * Register a client in the server registry
     * @param reference Microcontroller reference
     * @param handler ClientHandler instance
     */
    public void registerClient(String reference, ClientHandler handler) {
        connectedClients.put(reference, handler);
        log("Client registered: " + reference + " (Total: " + connectedClients.size() + ")");
    }

    /**
     * Unregister a client from the server registry
     * @param reference Microcontroller reference
     */
    public void unregisterClient(String reference) {
        connectedClients.remove(reference);
        log("Client unregistered: " + reference + " (Total: " + connectedClients.size() + ")");
    }

    /**
     * Check if a client is already registered
     * @param reference Microcontroller reference
     * @return true if registered
     */
    public boolean isClientRegistered(String reference) {
        return connectedClients.containsKey(reference);
    }

    /**
     * Get a specific client handler
     * @param reference Microcontroller reference
     * @return ClientHandler or null if not found
     */
    public ClientHandler getClientHandler(String reference) {
        return connectedClients.get(reference);
    }

    /**
     * Broadcast message to all connected clients
     * @param message Message to broadcast
     */
    public void broadcastMessage(String message) {
        log("Broadcasting to " + connectedClients.size() + " clients: " + message);
        for (ClientHandler handler : connectedClients.values()) {
            if (handler.isConnected()) {
                handler.sendMessage(message);
            }
        }
    }

    /**
     * Get server metrics
     * @return ServerMetrics instance
     */
    public ServerMetrics getMetrics() {
        return metrics;
    }

    /**
     * Get data driver
     * @return DataDriver instance
     */
    public DataDriver getDataDriver() {
        return dataDriver;
    }

    /**
     * Check if server is running
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Start background thread to print metrics periodically
     */
    private void startMetricsPrinter() {
        Thread metricsThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60000); // Print every 60 seconds
                    if (running && config.isEnableMetrics()) {
                        metrics.printSummary();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        metricsThread.setDaemon(true);
        metricsThread.start();
    }

    /**
     * Log helper method
     */
    private void log(String message) {
        System.out.println("[SmartWasteServer] " + message);
    }

    /**
     * Main entry point
     */
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
