package fr.smart_waste.sapue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe metrics tracker for server monitoring
 */
public class ServerMetrics {
    
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalDataReceived = new AtomicLong(0); // in bytes
    private final AtomicLong totalDataSent = new AtomicLong(0); // in bytes
    
    private final long startTime;
    
    public ServerMetrics() {
        this.startTime = System.currentTimeMillis();
    }
    
    // Connection metrics
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
        totalConnections.incrementAndGet();
    }
    
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public int getTotalConnections() {
        return totalConnections.get();
    }
    
    // Request metrics
    public void incrementRequests() {
        totalRequests.incrementAndGet();
    }
    
    public long getTotalRequests() {
        return totalRequests.get();
    }
    
    // Error metrics
    public void incrementErrors() {
        totalErrors.incrementAndGet();
    }
    
    public long getTotalErrors() {
        return totalErrors.get();
    }
    
    // Data transfer metrics
    public void addDataReceived(long bytes) {
        totalDataReceived.addAndGet(bytes);
    }
    
    public void addDataSent(long bytes) {
        totalDataSent.addAndGet(bytes);
    }
    
    public long getTotalDataReceived() {
        return totalDataReceived.get();
    }
    
    public long getTotalDataSent() {
        return totalDataSent.get();
    }
    
    // Uptime
    public long getUptimeMillis() {
        return System.currentTimeMillis() - startTime;
    }
    
    public String getUptimeFormatted() {
        long uptime = getUptimeMillis();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%dd %dh %dm %ds", 
            days, hours % 24, minutes % 60, seconds % 60);
    }
    
    /**
     * Print metrics summary
     */
    public void printSummary() {
        System.out.println("\n========== Server Metrics ==========");
        System.out.println("Uptime: " + getUptimeFormatted());
        System.out.println("Active Connections: " + getActiveConnections());
        System.out.println("Total Connections: " + getTotalConnections());
        System.out.println("Total Requests: " + getTotalRequests());
        System.out.println("Total Errors: " + getTotalErrors());
        System.out.println("Data Received: " + formatBytes(getTotalDataReceived()));
        System.out.println("Data Sent: " + formatBytes(getTotalDataSent()));
        System.out.println("====================================\n");
    }
    
    /**
     * Reset all metrics (useful for testing)
     */
    public void reset() {
        activeConnections.set(0);
        totalConnections.set(0);
        totalRequests.set(0);
        totalErrors.set(0);
        totalDataReceived.set(0);
        totalDataSent.set(0);
    }
    
    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), pre);
    }
}
