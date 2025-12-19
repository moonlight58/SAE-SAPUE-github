Feature: Error Recovery and System Resilience
  En tant que système de gestion des déchets
  Je veux gérer les erreurs réseau, base de données et pertes de données
  Afin d'assurer la continuité du service même en cas de problème

  Background:
    Given le système central est en fonctionnement
    And la base de données est disponible

  Scenario: Microcontroller loses connection and reconnects
    Given la poubelle "MC-001" est connectée au système
    When la connexion réseau est perdue
    Then le système détecte la déconnexion
    And le système libère les ressources du client
    When la poubelle "MC-001" tente de se reconnecter
    Then le système accepte la nouvelle connexion
    And la poubelle peut envoyer des données à nouveau

  Scenario: Microcontroller sends data during network instability
    Given la poubelle "MC-001" est connectée au système
    And le réseau est instable
    When la poubelle envoie des données de remplissage
    And le timeout de socket est atteint
    Then le système envoie "ERR_REQUEST_TIMEOUT"
    And la poubelle peut réessayer l'envoi

  Scenario: Multiple reconnection attempts from same device
    Given la poubelle "MC-001" était connectée
    And la connexion a été perdue
    When la poubelle tente de se reconnecter 3 fois en 10 secondes
    Then le système accepte toutes les tentatives valides
    And seule la dernière connexion reste active

  Scenario: System handles connection timeout gracefully
    Given la poubelle "MC-001" est connectée
    When aucune activité n'est détectée pendant le timeout configuré
    Then le système ferme la connexion automatiquement
    And les ressources sont libérées
    And les métriques sont mises à jour

  Scenario: Database connection is lost during operation
    Given la poubelle "MC-001" est connectée
    And MongoDB devient indisponible pendant l'opération
    When la poubelle envoie des données
    Then le système détecte l'erreur de base de données
    And le système envoie "ERR_DATABASE_ERROR"
    And le système tente de reconnecter à MongoDB
    When MongoDB redevient disponible
    Then les nouvelles données sont stockées correctement

  Scenario: Database recovery after complete failure
    Given MongoDB est complètement arrêté
    When le système détecte l'indisponibilité
    Then le système continue d'accepter les connexions
    And le système envoie "ERR_DATABASE_ERROR" pour toutes les écritures
    When MongoDB redémarre
    And le système détecte la disponibilité
    Then les opérations de base de données reprennent normalement

  Scenario: Partial database corruption in Releves collection
    Given des données corrompues existent dans la collection "releves"
    When le système tente de lire ces données
    Then le système détecte l'erreur de désérialisation
    And le système saute les documents corrompus
    And le système log l'erreur avec l'ID du document
    And le système continue avec les données valides

  Scenario: Transaction rollback on write failure
    Given la poubelle "MC-001" envoie des données valides
    When l'écriture dans "releves" réussit
    But la mise à jour de "poubelles.lastMeasurement" échoue
    Then le système maintient la cohérence
    And la mesure reste dans "releves"
    And un avertissement est logué

  Scenario: Data sent during system restart
    Given le système est en cours de redémarrage
    When une poubelle tente d'envoyer des données
    Then la connexion est refusée
    And la poubelle devra réessayer après le redémarrage

  Scenario: Data queue overflow during high load
    Given le système reçoit un grand volume de données
    And la base de données est lente
    When la file d'attente interne atteint sa limite
    Then le système envoie "ERR_QUEUE_FULL"
    And les nouvelles données sont rejetées temporairement
    When la file se vide
    Then le système accepte à nouveau les données

  Scenario: Incomplete data packet received
    Given la poubelle "MC-001" commence à envoyer des données
    When la connexion est interrompue en milieu de transmission
    Then le système détecte le paquet incomplet
    And le système ne stocke pas les données partielles
    And le système ferme la connexion proprement

  Scenario: Duplicate data detection
    Given la poubelle "MC-001" a envoyé des données à "14:30:00"
    When la même mesure est reçue à "14:30:01"
    Then le système détecte le doublon potentiel
    And le système stocke quand même la mesure
    And un avertissement de doublon est logué

  Scenario: System reaches maximum connection limit
    Given 100 microcontrôleurs sont connectés (limite max)
    When un nouveau microcontrôleur tente de se connecter
    Then le système refuse la connexion
    And le système log l'événement "ERR_MAX_CONNECTIONS"
    When une connexion existante se termine
    Then le nouveau client peut se connecter

  Scenario: Memory exhaustion during large data processing
    Given le système traite de grandes quantités de données
    When la mémoire disponible devient critique
    Then le système ralentit l'acceptation de nouvelles données
    And le système prioritise les données urgentes
    And le système log un avertissement de mémoire faible

  Scenario: Disk space exhaustion for MongoDB
    Given l'espace disque de MongoDB est presque plein
    When le système tente d'écrire de nouvelles données
    Then MongoDB retourne "ERR_STORAGE_FULL"
    And le système envoie une alerte critique
    And le système refuse temporairement les nouvelles données

  Scenario: API Node is unavailable affects multiple operations
    Given l'API Node est indisponible
    When plusieurs poubelles envoient des données simultanément
    Then le système bascule sur l'accès direct MongoDB pour toutes
    And toutes les données sont stockées correctement
    And les métriques montrent l'utilisation du fallback

  Scenario: Both API and direct MongoDB access fail
    Given l'API Node est indisponible
    And MongoDB direct access échoue également
    When une poubelle envoie des données
    Then le système envoie "ERR_SERVICE_UNAVAILABLE"
    And le système log l'échec complet
    And la poubelle devra réessayer plus tard

  Scenario: Concurrent updates to same Poubelle
    Given la poubelle "BIN-001" a 2 microcontrôleurs ["MC-001", "MC-002"]
    When "MC-001" et "MC-002" envoient des données simultanément
    Then les deux mesures sont stockées dans "releves"
    And "lastMeasurement" contient la mesure la plus récente
    And aucune donnée n'est perdue

  Scenario: Out-of-order data arrival
    Given la poubelle "MC-001" a envoyé des données à "14:30:00"
    When des données plus anciennes arrivent (timestampées "14:25:00")
    Then les deux mesures sont stockées dans "releves"
    And "lastMeasurement" conserve la mesure la plus récente
    And un avertissement est logué pour données hors ordre

  Scenario: System restart preserves registered devices
    Given 5 microcontrôleurs sont connectés et enregistrés
    When le système redémarre
    Then tous les clients perdent leur connexion
    And le registre des clients est vidé
    When les microcontrôleurs se reconnectent
    Then ils doivent s'enregistrer à nouveau avec REGISTER

  Scenario: System operates in degraded mode
    Given MongoDB est lent (>2s par requête)
    When le système détecte la lenteur
    Then le système passe en mode dégradé
    And le système augmente les timeouts
    And le système limite les connexions simultanées
    And le système continue de fonctionner

  Scenario: Recovery from degraded mode
    Given le système est en mode dégradé
    When MongoDB retrouve des performances normales
    Then le système détecte l'amélioration
    And le système repasse en mode normal
    And les limites sont rétablies

  Scenario: Critical errors are logged with full context
    Given une erreur critique survient
    When le système traite l'erreur
    Then l'erreur est loguée avec timestamp
    And le contexte complet est inclus (client, command, parameters)
    And la stack trace est sauvegardée
    And les métriques d'erreur sont incrémentées

  Scenario: System metrics track error rates
    Given le système est en fonctionnement
    When plusieurs erreurs surviennent
    Then les métriques totales d'erreurs augmentent
    And les métriques sont catégorisées par type d'erreur
    And le taux d'erreur peut être calculé