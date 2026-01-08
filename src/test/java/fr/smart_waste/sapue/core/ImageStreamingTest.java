package fr.smart_waste.sapue.core;

import fr.smart_waste.sapue.client.MediaAnalysisClient;
import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ImageStreamingTest {

    @Mock
    private Socket socket;
    @Mock
    private DataDriver dataDriver;
    @Mock
    private ServerConfig config;
    @Mock
    private ServerMetrics metrics;
    @Mock
    private SmartWasteServer server;
    @Mock
    private MediaAnalysisClient mediaAnalysisClient;

    private ClientHandler handler;
    private PipedOutputStream clientOut;
    private PipedInputStream serverIn;
    private PipedOutputStream serverOut;
    private PipedInputStream clientIn;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        clientOut = new PipedOutputStream();
        serverIn = new PipedInputStream(clientOut);
        
        serverOut = new PipedOutputStream();
        clientIn = new PipedInputStream(serverOut);

        when(socket.getInputStream()).thenReturn(serverIn);
        when(socket.getOutputStream()).thenReturn(serverOut);
        when(socket.getInetAddress()).thenReturn(java.net.InetAddress.getByName("127.0.0.1"));
        when(config.getSocketTimeout()).thenReturn(5000);
        when(config.isVerboseLogging()).thenReturn(true);

        handler = new ClientHandler(socket, dataDriver, config, metrics, server, mediaAnalysisClient);
    }

    @Test
    public void testImageStreamingFlow() throws IOException, InterruptedException {
        // Mock analysis response
        when(mediaAnalysisClient.analyzeImage(anyString(), anyString())).thenReturn("recyclage");

        // Start handler in a separate thread
        Thread handlerThread = new Thread(handler);
        handlerThread.start();

        // Simulate client sending IMG_B64
        PrintWriter writer = new PrintWriter(clientOut, true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));

        writer.println("IMG_B64");
        writer.println("line1base64");
        writer.println("line2base64");
        writer.println(""); // End of stream

        // Wait for response
        String line1 = reader.readLine();
        String line2 = reader.readLine();
        String line3 = reader.readLine();

        // Stop handler
        handler.disconnect();
        handlerThread.join(1000);

        // Verify response (JAUNE = recyclage)
        assertTrue(line1.contains("JAUNE"), "Expected JAUNE for recyclage, got: " + line1);
        assertTrue(line2.equals("45"), "Expected distance 45, got: " + line2);
        assertTrue(line3.equals("00"), "Expected icon 00, got: " + line3);
        
        verify(mediaAnalysisClient).analyzeImage(eq("legacy-device"), contains("line1base64line2base64"));
    }
}
