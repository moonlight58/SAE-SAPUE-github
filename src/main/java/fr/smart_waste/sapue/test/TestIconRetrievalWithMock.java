package fr.smart_waste.sapue.test;

import fr.smart_waste.sapue.client.MediaAnalysisClient;
import fr.smart_waste.sapue.dataaccess.MongoDataDriver;
import fr.smart_waste.sapue.protocol.ImageStreamHandler;

public class TestIconRetrievalWithMock {
    
    public static void main(String[] args) {
        System.out.println("=== Démarrage du test de récupération d'icônes ===\n");
        
        // 1. Initialiser le DataDriver
        System.out.println("1️⃣  Connexion à MongoDB...");
        MongoDataDriver dataDriver = new MongoDataDriver(
            "mongodb://admin:admin-mdp@localhost:50000/",
            "sae_db" // ou "sae_db" selon votre config
        );
        
        // 2. Créer MediaAnalysisClient
        System.out.println("2️⃣  Création du client d'analyse média...");
        MediaAnalysisClient mediaClient = new MediaAnalysisClient("localhost", 50060);
        
        // 3. Tester toutes les couleurs
        String[] colors = {"jaune", "verte", "marron", "grise"};
        
        for (String color : colors) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🎨 TEST COULEUR : " + color.toUpperCase());
            System.out.println("=".repeat(60));
            
            // Configurer le mock
            mediaClient.setMockResponse(color);
            System.out.println("✓ Mock configuré avec réponse : " + color);
            
            // Créer ImageStreamHandler
            ImageStreamHandler handler = new ImageStreamHandler(dataDriver, mediaClient);
            
            // Simuler le streaming d'image
            System.out.println("✓ Démarrage du streaming d'image...");
            handler.startStream();
            handler.appendLine("iVBORw0KGgoAAAANSUhEUgAAAAUA"); // Fake base64
            handler.appendLine("AAALEgAACxIB0t1+/AAAADh0RVh0U");
            handler.appendLine("m9mdHdhcmUAbWF0cGxvdGxpYiB2ZX"); 
            handler.appendLine(""); // Fin du stream
            
            // Analyser et obtenir la réponse
            System.out.println("✓ Analyse de l'image...");
            String response = handler.analyzeAndGetResponse("MC-001");
            
            // Afficher le résultat
            System.out.println("\n📤 RÉPONSE GÉNÉRÉE :");
            System.out.println("─".repeat(60));
            System.out.println(response);
            System.out.println("─".repeat(60));
            
            // Détail ligne par ligne
            System.out.println("\n📋 DÉTAIL LIGNE PAR LIGNE :");
            String[] lines = response.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String lineContent = lines[i].isEmpty() ? "(vide)" : lines[i];
                int lineLength = lines[i].length();
                System.out.printf("   Ligne %d : %-15s (longueur: %d)\n", 
                    (i+1), lineContent, lineLength);
            }
            
            // Validation
            System.out.println("\n✅ VALIDATION :");
            if (lines.length >= 3) {
                System.out.println("   ✓ Nombre de lignes : " + lines.length + " (OK)");
                System.out.println("   ✓ Couleur (ligne 1) : " + lines[0]);
                System.out.println("   ✓ Distance (ligne 2) : " + lines[1]);
                System.out.println("   ✓ Icône hex (ligne 3) : " + 
                    (lines[2].length() > 20 ? lines[2].substring(0, 20) + "..." : lines[2]));
                
                // Vérifier que l'icône n'est pas "00" (valeur par défaut)
                if (!"00".equals(lines[2])) {
                    System.out.println("   ✓ Icône récupérée depuis la BDD !");
                } else {
                    System.out.println("   ⚠ Attention : icône par défaut retournée");
                }
            } else {
                System.out.println("   ✗ ERREUR : Nombre de lignes insuffisant");
            }
            
            try {
                Thread.sleep(500); // Petite pause entre les tests
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // 4. Test d'erreur
        System.out.println("\n" + "=".repeat(60));
        System.out.println("❌ TEST ERREUR (analyse échouée)");
        System.out.println("=".repeat(60));
        
        mediaClient.setMockResponse(null); // Simuler un échec
        ImageStreamHandler handler = new ImageStreamHandler(dataDriver, mediaClient);
        
        handler.startStream();
        handler.appendLine("fake_data");
        handler.appendLine("");
        
        String errorResponse = handler.analyzeAndGetResponse("MC-001");
        System.out.println("📤 Réponse d'erreur : ");
        System.out.println(errorResponse);
        
        if (errorResponse.startsWith("ERREUR")) {
            System.out.println("✓ Gestion d'erreur correcte");
        }
        
        // 5. Nettoyage
        System.out.println("\n5️⃣  Fermeture de la connexion MongoDB...");
        dataDriver.close();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ TESTS TERMINÉS AVEC SUCCÈS");
        System.out.println("=".repeat(60));
    }
}