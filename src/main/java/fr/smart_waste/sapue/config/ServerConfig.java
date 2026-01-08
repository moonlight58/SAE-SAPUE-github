package fr.smart_waste.sapue.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Configuration holder for Smart Waste TCP Server
 * Loads settings from environment variables first, then YAML file as fallback
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
     * Load configuration from environment variables or YAML file
     * Priority: ENV > YAML > defaults
     * @param configFilePath Path to config.yml file (optional, can be null)
     * @return ServerConfig instance
     * @throws Exception if configuration is invalid
     */
    public static ServerConfig loadFromFile(String configFilePath) throws Exception {
        ServerConfig config = new ServerConfig();

        // Check if YAML file exists
        boolean yamlExists = configFilePath != null && new File(configFilePath).exists();
        Map<String, Object> yamlData = null;

        if (yamlExists) {
            try (InputStream input = new FileInputStream(configFilePath)) {
                Yaml yaml = new Yaml();
                yamlData = yaml.load(input);
                System.out.println("✓ Loaded YAML configuration from: " + configFilePath);
            } catch (Exception e) {
                System.err.println("⚠ Warning: Could not load YAML file, using environment variables only");
            }
        } else {
            System.out.println("ℹ No YAML file found, using environment variables");
        }

        // Load server settings
        config.serverPort = getEnvAsInt("SERVER_PORT",
                getFromYaml(yamlData, "server.port", 50010));
        config.maxConnections = getEnvAsInt("MAX_CONNECTIONS",
                getFromYaml(yamlData, "server.maxConnections", 100));
        config.socketTimeout = getEnvAsInt("SOCKET_TIMEOUT",
                getFromYaml(yamlData, "server.socketTimeout", 30000));

        // Load MongoDB settings
        config.mongoConnectionString = getEnv("MONGO_CONNECTION_STRING",
                getFromYaml(yamlData, "mongodb.connectionString", "mongodb://localhost:27017/"));
        config.databaseName = getEnv("MONGO_DATABASE_NAME",
                getFromYaml(yamlData, "mongodb.databaseName", "sae_db"));
        config.environment = getEnv("ENVIRONMENT",
                getFromYaml(yamlData, "mongodb.environment", "dev"));

        // Load Media Analysis settings
        config.mediaServerHost = getEnv("MEDIA_SERVER_HOST",
                getFromYaml(yamlData, "mediaAnalysis.host", "localhost"));
        config.mediaServerPort = getEnvAsInt("MEDIA_SERVER_PORT",
                getFromYaml(yamlData, "mediaAnalysis.port", 50060));

        // Load logging settings
        config.enableMetrics = getEnvAsBoolean("ENABLE_METRICS",
                getFromYaml(yamlData, "logging.enableMetrics", true));
        config.verboseLogging = getEnvAsBoolean("VERBOSE_LOGGING",
                getFromYaml(yamlData, "logging.verbose", false));

        // Log configuration source
        System.out.println("Configuration loaded - Environment: " + config.environment);

        return config;
    }

    /**
     * Get value from nested YAML structure
     */
    @SuppressWarnings("unchecked")
    private static <T> T getFromYaml(Map<String, Object> yamlData, String path, T defaultValue) {
        if (yamlData == null) {
            return defaultValue;
        }

        try {
            String[] keys = path.split("\\.");
            Object current = yamlData;

            for (String key : keys) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(key);
                    if (current == null) {
                        return defaultValue;
                    }
                } else {
                    return defaultValue;
                }
            }

            return (T) current;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get environment variable or use default value
     */
    private static String getEnv(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            System.out.println("  ✓ Using ENV: " + envVar);
            return value;
        }
        return defaultValue;
    }

    /**
     * Get environment variable as integer or use default value
     */
    private static int getEnvAsInt(String envVar, int defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            try {
                System.out.println("  ✓ Using ENV: " + envVar);
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("⚠ Warning: Invalid integer for " + envVar +
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
            System.out.println("  ✓ Using ENV: " + envVar);
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
        if (value == null || value.length() < 15) return "***";
        return value.substring(0, 15) + "***";
    }
}