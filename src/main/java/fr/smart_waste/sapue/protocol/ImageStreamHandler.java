package fr.smart_waste.sapue.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import org.bson.Document;
import fr.smart_waste.sapue.client.MediaAnalysisClient;
import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.Modules;
import fr.smart_waste.sapue.model.Chipsets;

/**
 * Gère la réception d'images en streaming depuis les µC legacy
 * Format attendu:
 * 1. IMG_B64
 * 2. [lignes de base64]
 * 3. [ligne vide = fin]
 */
public class ImageStreamHandler {

    private final DataDriver dataDriver;
    private final MediaAnalysisClient mediaAnalysisClient;
    private StringBuilder imageBuffer;
    private boolean streamingActive;

    public ImageStreamHandler(DataDriver dataDriver, MediaAnalysisClient mediaAnalysisClient) {
        this.dataDriver = dataDriver;
        this.mediaAnalysisClient = mediaAnalysisClient;
        this.imageBuffer = new StringBuilder();
        this.streamingActive = false;
    }

    /**
     * Démarre un nouveau stream d'image
     */
    public void startStream() {
        imageBuffer = new StringBuilder();
        streamingActive = true;
        log("Image streaming started");
    }

    /**
     * Ajoute une ligne de base64 au buffer
     * 
     * @return true si stream terminé (ligne vide), false sinon
     */
    public boolean appendLine(String line) {
        if (line == null) {
            return true; // Connection closed
        }

        String trimmed = line.trim();

        // Ligne vide = fin du stream
        if (trimmed.isEmpty()) {
            streamingActive = false;
            log("Image streaming completed (" + imageBuffer.length() + " bytes)");
            return true;
        }

        // Ajouter la ligne au buffer
        imageBuffer.append(trimmed);

        // FIX: Arduino sends all data in one println(), so we finish immediately on
        // receiving data
        // to prevent deadlock waiting for an empty line that never comes.
        streamingActive = false;
        log("Image streaming completed (" + imageBuffer.length() + " bytes)");
        return true;
    }

    /**
     * Analyse l'image accumulée et retourne la réponse pour le µC
     * Format de réponse attendu par le µC:
     * Ligne 1: Type de déchet (JAUNE, VERT, GRIS, MARRON)
     * Ligne 2: Distance de détection (45)
     * Ligne 3: Icône hex (00)
     * 
     */
    public String analyzeAndGetResponse(String deviceReference) {
        if (imageBuffer.length() == 0) {
            log("ERROR: No image data received");
            return "ERREUR\n00\n00\n";
        }

        // 1. Get Module Configuration (Distance)
        String distance = "45"; // Default
        try {
            if (deviceReference != null && !deviceReference.equals("legacy-device")) {
                Modules module = dataDriver.findModuleByKey(deviceReference);
                if (module != null) {
                    List<Chipsets> chipsets = dataDriver.findChipsetsByModuleId(module.getId());
                    if (chipsets != null) {
                        for (Chipsets chipset : chipsets) {
                            // Check for HC-SR04 or DistanceSensor capability
                            if (chipset.getName() != null && (chipset.getName().contains("HC-SR04")
                                    || chipset.getName().contains("HCSR04"))) {
                                if (chipset.getConfig() != null
                                        && chipset.getConfig().containsKey("detection_distance")) {
                                    Object distVal = chipset.getConfig().get("detection_distance");
                                    if (distVal != null) {
                                        distance = String.valueOf(distVal);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log("WARNING: Failed to fetch distance config: " + e.getMessage());
        }

        String imageBase64 = imageBuffer.toString();

        // 2. Analyser via le service d'analyse
        String wasteBinType = mediaAnalysisClient.analyzeImage(imageBase64);

        if (wasteBinType == null) {
            log("ERROR: Image analysis failed");
            return "ERREUR\n00\n00\n";
        }

        // 3. Mapper le type de déchet vers le format µC
        String response = mapWasteBinTypeToResponse(wasteBinType, distance);
        log("Analysis result: " + response.trim());

        return response;
    }

    /**
     * Mappe les types de déchets du service d'analyse vers le format legacy
     * 
     * @param wasteBinType Type retourné par le service
     * @param distance     Distance configuration
     * @return Réponse au format multi-line: COLOR\nDISTANCE\nICON
     */
    private String mapWasteBinTypeToResponse(String wasteBinType, String distance) {
        String color;
        String icon;

        // Normaliser le type de déchet
        String normalizedType = wasteBinType.toLowerCase().trim();

        // Handle "OK JAUNE" format if present
        if (normalizedType.startsWith("ok ")) {
            normalizedType = normalizedType.substring(3).trim();
        }

        switch (normalizedType) {
            case "jaune":
                color = "JAUNE";
                break;
            case "verte":
            case "vert":
                color = "VERTE";
                break;
            case "marron":
            case "brun":
                color = "MARRON";
                break;
            case "grise":
            case "gris":
            default:
                color = "GRISE";
                break;
        }

        // Récupérer l'icône depuis la BDD
        icon = dataDriver.getHexaIconByWasteBinType(color.toLowerCase());

        // Format: COLOR\nDISTANCE\nICON
        return color + "\n" + distance + "\n" + icon + "\n";
    }

    /**
     * Réinitialise le handler pour une nouvelle image
     */
    public void reset() {
        imageBuffer = new StringBuilder();
        streamingActive = false;
    }

    /**
     * Vérifie si un streaming est en cours
     */
    public boolean isStreaming() {
        return streamingActive;
    }

    private void log(String message) {
        System.out.println("[ImageStreamHandler] " + message);
    }
}