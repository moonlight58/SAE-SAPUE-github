package fr.smart_waste.sapue;

import java.io.*;
import java.net.Socket;

/**
 * Simple test client to verify TCP server functionality
 * Simulates a microcontroller connecting and sending data
 */
public class TestClient {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    public TestClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connected to server: " + host + ":" + port);
    }
    
    /**
     * Send a command and receive response
     */
    public String sendCommand(String command) throws IOException {
        System.out.println("Sending: " + command);
        out.println(command);
        
        String response = in.readLine();
        System.out.println("Received: " + response);
        return response;
    }
    
    /**
     * Close connection
     */
    public void close() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
        System.out.println("Connection closed");
    }
    
    /**
     * Main test scenario
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8888;
        
        try {
            System.out.println("=== Test Client Started ===\n");
            
            TestClient client = new TestClient(host, port);
            
            // Test 1: Register microcontroller
            System.out.println("\n--- Test 1: Register ---");
            String response1 = client.sendCommand("REGISTER MC-001 192.168.1.100");
            assert response1.equals("OK") : "Registration failed";
            
            // Test 2: Send ping
            System.out.println("\n--- Test 2: Ping ---");
            String response2 = client.sendCommand("PING");
            assert response2.equals("OK") : "Ping failed";
            
            // Test 3: Send sensor data (not fully implemented yet)
            System.out.println("\n--- Test 3: Send Data ---");
            String response3 = client.sendCommand("DATA temperature=22.5 humidity=65.0");
            assert response3.equals("OK") : "Data send failed";
            
            // Test 4: Invalid command
            System.out.println("\n--- Test 4: Invalid Command ---");
            String response4 = client.sendCommand("INVALID_CMD");
            assert response4.equals("ERR_INVALID_COMMAND") : "Error handling failed";
            
            // Test 5: Disconnect
            System.out.println("\n--- Test 5: Disconnect ---");
            String response5 = client.sendCommand("DISCONNECT");
            assert response5.equals("OK") : "Disconnect failed";
            
            Thread.sleep(500); // Give server time to process
            client.close();
            
            System.out.println("\n=== All Tests Passed ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
