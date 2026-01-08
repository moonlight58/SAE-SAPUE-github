package fr.smart_waste.sapue.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Configuration holder for Smart Waste TCP Server
 * Loads settings from YAML configuration file with environment variable overrides
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

    // Media Analysis Server settings
    private String mediaServerHost;
    private int mediaServerPort;

    // Logging settings
    private boolean enableMetrics;
    private boolean verboseLogging;

    /**
     * Load configuration from YAML file with environment variable overrides
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
            config.serverPort = getEnvAsInt("SERVER_PORT",
                    (Integer) serverData.get("port"));
            config.maxConnections = getEnvAsInt("MAX_CONNECTIONS",
                    (Integer) serverData.getOrDefault("maxConnections", 100));
            config.socketTimeout = getEnvAsInt("SOCKET_TIMEOUT",
                    (Integer) serverData.getOrDefault("socketTimeout", 30000));

            // Load MongoDB settings
            Map<String, Object> mongoData = (Map<String, Object>) data.get("mongodb");
            config.mongoConnectionString = getEnv("MONGO_CONNECTION_STRING",
                    (String) mongoData.get("connectionString"));
            config.databaseName = getEnv("MONGO_DATABASE_NAME",
                    (String) mongoData.get("databaseName"));
            config.environment = getEnv("ENVIRONMENT",
                    (String) mongoData.getOrDefault("environment", "dev"));

            // Load Media Analysis settings
            Map<String, Object> mediaData = (Map<String, Object>) data.get("mediaAnalysis");
            if (mediaData != null) {
                config.mediaServerHost = getEnv("MEDIA_SERVER_HOST",
                        (String) mediaData.getOrDefault("host", "localhost"));
                config.mediaServerPort = getEnvAsInt("MEDIA_SERVER_PORT",
                        (Integer) mediaData.getOrDefault("port", 50060));
            } else {
                config.mediaServerHost = getEnv("MEDIA_SERVER_HOST", "localhost");
                config.mediaServerPort = getEnvAsInt("MEDIA_SERVER_PORT", 50060);
            }

            // Load logging settings
            Map<String, Object> loggingData = (Map<String, Object>) data.get("logging");
            config.enableMetrics = getEnvAsBoolean("ENABLE_METRICS",
                    (Boolean) loggingData.getOrDefault("enableMetrics", true));
            config.verboseLogging = getEnvAsBoolean("VERBOSE_LOGGING",
                    (Boolean) loggingData.getOrDefault("verbose", false));

        }

        return config;
    }

    /**
     * Get environment variable or use default value
     */
    private static String getEnv(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Get environment variable as integer or use default value
     */
    private static int getEnvAsInt(String envVar, int defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid integer for " + envVar +
                        ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Get environment variable as boolean or use default value
     */
    private static boolean getEnvAsBoolean(String envVar, boolean defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
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
        if (mediaServerHost == null || mediaServerHost.isEmpty()) {
            throw new IllegalArgumentException("Media server host cannot be empty");
        }
        if (mediaServerPort < 1024 || mediaServerPort > 65535) {
            throw new IllegalArgumentException("Media server port must be between 1024 and 65535");
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

    public String getMediaServerHost() {
        return mediaServerHost;
    }

    public int getMediaServerPort() {
        return mediaServerPort;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "serverPort=" + serverPort +
                ", maxConnections=" + maxConnections +
                ", socketTimeout=" + socketTimeout +
                ", mongoConnectionString='" + maskSensitive(mongoConnectionString) + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", environment='" + environment + '\'' +
                ", mediaServerHost='" + mediaServerHost + '\'' +
                ", mediaServerPort=" + mediaServerPort +
                ", enableMetrics=" + enableMetrics +
                ", verboseLogging=" + verboseLogging +
                '}';
    }

    /**
     * Mask sensitive information for logging
     */
    private String maskSensitive(String value) {
        if (value == null || value.length() < 10) return "***";
        return value.substring(0, 10) + "***";
    }
}