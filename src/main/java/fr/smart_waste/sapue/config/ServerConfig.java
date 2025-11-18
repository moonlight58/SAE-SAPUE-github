package fr.smart_waste.sapue.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Configuration holder for Smart Waste TCP Server
 * Loads settings from YAML configuration file
 */
public class ServerConfig {
    
    // Server settings
    private int serverPort;
    private int maxConnections;
    private int socketTimeout;
    
    // MongoDB settings
    private String mongoConnectionString;
    private String databaseName;
    private String environment; // "dev" or "prod"
    
    // Logging settings
    private boolean enableMetrics;
    private boolean verboseLogging;
    
    /**
     * Load configuration from YAML file
     * @param configFilePath Path to config.yml file
     * @return ServerConfig instance
     * @throws Exception if file not found or invalid
     */
    public static ServerConfig loadFromFile(String configFilePath) throws Exception {
        ServerConfig config = new ServerConfig();
        
        try (InputStream input = new FileInputStream(configFilePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            
            // Load server settings
            Map<String, Object> serverData = (Map<String, Object>) data.get("server");
            config.serverPort = (Integer) serverData.get("port");
            config.maxConnections = (Integer) serverData.getOrDefault("maxConnections", 100);
            config.socketTimeout = (Integer) serverData.getOrDefault("socketTimeout", 30000);
            
            // Load MongoDB settings
            Map<String, Object> mongoData = (Map<String, Object>) data.get("mongodb");
            config.mongoConnectionString = (String) mongoData.get("connectionString");
            config.databaseName = (String) mongoData.get("databaseName");
            config.environment = (String) mongoData.getOrDefault("environment", "dev");
            
            // Load logging settings
            Map<String, Object> loggingData = (Map<String, Object>) data.get("logging");
            config.enableMetrics = (Boolean) loggingData.getOrDefault("enableMetrics", true);
            config.verboseLogging = (Boolean) loggingData.getOrDefault("verbose", false);
            
        }
        
        return config;
    }
    
    /**
     * Validate configuration values
     * @throws IllegalArgumentException if invalid
     */
    public void validate() {
        if (serverPort < 1024 || serverPort > 65535) {
            throw new IllegalArgumentException("Server port must be between 1024 and 65535");
        }
        if (maxConnections < 1) {
            throw new IllegalArgumentException("Max connections must be at least 1");
        }
        if (mongoConnectionString == null || mongoConnectionString.isEmpty()) {
            throw new IllegalArgumentException("MongoDB connection string cannot be empty");
        }
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be empty");
        }
    }
    
    // Getters
    public int getServerPort() {
        return serverPort;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public int getSocketTimeout() {
        return socketTimeout;
    }
    
    public String getMongoConnectionString() {
        return mongoConnectionString;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public boolean isEnableMetrics() {
        return enableMetrics;
    }
    
    public boolean isVerboseLogging() {
        return verboseLogging;
    }
    
    @Override
    public String toString() {
        return "ServerConfig{" +
                "serverPort=" + serverPort +
                ", maxConnections=" + maxConnections +
                ", socketTimeout=" + socketTimeout +
                ", mongoConnectionString='" + mongoConnectionString + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", environment='" + environment + '\'' +
                ", enableMetrics=" + enableMetrics +
                ", verboseLogging=" + verboseLogging +
                '}';
    }
}
