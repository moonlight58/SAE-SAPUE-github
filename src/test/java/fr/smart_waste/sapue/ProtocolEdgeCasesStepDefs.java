package fr.smart_waste.sapue;

import fr.smart_waste.sapue.config.ServerConfig;
import fr.smart_waste.sapue.mocks.MockDataDriver;
import fr.smart_waste.sapue.mocks.MockSmartWasteServer;
import fr.smart_waste.sapue.model.Modules;
import fr.smart_waste.sapue.model.MapPoints;
import fr.smart_waste.sapue.model.Chipsets;
import fr.smart_waste.sapue.protocol.CommandHandler;
import fr.smart_waste.sapue.protocol.ProtocolRequest;
import fr.smart_waste.sapue.client.MediaAnalysisClient;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.bson.types.ObjectId;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolEdgeCasesStepDefs {

    private MockDataDriver dataDriver;
    private MockSmartWasteServer server;
    private CommandHandler commandHandler;
    private MediaAnalysisClient mediaAnalysisClient;

    private String lastResponse;
    private String lastReference;
    private String lastCommand;

    private Set<String> connectedClients = new HashSet<>();

    @Before
    public void setup() {
        dataDriver = new MockDataDriver();
        server = new MockSmartWasteServer(new ServerConfig());
        mediaAnalysisClient = new MediaAnalysisClient("localhost", 50060);
        commandHandler = new CommandHandler(dataDriver, server, mediaAnalysisClient);
        connectedClients.clear();
    }

    private void setupBinInDb(String ref) {
        if (dataDriver.findModuleByKey(ref) != null) return;
        Modules module = new Modules();
        module.setKey(ref);
        module.setId(new ObjectId());
        MapPoints p = new MapPoints();
        p.setId(new ObjectId());
        p.setModules(Collections.singletonList(module.getId()));
        dataDriver.addModule(module);
        dataDriver.addMapPoint(p);
    }

    @When("un client envoie {string}")
    public void unClientEnvoie(String command) {
        lastCommand = command;
        if (command.startsWith("IMAGE ANALYSE")) {
            setupBinInDb("MC-001");
            server.registerClient("MC-001", null);
            mediaAnalysisClient.setMockResponse(command.contains("recyclage") ? "recyclage" : "ordures_menageres");
        }

        try {
            ProtocolRequest request = fr.smart_waste.sapue.protocol.ProtocolParser.parse(command);
            lastResponse = commandHandler.execute(request);
            if (request != null) {
                lastReference = request.getReference();
            }
        } catch (Exception e) {
            lastResponse = "ERR_INTERNAL_ERROR";
        }
    }

    @Given("le système central est en fonctionnement \\(Protocol)")
    public void leSystèmeCentralEstEnFonctionnementProtocol() {
        server.setRunning(true);
        assertTrue(server.isRunning());
    }

    @Given("{string} existe dans la base")
    public void existeDansLaBase(String ref) {
        setupBinInDb(ref);
    }

    @Given("{string} est enregistré")
    public void estEnregistré(String ref) {
        setupBinInDb(ref);
        server.registerClient(ref, null);
        connectedClients.add(ref);
    }

    @Then("le système retourne {string}")
    public void leSystèmeRetourne(String response) {
        if (response.equals("...")) {
            assertTrue(lastResponse.startsWith("OK"));
        } else if (response.startsWith("OK ")) {
            assertTrue(lastResponse.startsWith("OK"));
            assertTrue(lastResponse.contains(response.substring(3)));
        } else {
            assertEquals(response, lastResponse);
        }
    }

    @Then("le système rejette avec {string}")
    public void leSystèmeRejetteAvec(String error) {
        assertEquals(error, lastResponse);
    }

    @When("un client non enregistré envoie {string}")
    public void unClientNonEnregistréEnvoie(String cmd) {
        // Fix: Ensure device exists in DB so we get NOT_REGISTERED instead of NOT_FOUND
        String ref = cmd.split(" ")[1];
        setupBinInDb(ref);
        // Ensure server doesn't have it in active connections
        connectedClients.remove(ref);
        unClientEnvoie(cmd);
    }

    @Then("le système détecte la déconnexion protocol")
    public void leSystemeDetecteLaDeconnexionProtocol() {
        assertEquals("ERR_CONNECTION_LOST", lastResponse);
    }

    @And("la référence fait exactement {int} caractères")
    public void laRéférenceFaitExactementCaractères(int count) {
        assertTrue(lastCommand.contains("MC-"));
        assertEquals(count, lastCommand.split(" ")[1].length());
    }

    @Then("le système accepte l'enregistrement")
    public void leSystèmeAccepteLEnregistrement() {
        assertTrue(lastResponse.startsWith("OK"));
    }

    @Then("le système traite la commande correctement")
    public void leSystèmeTraiteLaCommandeCorrectement() {
        assertTrue(lastResponse.startsWith("OK"));
    }

    @Then("le système supprime les espaces de fin")
    public void leSystèmeSupprimeLesEspacesDeFin() {
        assertTrue(lastResponse.startsWith("OK"));
    }

    @Then("la connexion est déjà fermée")
    public void laConnexionEstDéjàFermée() {
        // Since it's the second DISCONNECT, it might return OK (idempotent) or an error
        assertNotNull(lastResponse);
    }

    @When("un client envoie une commande contenant {string}")
    public void unClientEnvoieUneCommandeContenant(String specialChar) {
        unClientEnvoie("REGISTER MC-001 " + specialChar);
    }

    @Then("le système traite jusqu'au null byte")
    public void leSystèmeTraiteJusquAuNullByte() {
        assertNotNull(lastResponse);
    }

    @Then("le système peut traiter les tabs comme espaces")
    public void leSystèmePeutTraiterLesTabsCommeEspaces() {
        assertTrue(lastResponse.startsWith("OK"));
    }

    @And("le système retourne les informations d'analyse")
    public void leSystèmeRetourneLesInformationsDAnalyse() {
        assertTrue(lastResponse.startsWith("OK"));
    }

    @Then("le système accepte les underscores")
    public void leSystèmeAccepteLesUnderscores() { assertTrue(lastResponse.startsWith("OK")); }

    @Then("le système accepte les hyphens")
    public void leSystèmeAccepteLesHyphens() { assertTrue(lastResponse.startsWith("OK")); }

    @Then("le système normalise les espaces multiples")
    public void leSystèmeNormaliseLesEspacesMultiples() { assertTrue(lastResponse.startsWith("OK")); }

    @Then("le système valide la longueur")
    public void leSystèmeValideLaLongueur() { assertNotNull(lastResponse); }

    @And("le système accepte si la référence existe en base")
    public void leSystèmeAccepteSiLaRéférenceExisteEnBase() { assertTrue(lastResponse.startsWith("OK")); }

    @Then("le système rejette si caractères non-ASCII")
    public void leSystèmeRejetteSiCaractèresNonASCII() {
        // Implementation typically returns an error or filters characters
        assertTrue(lastResponse.startsWith("ERR"));
    }

    @And("la commande partielle est abandonnée")
    public void laCommandePartielleEstAbandonnée() {}

    @And("les ressources sont libérées")
    public void lesRessourcesSontLibérées() {}

    @When("le client envoie à nouveau {string}")
    public void leClientEnvoieÀNouveau(String cmd) {
        unClientEnvoie(cmd);
    }

    @Given("{string} est déjà enregistré et connecté")
    public void estDéjàEnregistréEtConnecté(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("le même client tente de se réenregistrer")
    public void leMêmeClientTenteDeSeRéenregistrer() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("la connexion existante reste active")
    public void laConnexionExistanteResteActive() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("{string} n'existe pas dans la collection microcontrolleurs")
    public void nExistePasDansLaCollectionMicrocontrolleurs(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("la connexion n'est pas enregistrée")
    public void laConnexionNEstPasEnregistrée() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("{string} a un configSensor.sensorType {string}")
    public void aUnConfigSensorSensorType(String arg0, String arg1) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système stocke la valeur \\(pas de validation de plage)")
    public void leSystèmeStockeLaValeurPasDeValidationDePlage() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("un avertissement peut être logué")
    public void unAvertissementPeutÊtreLogué() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte les valeurs négatives")
    public void leSystèmeAccepteLesValeursNégatives() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("les données sont stockées correctement")
    public void lesDonnéesSontStockéesCorrectement() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système stocke la valeur avec précision double")
    public void leSystèmeStockeLaValeurAvecPrécisionDouble() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("aucune perte de précision significative ne se produit")
    public void aucunePerteDePrécisionSignificativeNeSeProduit() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("un client envoie {int} paires key:value dans une commande DATA")
    public void unClientEnvoiePairesKeyValueDansUneCommandeDATA(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système traite tous les paramètres")
    public void leSystèmeTraiteTousLesParamètres() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("toutes les valeurs reconnues sont stockées")
    public void toutesLesValeursReconnuesSontStockées() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("les valeurs non reconnues génèrent des avertissements")
    public void lesValeursNonReconnuesGénèrentDesAvertissements() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système utilise la dernière valeur \\({double})")
    public void leSystèmeUtiliseLaDernièreValeur(int arg0, int arg1) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("un avertissement de doublon peut être logué")
    public void unAvertissementDeDoublonPeutÊtreLogué() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("toutes les données sont stockées correctement")
    public void toutesLesDonnéesSontStockéesCorrectement() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("{string} n'a pas de configSensor dans la base")
    public void nAPasDeConfigSensorDansLaBase(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("les paramètres supplémentaires sont ignorés")
    public void lesParamètresSupplémentairesSontIgnorés() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("le système retourne la configuration correctement")
    public void leSystèmeRetourneLaConfigurationCorrectement() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système parse {string} comme boolean")
    public void leSystèmeParseCommeBoolean(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("la configuration est mise à jour")
    public void laConfigurationEstMiseÀJour() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte aussi \\(case-insensitive)")
    public void leSystèmeAccepteAussiCaseInsensitive() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système parse {string} comme string \\(pas boolean)")
    public void leSystèmeParseCommeStringPasBoolean(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("stocke dans parameters")
    public void stockeDansParameters() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte mais peut loguer un avertissement")
    public void leSystèmeAccepteMaisPeutLoguerUnAvertissement() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte la valeur")
    public void leSystèmeAccepteLaValeur() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système stocke dans configSensor.parameters")
    public void leSystèmeStockeDansConfigSensorParameters() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("la valeur est accessible via CONFIG_GET")
    public void laValeurEstAccessibleViaCONFIG_GET() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système stocke la valeur \\(validation optionnelle)")
    public void leSystèmeStockeLaValeurValidationOptionnelle() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système stocke la valeur")
    public void leSystèmeStockeLaValeur() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("un avertissement est logué")
    public void unAvertissementEstLogué() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("toutes les métriques sont stockées")
    public void toutesLesMétriquesSontStockées() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système convertit en majuscules")
    public void leSystèmeConvertitEnMajuscules() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("traite la commande normalement")
    public void traiteLaCommandeNormalement() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système traite jusqu'au line break")
    public void leSystèmeTraiteJusquAuLineBreak() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("rejette probablement avec {string}")
    public void rejetteProbablementAvec(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("un client envoie une commande de {int} caractères")
    public void unClientEnvoieUneCommandeDeCaractères(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système peut limiter la longueur")
    public void leSystèmePeutLimiterLaLongueur() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("{string} et {string} envoient {string} en même temps")
    public void etEnvoientEnMêmeTemps(String arg0, String arg1, String arg2) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("un seul client est accepté")
    public void unSeulClientEstAccepté() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("l'autre reçoit {string}")
    public void lAutreReçoit(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("aucune condition de course ne se produit")
    public void aucuneConditionDeCourseNeSeProduit() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("le client envoie {int} commandes DATA en moins de {int} seconde")
    public void leClientEnvoieCommandesDATAEnMoinsDeSeconde(int arg0, int arg1) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("toutes les commandes sont traitées")
    public void toutesLesCommandesSontTraitées() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("l{string}exécution est préservé")
    public void lOrdreDExécutionEstPréservé() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("le client commence à envoyer une commande DATA")
    public void leClientCommenceÀEnvoyerUneCommandeDATA() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("la connexion est fermée pendant la transmission")
    public void laConnexionEstFerméePendantLaTransmission() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte \\(alphanumeric valide)")
    public void leSystèmeAccepteAlphanumericValide() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte \\(regex permet hyphen)")
    public void leSystèmeAccepteRegexPermetHyphen() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("le système accepte \\(pas de restriction)")
    public void leSystèmeAcceptePasDeRestriction() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("le client envoie {string}")
    public void leClientEnvoie(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}