package fr.smart_waste.sapue.core;

import fr.smart_waste.sapue.core.ServerMetrics;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.config.ServerConfig;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles communication with a single client (microcontroller)
 * Runs in its own thread
 */
public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private final ServerConfig config;
    private final ServerMetrics metrics;
    private final SmartWasteServer server;
    
    private BufferedReader in;
    private PrintWriter out;
    private String microcontrollerReference;
    private boolean running;
    
    public ClientHandler(Socket clientSocket, DataDriver dataDriver, 
                        ServerConfig config, ServerMetrics metrics, 
                        SmartWasteServer server) {
        this.clientSocket = clientSocket;
        this.config = config;
        this.metrics = metrics;
        this.server = server;
        this.running = true;
    }
    
    @Override
    public void run() {
        String clientAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        
        try {
            // Setup socket timeout
            clientSocket.setSoTimeout(config.getSocketTimeout());
            
            // Initialize I/O streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            log("Client connected: " + clientAddress);
            metrics.incrementActiveConnections();
            
            // Main communication loop
            while (running && !clientSocket.isClosed()) {
                String request = in.readLine();
                
                if (request == null) {
                    // Client disconnected
                    break;
                }
                
                // Track metrics
                metrics.incrementRequests();
                metrics.addDataReceived(request.length());
                
                if (config.isVerboseLogging()) {
                    log("Received from " + clientAddress + ": " + request);
                }
                
                // Process request
                String response = processRequest(request);
                
                // Send response
                out.println(response);
                metrics.addDataSent(response.length());
                
                if (config.isVerboseLogging()) {
                    log("Sent to " + clientAddress + ": " + response);
                }
            }
            
        } catch (SocketException e) {
            log("Connection reset: " + clientAddress);
        } catch (Exception e) {
            log("Error handling client " + clientAddress + ": " + e.getMessage());
            metrics.incrementErrors();
            e.printStackTrace();
        } finally {
            cleanup(clientAddress);
        }
    }
    
    /**
     * Process incoming request and return response
     * This is where the protocol parsing will happen
     * @param request Raw request string
     * @return Response string
     */
    private String processRequest(String request) {
        try {
            // Parse request
            String[] parts = request.trim().split("\\s+");
            
            if (parts.length == 0) {
                return "ERR_MALFORMED_REQUEST";
            }
            
            String command = parts[0].toUpperCase();
            
            // Route to appropriate handler
            return switch (command) {
                case "REGISTER" -> handleRegister(parts);
                case "DATA" -> handleData(parts);
                case "PING" -> handlePing(parts);
                case "CONFIG_GET" -> handleConfig(parts);
                case "STATUS" -> handleStatus(parts);
                case "DISCONNECT" -> {
                    running = false;
                    yield "OK";
                }
                default -> "ERR_INVALID_COMMAND";
            };
            
        } catch (Exception e) {
            metrics.incrementErrors();
            e.printStackTrace();
            return "ERR_INTERNAL_ERROR";
        }
    }
    
    /**
     * Handle REGISTER command
     * Example: REGISTER MC-001 192.168.1.100
     */
    private String handleRegister(String[] parts) {
        if (parts.length < 3) {
            return "ERR_MISSING_PARAMS";
        }
        
        String reference = parts[1];
        String ipAddress = parts[2];
        
        // Check if already registered in server
        if (server.isClientRegistered(reference)) {
            // Duplicate connection - handle based on business logic
            log("WARNING: Duplicate registration attempt for " + reference);
            return "ERR_ALREADY_REGISTERED";
        }
        
        // Store reference
        this.microcontrollerReference = reference;
        
        // Register with server
        server.registerClient(reference, this);
        
        log("Microcontroller registered: " + reference + " (" + ipAddress + ")");
        
        return "OK";
    }
    
    /**
     * Handle DATA command
     * Example: DATA temperature=22.5 humidity=65.0
     */
    private String handleData(String[] parts) {
        if (microcontrollerReference == null) {
            return "ERR_DEVICE_NOT_REGISTERED";
        }
        
        if (parts.length < 2) {
            return "ERR_MISSING_PARAMS";
        }
        
        // TODO: Parse sensor data and store in database

        log("Data received from " + microcontrollerReference);
        
        return "OK";
    }
    
    /**
     * Send a message to this client
     * Useful for server-initiated communication
     */
    public void sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            out.println(message);
            metrics.addDataSent(message.length());
        }
    }
    
    /**
     * Get microcontroller reference
     */
    public String getMicrocontrollerReference() {
        return microcontrollerReference;
    }

    public String handlePing(String[] parts) {

        return "OK";
    }

    public String handleConfig(String[] parts) {
        return "OK";
    }

    public String handleStatus(String[] parts) {
        return "OK";
        // return ("OK sensorType:" + microcontrolleur.sensortype + " enable: " + microcontrolleur.isConnected() + "samplingInterval:" + microcontrolleur.samplingInterval)
    }

    /**
     * Check if client is still connected
     */
    public boolean isConnected() {
        return running && !clientSocket.isClosed();
    }
    
    /**
     * Gracefully disconnect client
     */
    public void disconnect() {
        running = false;
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup(String clientAddress) {
        try {
            // Unregister from server
            if (microcontrollerReference != null) {
                server.unregisterClient(microcontrollerReference);
                log("Microcontroller unregistered: " + microcontrollerReference);
            }
            
            // Close streams
            if (in != null) in.close();
            if (out != null) out.close();
            if (!clientSocket.isClosed()) clientSocket.close();
            
            metrics.decrementActiveConnections();
            log("Client disconnected: " + clientAddress);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Log helper method
     */
    private void log(String message) {
        System.out.println("[ClientHandler] " + message);
    }
}
