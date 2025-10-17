# language: en
Feature: Le système central reçoit et traite les résultats d'analyse multimédia
  En tant que système central de collecte
  Je dois recevoir les résultats d'analyse des photos et vidéos
  Afin d'enrichir les signalements et alertes avec des informations précises

  Background:
    Given le système central est en fonctionnement
    And le service d'analyse multimédia peut envoyer des résultats
    And le service de gestion des données est disponible

  Scenario: Le système reçoit l'analyse d'une photo de dépôt sauvage
    When le service d'analyse se connecte au système
    And envoie les résultats d'analyse pour le signalement "REP001"
    And le type de déchet identifié est "encombrant"
    And le niveau de confiance est de 92%
    Then le système accepte ces résultats
    And le système confirme la réception au service d'analyse
    And le système transmet ces informations au service de gestion

  Scenario: Le système vérifie la cohérence des résultats d'analyse
    When le service d'analyse envoie un résultat pour le signalement "REP001"
    And le signalement "REP001" existe dans le système
    Then le système valide que le signalement existe
    And le système accepte les résultats
    And le système transmet au service de gestion

  Scenario: Le système rejette une analyse pour un signalement inexistant
    When le service d'analyse envoie un résultat pour "REP999"
    And le signalement "REP999" n'existe pas
    Then le système détecte que le signalement est introuvable
    And le système refuse les résultats
    And le système informe le service d'analyse de l'erreur

  Scenario: Le système vérifie le niveau de confiance minimal
    When le service d'analyse envoie un résultat
    And le niveau de confiance est inférieur à 50%
    Then le système accepte quand même le résultat
    And le système marque ce résultat comme "faible confiance"
    And le système transmet avec cet avertissement au service de gestion

  Scenario: Le système enrichit un signalement avec les résultats d'analyse
    Given un signalement "REP001" existe sans analyse
    When le service d'analyse envoie les résultats pour "REP001"
    And identifie le type comme "dépôt sauvage"
    Then le système demande au service de gestion d'enrichir le signalement
    And le type de déchet est ajouté au signalement
    And la date d'analyse est enregistrée
    And le statut du signalement reste inchangé

  Scenario: Le système met à jour un signalement déjà analysé
    Given un signalement "REP001" a déjà une première analyse
    When le service d'analyse envoie une nouvelle analyse plus précise
    And le nouveau niveau de confiance est supérieur
    Then le système remplace l'ancienne analyse par la nouvelle
    And le système conserve l'historique des analyses
    And le système transmet la mise à jour au service de gestion

  Scenario: Le système reçoit des résultats incomplets
    When le service d'analyse envoie des résultats
    And le type de déchet n'est pas spécifié
    Then le système détecte l'information manquante
    And le système refuse les résultats
    And le système informe le service d'analyse de ce qui manque

  Scenario: Le système reçoit des résultats mal formatés
    When le service d'analyse envoie des données illisibles
    Then le système ne peut pas interpréter les résultats
    And le système rejette la transmission
    And le système demande au service d'analyse de renvoyer correctement

  Scenario: Le service d'analyse signale un échec d'analyse
    When le service d'analyse se connecte au système
    And indique que l'analyse du signalement "REP001" a échoué
    And la raison est "image trop floue"
    Then le système enregistre cet échec
    And le système marque le signalement comme "nécessite nouvelle photo"
    And le système transmet cette information au service de gestion

  Scenario: Le service d'analyse se connecte au système
    When le service d'analyse établit une connexion
    Then le système accepte la connexion
    And le système confirme que le service peut envoyer des résultats
    And le système reste en attente des analyses

  Scenario: Le service d'analyse se déconnecte proprement
    Given le service d'analyse est connecté
    When le service d'analyse termine son travail et se déconnecte
    Then le système détecte la déconnexion
    And le système libère les ressources associées
    And le système est prêt pour une nouvelle connexion