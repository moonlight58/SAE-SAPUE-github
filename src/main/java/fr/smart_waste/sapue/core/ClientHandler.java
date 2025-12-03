package fr.smart_waste.sapue.core;

import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.dataaccess.MongoDataDriver;
import fr.smart_waste.sapue.protocol.CommandHandler;
import fr.smart_waste.sapue.protocol.ProtocolParser;
import fr.smart_waste.sapue.protocol.ProtocolRequest;
import fr.smart_waste.sapue.protocol.ProtocolException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles communication with a single client (microcontroller)
 * Runs in its own thread
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final MongoDataDriver dataDriver;
    private final ServerConfig config;
    private final ServerMetrics metrics;
    private final SmartWasteServer server;

    private BufferedReader in;
    private PrintWriter out;
    private String microcontrollerReference;
    private boolean running;

    public ClientHandler(Socket clientSocket, MongoDataDriver dataDriver,
                         ServerConfig config, ServerMetrics metrics,
                         SmartWasteServer server) {
        this.clientSocket = clientSocket;
        this.dataDriver = dataDriver;
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
     * Process incoming request using ProtocolParser and CommandHandler
     * @param request Raw request string
     * @return Response string
     */
    private String processRequest(String request) {
        try {
            // Parse request using ProtocolParser
            ProtocolRequest parsedRequest = ProtocolParser.parse(request);

            // Execute command using CommandHandler
            CommandHandler commandHandler = new CommandHandler(dataDriver, server);
            String response = commandHandler.execute(parsedRequest);

            // Register client ONLY after successful REGISTER command
            if ("REGISTER".equals(parsedRequest.getCommand()) && "OK".equals(response)) {
                this.microcontrollerReference = parsedRequest.getReference();
                server.registerClient(microcontrollerReference, this);
            }

            if ("DATA".equals(parsedRequest.getCommand()) && "OK".equals(response)) {
                // Data received successfully
            }

            if ("PING".equals(parsedRequest.getCommand()) && "PONG".equals(response)) {
                // Ping-Pong successful
            }

            if ("CONFIG_GET".equals(parsedRequest.getCommand())) {
                // Configuration sent
            }

            // Handle DISCONNECT command
            if ("DISCONNECT".equals(parsedRequest.getCommand()) && "OK".equals(response)) {
                running = false;
            }

            return response;

        } catch (ProtocolException e) {
            // Protocol parsing error - return error code
            if (config.isVerboseLogging()) {
                log("Protocol error: " + e.getMessage());
            }
            return e.getResponse();

        } catch (Exception e) {
            // Unexpected error
            metrics.incrementErrors();
            log("Error processing request: " + e.getMessage());
            e.printStackTrace();
            return "ERR_INTERNAL_ERROR";
        }
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