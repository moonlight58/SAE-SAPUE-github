package fr.smart_waste.sapue.mocks;

import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.core.SmartWasteServer;

import java.util.HashSet;
import java.util.Set;

public class MockSmartWasteServer extends SmartWasteServer {
    
    private boolean isRunning = false;
    private Set<String> registeredClients = new HashSet<>();
    
    public MockSmartWasteServer(ServerConfig config) {
        super(config, null, null); // Pass nulls as we override methods that use them
    }
    
    @Override
    public void start() { isRunning = true; }
    
    @Override
    public boolean isRunning() { return isRunning; }
    
    public void setRunning(boolean running) { this.isRunning = true; }
    
    @Override
    public boolean isClientRegistered(String reference) {
        return registeredClients.contains(reference);
    }
    
    @Override
    public boolean registerClient(String reference, fr.smart_waste.sapue.core.ClientHandler handler) {
        registeredClients.add(reference);
        return true;
    }
}
