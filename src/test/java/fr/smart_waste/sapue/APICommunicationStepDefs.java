package fr.smart_waste.sapue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;


public class APICommunicationStepDefs {
    @Given("le système central est en fonctionnement")
    public void leSystemeCentralEstEnFonctionnement() {
    }

    @And("le service de gestion des données est disponible")
    public void leServiceDeGestionDesDonneesEstDisponible() {
    }

    @Given("le système a reçu une mesure de remplissage de la poubelle {string}")
    public void leSystemeARecuUneMesureDeRemplissageDeLaPoubelle(String arg0) {
    }

    @When("le système transmet cette information au service de gestion")
    public void leSystemeTransmetCetteInformationAuServiceDeGestion() {
    }

    @Then("le service de gestion confirme la réception")
    public void leServiceDeGestionConfirmeLaReception() {
    }

    @And("le système notifie la poubelle que tout s'est bien passé")
    public void leSystemeNotifieLaPoubelleQueToutSEstBienPasse() {
    }

    @Given("le système a reçu une mesure de poids de {double} kg")
    public void leSystemeARecuUneMesureDePoidsDeKg(int arg0, int arg1) {
    }

    @And("cette mesure provient de la poubelle {string}")
    public void cetteMesureProvientDeLaPoubelle(String arg0) {
    }

    @When("le système transmet cette information")
    public void leSystemeTransmetCetteInformation() {
    }

    @Then("le service de gestion enregistre le poids")
    public void leServiceDeGestionEnregistreLePoids() {
    }

    @And("le système reçoit une confirmation")
    public void leSystemeRecoitUneConfirmation() {
    }

    @Given("le système a reçu une alerte de mauvaise qualité d'air")
    public void leSystemeARecuUneAlerteDeMauvaiseQualiteDAir() {
    }

    @And("l'alerte provient de la poubelle {string}")
    public void lAlerteProvientDeLaPoubelle(String arg0) {
    }

    @When("le système transmet cette alerte au service de gestion")
    public void leSystemeTransmetCetteAlerteAuServiceDeGestion() {
    }

    @Then("le service de gestion enregistre l'alerte")
    public void leServiceDeGestionEnregistreLAlerte() {
    }

    @Given("le service d'analyse a identifié un type de déchet")
    public void leServiceDAnalyseAIdentifieUnTypeDeDechet() {
    }

    @And("le type identifié est {string}")
    public void leTypeIdentifieEst(String arg0) {
    }

    @And("le niveau de confiance est de {int}%")
    public void leNiveauDeConfianceEstDe(int arg0) {
    }

    @When("le système transmet ces résultats au service de gestion")
    public void leSystemeTransmetCesResultatsAuServiceDeGestion() {
    }

    @Then("le service de gestion met à jour le signalement")
    public void leServiceDeGestionMetAJourLeSignalement() {
    }

    @Given("le service d'analyse a détecté un acte de vandalisme")
    public void leServiceDAnalyseADetecteUnActeDeVandalisme() {
    }

    @And("la gravité est {string}")
    public void laGraviteEst(String arg0) {
    }

    @And("la poubelle concernée est {string}")
    public void laPoubelleConcerneeEst(String arg0) {
    }

    @Then("le service de gestion crée un incident")
    public void leServiceDeGestionCreeUnIncident() {
    }

    @And("le service de gestion met à jour le statut de la poubelle")
    public void leServiceDeGestionMetAJourLeStatutDeLaPoubelle() {
    }

    @Given("une poubelle {string} demande sa configuration")
    public void unePoubelleDemandeSaConfiguration(String arg0) {
    }

    @When("le système interroge le service de gestion pour obtenir ces informations")
    public void leSystemeInterrogeLeServiceDeGestionPourObtenirCesInformations() {
    }

    @Then("le service de gestion fournit l'intervalle de mesure")
    public void leServiceDeGestionFournitLIntervalleDeMesure() {
    }

    @And("le service de gestion fournit le seuil d'alerte")
    public void leServiceDeGestionFournitLeSeuilDAlerte() {
    }

    @And("le système transmet ces paramètres à la poubelle")
    public void leSystemeTransmetCesParametresALaPoubelle() {
    }

    @Given("une poubelle {string} veut connaître son statut")
    public void unePoubelleVeutConnaitreSonStatut(String arg0) {
    }

    @When("le système interroge le service de gestion")
    public void leSystemeInterrogeLeServiceDeGestion() {
    }

    @Then("le service de gestion fournit le dernier niveau de remplissage connu")
    public void leServiceDeGestionFournitLeDernierNiveauDeRemplissageConnu() {
    }

    @And("le service de gestion indique si une collecte est prévue")
    public void leServiceDeGestionIndiqueSiUneCollecteEstPrevue() {
    }

    @And("le système transmet ces informations à la poubelle")
    public void leSystemeTransmetCesInformationsALaPoubelle() {
    }

    @Then("le service de gestion indique que cette poubelle n'existe pas")
    public void leServiceDeGestionIndiqueQueCettePoubelleNExistePas() {
    }

    @And("le système informe la poubelle qu{string}est pas enregistrée")
    public void leSystemeInformeLaPoubelleQuElleNEstPasEnregistree() {
    }

    @Given("le système a reçu une mesure de la poubelle {string}")
    public void leSystemeARecuUneMesureDeLaPoubelle(String arg0) {
    }

    @And("le service de gestion est temporairement indisponible")
    public void leServiceDeGestionEstTemporairementIndisponible() {
    }

    @When("le système tente de transmettre la mesure")
    public void leSystemeTenteDeTransmettreLaMesure() {
    }

    @Then("le système détecte que le service ne répond pas")
    public void leSystemeDetecteQueLeServiceNeRepondPas() {
    }

    @And("le système utilise une méthode alternative pour sauvegarder les données")
    public void leSystemeUtiliseUneMethodeAlternativePourSauvegarderLesDonnees() {
    }

    @And("le système confirme à la poubelle que les données sont sauvegardées")
    public void leSystemeConfirmeALaPoubelleQueLesDonneesSontSauvegardees() {
    }

    @Given("le système transmet une mesure avec des informations incomplètes")
    public void leSystemeTransmetUneMesureAvecDesInformationsIncompletes() {
    }

    @When("le service de gestion examine la transmission")
    public void leServiceDeGestionExamineLaTransmission() {
    }

    @Then("le service de gestion identifie le problème")
    public void leServiceDeGestionIdentifieLeProbleme() {
    }

    @And("le service de gestion informe le système de ce qui manque")
    public void leServiceDeGestionInformeLeSystemeDeCeQuiManque() {
    }

    @And("le système notifie la poubelle de l'erreur")
    public void leSystemeNotifieLaPoubelleDeLErreur() {
    }

    @Given("le système transmet une mesure au service de gestion")
    public void leSystemeTransmetUneMesureAuServiceDeGestion() {
    }

    @And("le service de gestion est surchargé")
    public void leServiceDeGestionEstSurcharge() {
    }

    @When("le délai de réponse dépasse le temps acceptable")
    public void leDelaiDeReponseDepasseLeTempsAcceptable() {
    }

    @Then("le système considère que la transmission a échoué")
    public void leSystemeConsidereQueLaTransmissionAEchoue() {
    }

    @And("le système utilise une méthode alternative")
    public void leSystemeUtiliseUneMethodeAlternative() {
    }

    @And("le niveau de remplissage est de {int}%")
    public void leNiveauDeRemplissageEstDe(int arg0) {
    }

    @When("le système vérifie la cohérence des données")
    public void leSystemeVerifieLaCoherenceDesDonnees() {
    }

    @Then("le système détecte que {int}% est impossible")
    public void leSystemeDetecteQueEstImpossible(int arg0) {
    }

    @And("le système ne transmet pas ces données au service de gestion")
    public void leSystemeNeTransmetPasCesDonneesAuServiceDeGestion() {
    }

    @And("le système informe la poubelle de l'erreur")
    public void leSystemeInformeLaPoubelleDeLErreur() {
    }

    @Given("le service de gestion ne répond plus")
    public void leServiceDeGestionNeRepondPlus() {
    }

    @And("le système a reçu une mesure importante")
    public void leSystemeARecuUneMesureImportante() {
    }

    @When("le système tente la transmission normale")
    public void leSystemeTenteLaTransmissionNormale() {
    }

    @And("la transmission échoue")
    public void laTransmissionEchoue() {
    }

    @Then("le système bascule automatiquement sur la sauvegarde directe")
    public void leSystemeBasculeAutomatiquementSurLaSauvegardeDirecte() {
    }

    @And("les données sont quand même conservées")
    public void lesDonneesSontQuandMemeConservees() {
    }
}
