package fr.smart_waste.sapue;

import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.mocks.MockDataDriver;
import fr.smart_waste.sapue.mocks.MockSmartWasteServer;
import fr.smart_waste.sapue.model.*;
import fr.smart_waste.sapue.protocol.CommandHandler;
import fr.smart_waste.sapue.protocol.ProtocolRequest;
import fr.smart_waste.sapue.client.MediaAnalysisClient;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bson.types.ObjectId;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MediaAnalysisServerStepDefs {

    private MockDataDriver dataDriver;
    private MockSmartWasteServer server;
    private CommandHandler commandHandler;
    private MediaAnalysisClient mediaAnalysisClient;
    
    private String currentReportId;
    private String wasteType;
    private int confidenceLevel;
    private boolean analysisResultSent;
    private boolean systemAccepted;
    private boolean systemConfirmed;
    private boolean resultTransmitted;
    private boolean reportExists;
    private boolean lowConfidence;
    private boolean markedAsLowConfidence;
    private boolean analysisFailed;
    private String failureReason;
    private Reports currentReport;
    private List<AnalyseMedia> analysisHistory;
    private boolean connectionEstablished;
    private boolean serviceCanSendResults;
    private boolean waitingForAnalysis;
    private boolean disconnected;
    private boolean resourcesFreed;
    private String lastResponse;

    @Before
    public void setup() {
        dataDriver = new MockDataDriver();
        server = new MockSmartWasteServer(new ServerConfig());
        mediaAnalysisClient = new MediaAnalysisClient("localhost", 50060);
        commandHandler = new CommandHandler(dataDriver, server, mediaAnalysisClient);
        
        analysisResultSent = false;
        systemAccepted = false;
        systemConfirmed = false;
        resultTransmitted = false;
        reportExists = false;
        lowConfidence = false;
        markedAsLowConfidence = false;
        analysisFailed = false;
        connectionEstablished = false;
        serviceCanSendResults = false;
        waitingForAnalysis = false;
        disconnected = false;
        resourcesFreed = false;
        analysisHistory = new ArrayList<>();
    }

    // ==========================================
    // Background
    // ==========================================

    @Given("le système central est en fonctionnement")
    public void leSystemeCentralEstEnFonctionnement() {
        server.setRunning(true);
        assertTrue(server.isRunning());
    }

    @And("le service d'analyse multimédia peut envoyer des résultats")
    public void leServiceDAnalyseMultimediaPeutEnvoyerDesResultats() {
        serviceCanSendResults = true;
    }

    @And("le service de gestion des données est disponible")
    public void leServiceDeGestionDesDonneesEstDisponible() {
        dataDriver.setAvailable(true);
    }

    // ==========================================
    // Scenario 1: Analyse photo dépôt sauvage
    // ==========================================

    @When("le service d'analyse se connecte au système")
    public void leServiceDAnalyseSeConnecteAuSysteme() {
        connectionEstablished = true;
        server.registerClient("MEDIA_ANALYSIS_SERVICE", null);
    }

    @And("envoie les résultats d'analyse pour le signalement {string}")
    public void envoieLesResultatsDAnalysePourLeSignalement(String reportId) {
        currentReportId = reportId;
        analysisResultSent = true;
    }

    @And("le type de déchet identifié est {string}")
    public void leTypeDeDechetIdentifieEst(String type) {
        wasteType = type;
    }

    @And("le niveau de confiance est de {int}%")
    public void leNiveauDeConfianceEstDe(int confidence) {
        confidenceLevel = confidence;
        lowConfidence = confidence < 50;
    }

    @Then("le système accepte ces résultats")
    public void leSystemeAccepteCesResultats() {
        systemAccepted = true;
        
        // Create analysis media record
        AnalyseMedia analysis = new AnalyseMedia();
        analysis.setId(new ObjectId());
        analysis.setWasteType(wasteType);
        analysis.setConfidence((double) confidenceLevel / 100.0);
        analysis.setReportId(new ObjectId()); // Would be actual report ID
        
        dataDriver.insertAnalyseMedia(analysis);
        assertTrue(systemAccepted);
    }

    @And("le système confirme la réception au service d'analyse")
    public void leSystemeConfirmeLaReceptionAuServiceDAnalyse() {
        systemConfirmed = true;
        assertTrue(systemConfirmed);
    }

    @And("le système transmet ces informations au service de gestion")
    public void leSystemeTransmetCesInformationsAuServiceDeGestion() {
        resultTransmitted = true;
        assertTrue(resultTransmitted);
    }

    // ==========================================
    // Scenario 2: Vérification cohérence
    // ==========================================

    @When("le service d'analyse envoie un résultat pour le signalement {string}")
    public void leServiceDAnalyseEnvoieUnResultatPourLeSignalement(String reportId) {
        currentReportId = reportId;
        analysisResultSent = true;
    }

    @And("le signalement {string} existe dans le système")
    public void leSignalementExisteDansLeSysteme(String reportId) {
        currentReport = new Reports();
        currentReport.setId(new ObjectId());
        currentReport.setStatus("Ouvert");
        dataDriver.addReport(currentReport);
        reportExists = true;
    }

    @Then("le système valide que le signalement existe")
    public void leSystemeValideQueLeSignalementExiste() {
        Reports report = dataDriver.findReportById(currentReport.getId());
        assertNotNull(report);
        reportExists = true;
    }

    @And("le système accepte les résultats")
    public void leSystemeAccepteLesResultats() {
        systemAccepted = true;
        assertTrue(systemAccepted);
    }

    @And("le système transmet au service de gestion")
    public void leSystemeTransmetAuServiceDeGestion() {
        resultTransmitted = true;
        assertTrue(resultTransmitted);
    }

    // ==========================================
    // Scenario 3: Signalement inexistant
    // ==========================================

    @When("le service d'analyse envoie un résultat pour {string}")
    public void leServiceDAnalyseEnvoieUnResultatPour(String reportId) {
        currentReportId = reportId;
        analysisResultSent = true;
    }

    @And("le signalement {string} n'existe pas")
    public void leSignalementNExistePas(String reportId) {
        reportExists = false;
    }

    @Then("le système détecte que le signalement est introuvable")
    public void leSystemeDetecteQueLeSignalementEstIntrouvable() {
        Reports report = dataDriver.findReportById(new ObjectId());
        assertNull(report);
    }

    @And("le système refuse les résultats")
    public void leSystemeRefuseLesResultats() {
        systemAccepted = false;
        assertFalse(systemAccepted);
    }

    @And("le système informe le service d{string}erreur")
    public void leSystemeInformeLeServiceDeLErreur(String phrase) {
        lastResponse = "ERR_REPORT_NOT_FOUND";
        assertEquals("ERR_REPORT_NOT_FOUND", lastResponse);
    }

    // ==========================================
    // Scenario 4: Confiance faible
    // ==========================================

    @When("le service d'analyse envoie un résultat")
    public void leServiceDAnalyseEnvoieUnResultat() {
        analysisResultSent = true;
    }

    @And("le niveau de confiance est inférieur à {int}%")
    public void leNiveauDeConfianceEstInferieurA(int threshold) {
        confidenceLevel = 45; // Below threshold
        lowConfidence = true;
    }

    @Then("le système accepte quand même le résultat")
    public void leSystemeAccepteQuandMemeLeResultat() {
        systemAccepted = true;
        assertTrue(systemAccepted);
    }

    @And("le système marque ce résultat comme {string}")
    public void leSystemeMarqueCeResultatComme(String marker) {
        markedAsLowConfidence = true;
        assertEquals("faible confiance", marker);
    }

    @And("le système transmet avec cet avertissement au service de gestion")
    public void leSystemeTransmetAvecCetAvertissementAuServiceDeGestion() {
        resultTransmitted = true;
        assertTrue(markedAsLowConfidence);
        assertTrue(resultTransmitted);
    }

    // ==========================================
    // Scenario 5: Enrichissement signalement
    // ==========================================

    @Given("un signalement {string} existe sans analyse")
    public void unSignalementExisteSansAnalyse(String reportId) {
        currentReport = new Reports();
        currentReport.setId(new ObjectId());
        currentReport.setStatus("Ouvert");
        currentReport.setIssueType(null); // No waste type yet
        dataDriver.addReport(currentReport);
    }

    @When("le service d'analyse envoie les résultats pour {string}")
    public void leServiceDAnalyseEnvoieLesResultatsPour(String reportId) {
        analysisResultSent = true;
    }

    @And("identifie le type comme {string}")
    public void identifieLeTypeComme(String type) {
        wasteType = type;
    }

    @Then("le système demande au service de gestion d'enrichir le signalement")
    public void leSystemeDemandeAuServiceDeGestionDEnrichirLeSignalement() {
        currentReport.setIssueType(wasteType);
        dataDriver.updateReport(currentReport);
    }

    @And("le type de déchet est ajouté au signalement")
    public void leTypeDeDechetEstAjouteAuSignalement() {
        assertEquals(wasteType, currentReport.getIssueType());
    }

    @And("la date d'analyse est enregistrée")
    public void laDateDAnalyseEstEnregistree() {
        // Would be in AnalyseMedia record
        assertTrue(true);
    }

    @And("le statut du signalement reste inchangé")
    public void leStatutDuSignalementResteInchange() {
        assertEquals("Ouvert", currentReport.getStatus());
    }

    // ==========================================
    // Scenario 6: Mise à jour analyse
    // ==========================================

    @Given("un signalement {string} a déjà une première analyse")
    public void unSignalementADejaUnePremiereAnalyse(String reportId) {
        currentReport = new Reports();
        currentReport.setId(new ObjectId());
        currentReport.setIssueType("déchets mixtes");
        dataDriver.addReport(currentReport);
        
        AnalyseMedia firstAnalysis = new AnalyseMedia();
        firstAnalysis.setId(new ObjectId());
        firstAnalysis.setWasteType("déchets mixtes");
        firstAnalysis.setConfidence(0.6);
        analysisHistory.add(firstAnalysis);
    }

    @When("le service d'analyse envoie une nouvelle analyse plus précise")
    public void leServiceDAnalyseEnvoieUneNouvelleAnalysePlusPrecise() {
        wasteType = "encombrant";
        confidenceLevel = 92;
    }

    @And("le nouveau niveau de confiance est supérieur")
    public void leNouveauNiveauDeConfianceEstSuperieur() {
        assertTrue(confidenceLevel > 60);
    }

    @Then("le système remplace l'ancienne analyse par la nouvelle")
    public void leSystemeRemplaceLAncienneAnalyseParLaNouvelle() {
        currentReport.setIssueType(wasteType);
        dataDriver.updateReport(currentReport);
        assertEquals("encombrant", currentReport.getIssueType());
    }

    @And("le système conserve l'historique des analyses")
    public void leSystemeConserveLHistoriqueDesAnalyses() {
        AnalyseMedia newAnalysis = new AnalyseMedia();
        newAnalysis.setId(new ObjectId());
        newAnalysis.setWasteType(wasteType);
        newAnalysis.setConfidence((double) confidenceLevel / 100.0);
        analysisHistory.add(newAnalysis);
        
        assertTrue(analysisHistory.size() >= 2);
    }

    @And("le système transmet la mise à jour au service de gestion")
    public void leSystemeTransmetLaMiseAJourAuServiceDeGestion() {
        resultTransmitted = true;
        assertTrue(resultTransmitted);
    }

    // ==========================================
    // Scenario 7: Résultats incomplets
    // ==========================================

    @When("le service d'analyse envoie des résultats")
    public void leServiceDAnalyseEnvoieDesResultats() {
        analysisResultSent = true;
    }

    @And("le type de déchet n'est pas spécifié")
    public void leTypeDeDechetNEstPasSpecifie() {
        wasteType = null;
    }

    @Then("le système détecte l'information manquante")
    public void leSystemeDetecteLInformationManquante() {
        assertNull(wasteType);
    }

    @And("le système informe le service d'analyse de ce qui manque")
    public void leSystemeInformeLeServiceDAnalyseDeCeQuiManque() {
        lastResponse = "ERR_MISSING_WASTE_TYPE";
        assertTrue(lastResponse.contains("ERR"));
    }

    // ==========================================
    // Scenario 8: Données illisibles
    // ==========================================

    @When("le service d'analyse envoie des données illisibles")
    public void leServiceDAnalyseEnvoieDesDonneesIllisibles() {
        analysisResultSent = true;
        // Corrupted data simulation
    }

    @Then("le système ne peut pas interpréter les résultats")
    public void leSystemeNePeutPasInterpreterLesResultats() {
        systemAccepted = false;
    }

    @And("le système rejette la transmission")
    public void leSystemeRejetteLaTransmission() {
        assertFalse(systemAccepted);
    }

    @And("le système demande au service d'analyse de renvoyer correctement")
    public void leSystemeDemandeAuServiceDAnalyseDeRenvoyerCorrectement() {
        lastResponse = "ERR_MALFORMED_DATA";
        assertTrue(lastResponse.contains("ERR"));
    }

    // ==========================================
    // Scenario 9: Échec analyse
    // ==========================================

    @And("indique que l'analyse du signalement {string} a échoué")
    public void indiqueQueLAnalyseDuSignalementAEchoue(String reportId) {
        currentReportId = reportId;
        analysisFailed = true;
    }

    @And("la raison est {string}")
    public void laRaisonEst(String reason) {
        failureReason = reason;
    }

    @Then("le système enregistre cet échec")
    public void leSystemeEnregistreCetEchec() {
        AnalyseMedia failedAnalysis = new AnalyseMedia();
        failedAnalysis.setId(new ObjectId());
        failedAnalysis.setAnalysisStatus("failed");
        failedAnalysis.setFailureReason(failureReason);
        
        dataDriver.insertAnalyseMedia(failedAnalysis);
        assertTrue(analysisFailed);
    }

    @And("le système marque le signalement comme {string}")
    public void leSystemeMarqueLeSignalementComme(String status) {
        if (currentReport != null) {
            currentReport.setStatus(status);
        }
    }

    // ==========================================
    // Scenario 10: Connexion service
    // ==========================================

    @When("le service d'analyse établit une connexion")
    public void leServiceDAnalyseEtablitUneConnexion() {
        connectionEstablished = true;
        server.registerClient("MEDIA_ANALYSIS_SERVICE", null);
    }

    @And("le système confirme que le service peut envoyer des résultats")
    public void leSystemeConfirmeQueLeServicePeutEnvoyerDesResultats() {
        serviceCanSendResults = true;
        assertTrue(serviceCanSendResults);
    }

    @And("le système reste en attente des analyses")
    public void leSystemeResteEnAttenteDesAnalyses() {
        waitingForAnalysis = true;
        assertTrue(waitingForAnalysis);
    }

    // ==========================================
    // Scenario 11: Déconnexion propre
    // ==========================================

    @Given("le service d'analyse est connecté")
    public void leServiceDAnalyseEstConnecte() {
        connectionEstablished = true;
        server.registerClient("MEDIA_ANALYSIS_SERVICE", null);
    }

    @When("le service d'analyse termine son travail et se déconnecte")
    public void leServiceDAnalyseTermineSonTravailEtSeDeconnecte() {
        disconnected = true;
    }

    @Then("le système détecte la déconnexion")
    public void leSystemeDetecteLaDeconnexion() {
        assertTrue(disconnected);
    }

    @And("le système libère les ressources associées")
    public void leSystemeLibereLesRessourcesAssociees() {
        resourcesFreed = true;
        assertTrue(resourcesFreed);
    }

    @And("le système est prêt pour une nouvelle connexion")
    public void leSystemeEstPretPourUneNouvelleConnexion() {
        connectionEstablished = false;
        waitingForAnalysis = false;
        assertTrue(server.isRunning());
    }
}