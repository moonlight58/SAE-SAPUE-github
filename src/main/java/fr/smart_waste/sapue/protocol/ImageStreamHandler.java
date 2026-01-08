package fr.smart_waste.sapue.protocol;

import fr.smart_waste.sapue.client.MediaAnalysisClient;
import java.io.BufferedReader;
import java.io.IOException;
import fr.smart_waste.sapue.dataaccess.DataDriver;

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
        return false;
    }

    /**
     * Analyse l'image accumulée et retourne la réponse pour le µC
     * Format de réponse attendu par le µC:
     * Ligne 1: Type de déchet (JAUNE, VERT, GRIS, MARRON)
     * Ligne 2: Distance de détection (45)
     * Ligne 3: Icône hex (00)

     */
    public String analyzeAndGetResponse(String deviceReference) {
        if (imageBuffer.length() == 0) {
            log("ERROR: No image data received");
            return "ERREUR\n00\n00\n";
        }

        String imageBase64 = imageBuffer.toString();
        
        // Analyser via le service d'analyse
        String wasteBinType = mediaAnalysisClient.analyzeImage(imageBase64);
        
        if (wasteBinType == null) {
            log("ERROR: Image analysis failed");
            return "ERREUR\n00\n00\n";
        }

        // Mapper le type de déchet vers le format µC
        String response = mapWasteBinTypeToResponse(wasteBinType);
        log("Analysis result: " + response);
        
        return response;
    }

    /**
     * Mappe les types de déchets du service d'analyse vers le format legacy
     * @param wasteType Type retourné par le service (ex: "recyclage", "ordures_menageres")
     * @return Réponse au format legacy (3 lignes)
     */
    private String mapWasteBinTypeToResponse(String wasteBinType) {
        String distance = "45"; // Distance par défaut

        String color;
        String icon;

        // Normaliser le type de déchet (au cas où le service d'analyse renvoie différents formats)
        String normalizedType = wasteBinType.toLowerCase().trim();
        
        switch (normalizedType) {
            case "jaune":
            case "recyclage":
            case "papier":
            case "carton":
                color = "JAUNE";
                break;
            case "verte":
            case "vert":
            case "verre":
            case "bouteille":
                color = "VERTE";
                break;
            case "marron":
            case "compost":
            case "organique":
            case "biodechet":
                color = "MARRON";
                break;
            case "grise":
            case "gris":
            case "ordures_menageres":
            case "ordures":
            case "general":
            default:
                color = "GRISE";
                break;
        }
        
        // Récupérer l'icône depuis la BDD en utilisant la couleur normalisée
        icon = dataDriver.getHexaIconByWasteBinType(color.toLowerCase());
        
        // Format: COLOR\nDISTANCE\nICONE\n
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