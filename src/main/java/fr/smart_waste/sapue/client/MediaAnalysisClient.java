package fr.smart_waste.sapue.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Client for communicating with the Media Analysis Server via TCP
 */
public class MediaAnalysisClient {

    private final String host;
    private final int port;
    private String mockResponse;

    /**
     * Constructor
     * @param host Media analysis server host
     * @param port Media analysis server port
     */
    public MediaAnalysisClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Set a mock response for testing
     * @param mockResponse The mock response to return
     */
    public void setMockResponse(String mockResponse) {
        this.mockResponse = mockResponse;
    }

    /**
     * Analyze an image by sending it to the media analysis server
     * @param reference Microcontroller reference
     * @param imageBase64 Base64 encoded image
     * @return The analysis result (waste type)
     */
    public String analyzeImage(String imageBase64) {
        if (mockResponse != null) {
            return mockResponse;
        }

        if (imageBase64 == null || imageBase64.isEmpty()) {
            return null;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 5000); // 5s connection timeout
            socket.setSoTimeout(10000); // 10s read timeout

            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                log("Sending image analysis request for reference: " + " (" + imageBase64.length() + " bytes)");
                
                // Format: IMAGE ANALYSE <imageBase64>
                out.print("IMAGE ANALYSE " + imageBase64 + "\n");
                out.flush();

                // The current python server echoes fragments back without newlines if it strips them.
                // For the "real" server, we expect a single line ending in \n.
                // To handle the echo server safely without hanging, we can use a timeout or read what's available.
                // However, readLine() is fine if the server correctly sends a newline.
                // If it doesn't, we'll hit the socket timeout set above.
                String response = in.readLine();
                if (response != null && response.length() > 100) {
                    log("Received response from media analysis server (truncated): " + response.substring(0, 50) + "...");
                } else {
                    log("Received response from media analysis server: " + response);
                }

                if (response != null) {
                    // Extract waste type or handle echo
                    if (response.startsWith("OK")) {
                        return response.substring("OK".length()).trim();
                    } 
                    return response;
                }
            } // Close inner try with resources (PrintWriter, BufferedReader)

            return null;

        } catch (java.net.SocketTimeoutException e) {
            log("Timeout communicating with media analysis server: " + e.getMessage());
            return null;
        } catch (IOException e) {
            log("Error communicating with media analysis server: " + e.getMessage());
            return null;
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void log(String message) {
        System.out.println("[MediaAnalysisClient] " + message);
    }
}
