package fr.smart_waste.sapue.protocol;

import fr.smart_waste.sapue.client.MediaAnalysisClient;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Gère la réception d'images en streaming depuis les µC legacy
 * Format attendu:
 * 1. IMG_B64
 * 2. [lignes de base64]
 * 3. [ligne vide = fin]
 */
public class ImageStreamHandler {

    private final MediaAnalysisClient mediaAnalysisClient;
    private StringBuilder imageBuffer;
    private boolean streamingActive;

    public ImageStreamHandler(MediaAnalysisClient mediaAnalysisClient) {
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
        String wasteType = mediaAnalysisClient.analyzeImage(imageBase64);
        
        if (wasteType == null) {
            log("ERROR: Image analysis failed");
            return "ERREUR\n00\n00\n";
        }

        // Mapper le type de déchet vers le format µC
        String response = mapWasteTypeToLegacyResponse(wasteType);
        log("Analysis result: " + wasteType + " -> " + response);
        
        return response;
    }

    /**
     * Mappe les types de déchets du service d'analyse vers le format legacy
     * @param wasteType Type retourné par le service (ex: "recyclage", "ordures_menageres")
     * @return Réponse au format legacy (3 lignes)
     */
    private String mapWasteTypeToLegacyResponse(String wasteType) {
        // Distance par défaut (peut être configuré différemment selon le type)
        String distance = "45";
        
        // Icônes hex (de CvnEITM.py)
        final String ICON_RECYCLE = "00"; // Jaune - Recyclage
        final String ICON_TRASH = "00";   // Gris - Ordures ménagères
        final String ICON_BOTTLE = "00";  // Vert - Bouteille
        final String ICON_PLANT = "00";   // Marron - Compost
        
        String color;
        String icon;
        
        switch (wasteType.toLowerCase()) {
            case "recyclage":
            case "papier":
            case "carton":
                color = "JAUNE";
                icon = ICON_RECYCLE;
                break;
                
            case "verre":
            case "bouteille":
                color = "VERTE";
                icon = ICON_BOTTLE;
                break;
                
            case "compost":
            case "organique":
            case "biodechet":
                color = "MARRON";
                icon = ICON_PLANT;
                break;
                
            case "ordures_menageres":
            case "ordures":
            case "general":
            default:
                color = "GRISE";
                icon = ICON_TRASH;
                break;
        }
        
        // Format: COULEUR\nDISTANCE\nICONE\n
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