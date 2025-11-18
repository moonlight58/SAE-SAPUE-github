# language: en
Feature: Stockage et récupération des données dans la base de données
  En tant que système de gestion des déchets
  Je veux stocker et récupérer les données des poubelles intelligentes
  Afin de conserver un historique et permettre l'analyse des données

  Background:
    Given la base de données MongoDB est accessible
    And les collections nécessaires existent

  Scenario: Enregistrement d'une mesure de niveau de remplissage
    When une mesure de remplissage est reçue pour la poubelle "BIN001"
    And le niveau est de 75%
    And la date est "2025-10-15T14:30:00Z"
    Then les données sont stockées dans la collection des mesures
    And l'enregistrement contient l'identifiant de la poubelle
    And l'enregistrement contient le niveau de remplissage
    And l'enregistrement contient la date et l'heure

  Scenario: Enregistrement d'une mesure de poids
    When une mesure de poids est reçue pour la poubelle "BIN001"
    And le poids est de 45.5 kg
    Then les données sont stockées dans la collection des mesures
    And l'enregistrement est horodaté automatiquement

  Scenario: Enregistrement d'une mesure de qualité d'air
    When une mesure de qualité d'air est reçue pour la poubelle "BIN001"
    And le taux de CO2 est de 450 ppm
    And le taux de COV est de 120 ppb
    Then les données sont stockées dans la collection des mesures
    And toutes les valeurs sont conservées

  Scenario: Récupération des dernières mesures d'une poubelle
    Given la poubelle "BIN001" a envoyé 10 mesures aujourd'hui
    When on demande les 5 dernières mesures de la poubelle "BIN001"
    Then le système retourne 5 mesures
    And les mesures sont triées de la plus récente à la plus ancienne

  Scenario: Récupération de l'historique d'une poubelle sur une période
    Given la poubelle "BIN001" a des mesures enregistrées
    When on demande l'historique entre le "2025-10-01" et le "2025-10-15"
    Then le système retourne toutes les mesures de cette période
    And les mesures sont renvoyées

  Scenario: Récupération du statut actuel d'une poubelle
    Given la poubelle "BIN001" a envoyé plusieurs mesures
    When on demande le statut actuel de la poubelle "BIN001"
    Then le système retourne la mesure la plus récente
    And les informations incluent le niveau de remplissage
    And les informations incluent le poids

  Scenario: Enregistrement d'une nouvelle poubelle dans le système
    When une nouvelle poubelle "BIN005" est ajoutée au système
    And sa localisation GPS est "47.2380, 6.0240"
    And son type est "poubelle urbaine"
    And sa capacité est de 120 litres
    Then la poubelle est enregistrée dans la base de données
    And l'enregistrement contient toutes les informations

  Scenario: Récupération de la liste de toutes les poubelles
    Given 5 poubelles sont enregistrées dans le système
    When on demande la liste complète des poubelles
    Then le système retourne 5 poubelles
    And chaque poubelle contient son identifiant et sa localisation

  Scenario: Enregistrement d'un signalement de dépôt sauvage
    When un citoyen signale un dépôt sauvage
    And la localisation GPS est "47.2385, 6.0245"
    And une photo est jointe au signalement
    And le type de déchet est "encombrant"
    Then le signalement est stocké dans la liste des signalements
    And le statut initial est "signalé"
    And la date de signalement est enregistrée

  Scenario: Mise à jour du statut d'un signalement
    Given un signalement "REP001" existe avec le statut "signalé"
    When un admin prend en charge le signalement
    Then le statut du signalement passe à "en cours"
    And la date de prise en charge est enregistrée

  Scenario: Finalisation d'un signalement avec photo de confirmation
    Given un signalement "REP001" a le statut "en cours"
    When une photo de confirmation de nettoyage est ajoutée
    Then le statut du signalement passe à "nettoyé"
    And la date de nettoyage est enregistrée
    And la photo après nettoyage est stockée

  Scenario: Calcul du taux de remplissage moyen par zone
    Given plusieurs poubelles existent dans différentes zones
    When on demande le taux de remplissage moyen par zone
    Then le système calcule la moyenne pour chaque zone
    And les résultats sont retournés groupés par zone

  Scenario: Identification des poubelles nécessitant une collecte
    Given plusieurs poubelles ont des niveaux de remplissage différents
    When on recherche les poubelles avec un niveau supérieur à 80%
    Then le système retourne uniquement les poubelles concernées
    And les résultats sont triés par niveau décroissant

  Scenario: Comptage des signalements par type de déchet
    Given plusieurs signalements existent avec différents types de déchets
    When on demande les statistiques par type de déchet
    Then le système compte le nombre de signalements pour chaque type
    And les résultats incluent "dépôt sauvage", "encombrant", "dangereux"

  Scenario: Rejet d'une donnée avec un identifiant de poubelle invalide
    When une mesure est reçue pour une poubelle "INEXISTANT"
    Then le système vérifie l'existence de la poubelle
    And la poubelle n'est pas trouvée
    And les données ne sont pas enregistrées
    And une erreur est retournée

  Scenario: Vérification des doublons de mesures
    Given une mesure a été enregistrée pour "BIN001" à "14:30:00"
    When la même mesure est reçue à nouveau
    Then le système détecte le doublon
    And la mesure en double n'est pas enregistrée
    And un avertissement est généré