package fr.smart_waste.sapue.core;

import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.client.MediaAnalysisClient;
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
    private final DataDriver dataDriver;
    private final MediaAnalysisClient mediaAnalysisClient;

    private ServerSocket serverSocket;
    private volatile boolean running;

    // Registry of connected clients: reference -> ClientHandler
    private final Map<String, ClientHandler> connectedClients;

    /**
     * Constructor
     * @param config Server configuration
     * @param dataDriver Data access driver
     * @param mediaAnalysisClient Media analysis client
     */
    public SmartWasteServer(ServerConfig config, DataDriver dataDriver, MediaAnalysisClient mediaAnalysisClient) {
        this.config = config;
        this.metrics = new ServerMetrics();
        this.connectedClients = new ConcurrentHashMap<>();
        this.dataDriver = dataDriver;
        this.mediaAnalysisClient = mediaAnalysisClient;

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
            log("Socket timeout: " + config.getSocketTimeout() + "ms");
            log("Database: " + config.getDatabaseName());
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
                            clientSocket, dataDriver, config, metrics, this, mediaAnalysisClient
                    );
                    Thread clientThread = new Thread(handler);
                    clientThread.setName("ClientHandler-" + clientSocket.getInetAddress().getHostAddress());
                    clientThread.start();

                } catch (SocketTimeoutException e) {
                    // Normal timeout, continue loop
                    continue;
                } catch (IOException e) {
                    if (running) {
                        log("Error accepting connection: " + e.getMessage());
                        metrics.incrementErrors();
                    }
                } catch (InterruptedException e) {
                    log("Sleep interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

        } catch (IOException e) {
            log("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
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
            if (!connectedClients.isEmpty()) {
                log("Disconnecting " + connectedClients.size() + " clients...");
                for (ClientHandler handler : connectedClients.values()) {
                    handler.disconnect();
                }
                connectedClients.clear();
            }

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
            log("Restart interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        start();
    }

    /**
     * Register a client in the server registry
     * @param reference Microcontroller reference
     * @param handler ClientHandler instance
     * @return true if registered, false if already exists
     */
    public synchronized boolean registerClient(String reference, ClientHandler handler) {
        if (connectedClients.containsKey(reference)) {
            log("Registration failed: " + reference + " already registered");
            return false;
        }

        connectedClients.put(reference, handler);
        log("Client registered: " + reference + " (Total: " + connectedClients.size() + ")");
        return true;
    }

    /**
     * Unregister a client from the server registry
     * @param reference Microcontroller reference
     */
    public synchronized void unregisterClient(String reference) {
        if (connectedClients.remove(reference) != null) {
            log("Client unregistered: " + reference + " (Total: " + connectedClients.size() + ")");
        }
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
     * Get count of connected clients
     * @return Number of currently connected clients
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }

    /**
     * Broadcast message to all connected clients
     * @param message Message to broadcast
     */
    public void broadcastMessage(String message) {
        if (connectedClients.isEmpty()) {
            log("No clients to broadcast to");
            return;
        }

        log("Broadcasting to " + connectedClients.size() + " clients: " + message);
        int successCount = 0;

        for (ClientHandler handler : connectedClients.values()) {
            if (handler.isConnected()) {
                handler.sendMessage(message);
                successCount++;
            }
        }

        log("Broadcast sent to " + successCount + "/" + connectedClients.size() + " clients");
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
     * Get server configuration
     * @return ServerConfig instance
     */
    public ServerConfig getConfig() {
        return config;
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
        metricsThread.setName("MetricsPrinter");
        metricsThread.setDaemon(true);
        metricsThread.start();

        log("Metrics printer started (interval: 60s)");
    }

    /**
     * Cleanup resources on shutdown
     */
    private void cleanup() {
        log("Running cleanup...");

        // Ensure all clients are disconnected
        if (!connectedClients.isEmpty()) {
            for (ClientHandler handler : connectedClients.values()) {
                handler.disconnect();
            }
            connectedClients.clear();
        }

        // Ensure server socket is closed
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log("Error closing server socket: " + e.getMessage());
            }
        }

        log("Cleanup complete");
    }

    /**
     * Log helper method with timestamp
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

            System.out.println("========================================");
            System.out.println("  Smart Waste TCP Server");
            System.out.println("========================================");
            System.out.println("Configuration loaded from: " + configFile);
            System.out.println(config);
            System.out.println("========================================\n");

            // Initialize DataDriver
            MongoDataDriver dataDriver = new MongoDataDriver(
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

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Shutdown Hook] Shutdown signal received");
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