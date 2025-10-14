# language: en
#
Feature: Les poubelles intelligentes envoient des données au système central
  En tant qu'opérateur de gestion des déchets
  Je veux que le système central reçoive et stocke les données des poubelles intelligentes
  Afin de pouvoir surveiller l'état des poubelles et planifier les collectes efficacement

  Background:
    Given le système central est en cours d'exécution
    And la base de données est disponible

  Scenario: Une nouvelle poubelle intelligente se connecte au système
    When une poubelle intelligente se connecte pour la première fois
    Then le système accepte la connexion
    And la poubelle est prête à envoyer des données

  Scenario: Plusieurs poubelles se connectent en même temps
    When 3 poubelles intelligentes se connectent simultanément
    Then le système gère toutes les connexions
    And chaque poubelle peut envoyer des données indépendamment

  Scenario: Le système reçoit le niveau de remplissage d'une poubelle
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie son niveau de remplissage de 75%
    Then le système stocke cette information
    And la poubelle reçoit une confirmation que les données ont été sauvegardées

  Scenario: Le système reçoit une mesure de poids d'une poubelle
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie une mesure de poids de 45.5 kg
    Then le système stocke l'information de poids
    And la poubelle reçoit une confirmation

  Scenario: Le système reçoit des données de qualité d'air d'une poubelle
    Given la poubelle "BIN001" est connectée au système
    When la poubelle détecte une mauvaise qualité d'air
    Then le système enregistre une alerte de qualité d'air
    And la poubelle reçoit une confirmation

  Scenario: La poubelle envoie des informations incomplètes
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie des données mais oublie d'inclure le niveau de remplissage
    Then le système rejette les données incomplètes
    And la poubelle reçoit un message d'erreur expliquant ce qui manque

  Scenario: La poubelle envoie une mesure invalide
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie un niveau de remplissage de 150%
    Then le système détecte la valeur impossible
    And la poubelle reçoit un message d'erreur sur la mesure invalide

  Scenario: La poubelle envoie des données corrompues
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie des données corrompues ou illisibles
    Then le système ne peut pas traiter l'information
    And la poubelle reçoit un message d'erreur pour réessayer

  Scenario: Le système continue de fonctionner quand la base de données a des problèmes
    Given la poubelle "BIN001" est connectée au système
    And la base de données principale est temporairement indisponible
    When la poubelle envoie des données de niveau de remplissage
    Then le système utilise une méthode de stockage de secours
    And les données sont quand même sauvegardées
    And la poubelle reçoit une confirmation

  Scenario: Le système ne peut pas du tout stocker les données
    Given la poubelle "BIN001" est connectée au système
    And les deux systèmes de stockage sont indisponibles
    When la poubelle envoie des données de niveau de remplissage
    Then le système ne peut pas sauvegarder les données
    And la poubelle reçoit un message d'erreur pour réessayer plus tard

  Scenario: La poubelle demande ses paramètres de configuration
    Given la poubelle "BIN001" est connectée et enregistrée dans le système
    When la poubelle demande sa configuration
    Then le système fournit l'intervalle de mesure
    And le système fournit le seuil d'alerte
    And la poubelle reçoit ces informations

  Scenario: Une poubelle non enregistrée demande une configuration
    Given une poubelle inconnue se connecte au système
    When la poubelle demande une configuration
    Then le système ne peut pas trouver cette poubelle dans les enregistrements
    And la poubelle reçoit une erreur qu'elle est pas enregistrée

  Scenario: La poubelle demande son statut actuel
    Given la poubelle "BIN001" est connectée au système
    When la poubelle demande son statut
    Then le système fournit le niveau de remplissage actuel
    And le système indique si une collecte est nécessaire
    And la poubelle reçoit ces informations

  Scenario: Le système reçoit l'analyse d'un signalement photo d'un citoyen
    Given un citoyen a signalé un dépôt sauvage avec une photo
    And la photo a été analysée
    When les résultats de l'analyse sont envoyés au système central
    Then le système stocke le type de déchet identifié
    And le système stocke le niveau de confiance de l'analyse
    And une confirmation est renvoyée

  Scenario: Le système reçoit les résultats de détection de vandalisme
    Given une photo d'une poubelle endommagée a été analysée
    When l'analyse détecte du vandalisme
    Then le système enregistre le type d'incident
    And le système marque le niveau de gravité
    And le statut de la poubelle est mis à jour pour nécessiter une maintenance

  Scenario: Le système valide les coordonnées de localisation de la poubelle
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie des données avec des coordonnées GPS invalides
    Then le système rejette l'information de localisation
    And la poubelle reçoit une erreur concernant la localisation invalide

  Scenario: Le système détecte un dysfonctionnement de capteur
    Given la poubelle "BIN001" est connectée au système
    When la poubelle envoie des mesures incohérentes
    Then le système détecte un possible problème de capteur
    And une alerte est générée pour la maintenance
    And la poubelle reçoit une demande de calibration