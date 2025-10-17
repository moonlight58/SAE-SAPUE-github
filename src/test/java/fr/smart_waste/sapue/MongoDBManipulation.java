package fr.smart_waste.sapue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class MongoDBManipulation {
    @Given("la base de données MongoDB est accessible")
    public void laBaseDeDonneesMongoDBEstAccessible() {
    }

    @And("les collections nécessaires existent")
    public void lesCollectionsNecessairesExistent() {
    }

    @When("une mesure de remplissage est reçue pour la poubelle {string}")
    public void uneMesureDeRemplissageEstRecuePourLaPoubelle(String arg0) {
    }

    @And("le niveau est de {int}%")
    public void leNiveauEstDe(int arg0) {
    }

    @And("la date est {string}")
    public void laDateEst(String arg0) {
    }

    @Then("les données sont stockées dans la collection des mesures")
    public void lesDonneesSontStockeesDansLaCollectionDesMesures() {
    }

    @When("une mesure de poids est reçue pour la poubelle {string}")
    public void uneMesureDePoidsEstRecuePourLaPoubelle(String arg0) {
    }

    @And("le poids est de {double} kg")
    public void lePoidsEstDeKg(int arg0, int arg1) {
    }

    @And("le taux de CO{int} est de {int} ppm")
    public void leTauxDeCOEstDePpm(int arg0, int arg1) {
    }

    @And("le taux de COV est de {int} ppb")
    public void leTauxDeCOVEstDePpb(int arg0) {
    }

    @And("toutes les valeurs sont conservées")
    public void toutesLesValeursSontConservees() {
    }

    @When("on demande les {int} dernières mesures de la poubelle {string}")
    public void onDemandeLesDernieresMesuresDeLaPoubelle(int arg0, String arg1) {
    }

    @Then("le système retourne {int} mesures")
    public void leSystemeRetourneMesures(int arg0) {
    }

    @And("les mesures sont triées de la plus récente à la plus ancienne")
    public void lesMesuresSontTrieesDeLaPlusRecenteALaPlusAncienne() {
    }

    @Given("la poubelle {string} a des mesures enregistrées")
    public void laPoubelleADesMesuresEnregistrees(String arg0) {
    }

    @Then("le système retourne toutes les mesures de cette période")
    public void leSystemeRetourneToutesLesMesuresDeCettePeriode() {
    }

    @Given("la poubelle {string} a envoyé plusieurs mesures")
    public void laPoubelleAEnvoyePlusieursMesures(String arg0) {
    }

    @When("on demande le statut actuel de la poubelle {string}")
    public void onDemandeLeStatutActuelDeLaPoubelle(String arg0) {
    }

    @Then("le système retourne la mesure la plus récente")
    public void leSystemeRetourneLaMesureLaPlusRecente() {
    }

    @And("les informations incluent le niveau de remplissage")
    public void lesInformationsIncluentLeNiveauDeRemplissage() {
    }

    @And("les informations incluent le poids")
    public void lesInformationsIncluentLePoids() {
    }

    @When("une nouvelle poubelle {string} est ajoutée au système")
    public void uneNouvellePoubelleEstAjouteeAuSysteme(String arg0) {
    }

    @And("sa localisation GPS est {string}")
    public void saLocalisationGPSEst(String arg0) {
    }

    @And("son type est {string}")
    public void sonTypeEst(String arg0) {
    }

    @And("sa capacité est de {int} litres")
    public void saCapaciteEstDeLitres(int arg0) {
    }

    @Given("{int} poubelles sont enregistrées dans le système")
    public void poubellesSontEnregistreesDansLeSysteme(int arg0) {
    }

    @When("on demande la liste complète des poubelles")
    public void onDemandeLaListeCompleteDesPoubelles() {
    }

    @Then("le système retourne {int} poubelles")
    public void leSystemeRetournePoubelles(int arg0) {
    }

    @And("chaque poubelle contient son identifiant et sa localisation")
    public void chaquePoubelleContientSonIdentifiantEtSaLocalisation() {
    }

    @When("un citoyen signale un dépôt sauvage")
    public void unCitoyenSignaleUnDepotSauvage() {
    }

    @And("la localisation GPS est {string}")
    public void laLocalisationGPSEst(String arg0) {
    }

    @And("une photo est jointe au signalement")
    public void unePhotoEstJointeAuSignalement() {
    }

    @And("le type de déchet est {string}")
    public void leTypeDeDechetEst(String arg0) {
    }

    @Then("le signalement est stocké dans la liste des signalements")
    public void leSignalementEstStockeDansLaListeDesSignalements() {
    }

    @And("le statut initial est {string}")
    public void leStatutInitialEst(String arg0) {
    }

    @And("la date de signalement est enregistrée")
    public void laDateDeSignalementEstEnregistree() {
    }

    @Given("un signalement {string} existe avec le statut {string}")
    public void unSignalementExisteAvecLeStatut(String arg0, String arg1) {
    }

    @When("un admin prend en charge le signalement")
    public void unAdminPrendEnChargeLeSignalement() {
    }

    @Then("le statut du signalement passe à {string}")
    public void leStatutDuSignalementPasseA(String arg0) {
    }

    @And("la date de prise en charge est enregistrée")
    public void laDateDePriseEnChargeEstEnregistree() {
    }

    @Given("un signalement {string} a le statut {string}")
    public void unSignalementALeStatut(String arg0, String arg1) {
    }

    @When("une photo de confirmation de nettoyage est ajoutée")
    public void unePhotoDeConfirmationDeNettoyageEstAjoutee() {
    }

    @And("la date de nettoyage est enregistrée")
    public void laDateDeNettoyageEstEnregistree() {
    }

    @And("la photo après nettoyage est stockée")
    public void laPhotoApresNettoyageEstStockee() {
    }

    @Given("plusieurs poubelles existent dans différentes zones")
    public void plusieursPoubellesExistentDansDifferentesZones() {
    }

    @When("on demande le taux de remplissage moyen par zone")
    public void onDemandeLeTauxDeRemplissageMoyenParZone() {
    }

    @Then("le système calcule la moyenne pour chaque zone")
    public void leSystemeCalculeLaMoyennePourChaqueZone() {
    }

    @And("les résultats sont retournés groupés par zone")
    public void lesResultatsSontRetournesGroupesParZone() {
    }

    @Given("plusieurs poubelles ont des niveaux de remplissage différents")
    public void plusieursPoubellesOntDesNiveauxDeRemplissageDifferents() {
    }

    @When("on recherche les poubelles avec un niveau supérieur à {int}%")
    public void onRechercheLesPoubellesAvecUnNiveauSuperieurA(int arg0) {
    }

    @Then("le système retourne uniquement les poubelles concernées")
    public void leSystemeRetourneUniquementLesPoubellesConcernees() {
    }

    @And("les résultats sont triés par niveau décroissant")
    public void lesResultatsSontTriesParNiveauDecroissant() {
    }

    @Given("plusieurs signalements existent avec différents types de déchets")
    public void plusieursSignalementsExistentAvecDifferentsTypesDeDechets() {
    }

    @When("on demande les statistiques par type de déchet")
    public void onDemandeLesStatistiquesParTypeDeDechet() {
    }

    @Then("le système compte le nombre de signalements pour chaque type")
    public void leSystemeCompteLeNombreDeSignalementsPourChaqueType() {
    }

    @And("les résultats incluent {string}, {string}, {string}")
    public void lesResultatsIncluent(String arg0, String arg1, String arg2) {
    }

    @When("une mesure est reçue pour une poubelle {string}")
    public void uneMesureEstRecuePourUnePoubelle(String arg0) {
    }

    @And("les données ne sont pas enregistrées")
    public void lesDonneesNeSontPasEnregistrees() {
    }

    @And("une erreur est retournée")
    public void uneErreurEstRetournee() {
    }

    @Given("une mesure a été enregistrée pour {string} à {string}")
    public void uneMesureAEteEnregistreePourA(String arg0, String arg1) {
    }

    @When("la même mesure est reçue à nouveau")
    public void laMemeMesureEstRecueANouveau() {
    }

    @Then("le système détecte le doublon")
    public void leSystemeDetecteLeDoublon() {
    }

    @And("un avertissement est généré")
    public void unAvertissementEstGenere() {
    }

    @And("l\\'enregistrement contient l\\'identifiant de la poubelle")
    public void lEnregistrementContientLIdentifiantDeLaPoubelle() {
    }

    @And("l\\'enregistrement contient le niveau de remplissage")
    public void lEnregistrementContientLeNiveauDeRemplissage() {
    }

    @And("l\\'enregistrement contient la date et l\\'heure")
    public void lEnregistrementContientLaDateEtLHeure() {
    }

    @And("l\\'enregistrement est horodaté automatiquement")
    public void lEnregistrementEstHorodateAutomatiquement() {
    }

    @When("une mesure de qualité d\\'air est reçue pour la poubelle {string}")
    public void uneMesureDeQualiteDAirEstRecuePourLaPoubelle(String arg0) {
    }

    @Given("la poubelle {string} a envoyé {int} mesures aujourd\\'hui")
    public void laPoubelleAEnvoyeMesuresAujourdHui(String arg0, int arg1) {
    }

    @When("on demande l\\'historique entre le {string} et le {string}")
    public void onDemandeLHistoriqueEntreLeEtLe(String arg0, String arg1) {
    }

    @And("l\\'enregistrement contient toutes les informations")
    public void lEnregistrementContientToutesLesInformations() {
    }

    @Then("le système vérifie l\\'existence de la poubelle")
    public void leSystemeVerifieLExistenceDeLaPoubelle() {
    }

    @And("la poubelle n\\'est pas trouvée")
    public void laPoubelleNEstPasTrouvee() {
    }

    @And("la mesure en double n\\'est pas enregistrée")
    public void laMesureEnDoubleNEstPasEnregistree() {
    }

    @And("les mesures sont renvoyées")
    public void lesMesuresSontRenvoyees() {
    }

    @Then("la poubelle est enregistrée dans la base de données")
    public void laPoubelleEstEnregistreeDansLaBaseDeDonnees() {
    }
}
