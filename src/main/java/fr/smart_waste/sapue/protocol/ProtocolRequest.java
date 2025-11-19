package fr.smart_waste.sapue.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a parsed protocol request
 */
public class ProtocolRequest {

    private final String command;
    private final String reference;
    private final Map<String, String> parameters;
    private final String rawRequest;

    public ProtocolRequest(String command, String reference, Map<String, String> parameters, String rawRequest) {
        this.command = command;
        this.reference = reference;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.rawRequest = rawRequest;
    }

    public String getCommand() {
        return command;
    }

    public String getReference() {
        return reference;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    public String getRawRequest() {
        return rawRequest;
    }

    @Override
    public String toString() {
        return "ProtocolRequest{" +
                "command='" + command + '\'' +
                ", reference='" + reference + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}