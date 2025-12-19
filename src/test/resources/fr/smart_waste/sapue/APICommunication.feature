Feature: Le système central communique avec le service de gestion des données
  En tant que système central de collecte
  Je dois transmettre les informations reçues au service de gestion
  Afin que les données soient traitées et stockées correctement

  Background:
    Given le système central est en fonctionnement
    And le service de gestion des données est disponible

  Scenario: Le système transmet une mesure de remplissage au service de gestion
    Given le système a reçu une mesure de remplissage de la poubelle "BIN001"
    And le niveau est de 75%
    When le système transmet cette information au service de gestion
    Then le service de gestion confirme la réception
    And le système notifie la poubelle que tout s'est bien passé

  Scenario: Le système transmet une alerte de qualité d'air
    Given le système a reçu une alerte de mauvaise qualité d'air
    And l'alerte provient de la poubelle "BIN001"
    When le système transmet cette alerte au service de gestion
    Then le service de gestion enregistre l'alerte
    And le système reçoit une confirmation

  Scenario: Le système transmet les résultats d'analyse d'un signalement
    Given le service d'analyse a identifié un type de déchet
    And le type identifié est "encombrant"
    And le niveau de confiance est de 92%
    When le système transmet ces résultats au service de gestion
    Then le service de gestion met à jour le signalement
    And le système reçoit une confirmation

  Scenario: Le système demande le statut actuel d'une poubelle
    Given une poubelle "BIN001" veut connaître son statut
    When le système interroge le service de gestion
    Then le service de gestion fournit le dernier niveau de remplissage connu
    And le service de gestion indique si une collecte est prévue
    And le système transmet ces informations à la poubelle

  Scenario: Le système demande des informations sur une poubelle inexistante
    Given une poubelle "INEXISTANT" demande sa configuration
    When le système interroge le service de gestion
    Then le service de gestion indique que cette poubelle n'existe pas
    And le système informe la poubelle qu'elle n'est pas enregistrée

  Scenario: Le service de gestion ne répond pas à une transmission
    Given le système a reçu une mesure de la poubelle "BIN001"
    And le service de gestion est temporairement indisponible
    When le système tente de transmettre la mesure
    Then le système détecte que le service ne répond pas
    And le système utilise une méthode alternative pour sauvegarder les données
    And le système confirme à la poubelle que les données sont sauvegardées

  Scenario: Le service de gestion rejette une transmission
    Given le système transmet une mesure avec des informations incomplètes
    When le service de gestion examine la transmission
    Then le service de gestion identifie le problème
    And le service de gestion informe le système de ce qui manque
    And le système notifie la poubelle de l'erreur

  Scenario: Le service de gestion met trop de temps à répondre
    Given le système transmet une mesure au service de gestion
    And le service de gestion est surchargé
    When le délai de réponse dépasse le temps acceptable
    Then le système considère que la transmission a échoué
    And le système utilise une méthode alternative
    And le système confirme à la poubelle que les données sont sauvegardées

  Scenario: Le système vérifie les données avant transmission
    Given le système a reçu une mesure de la poubelle "BIN001"
    And le niveau de remplissage est de 150%
    When le système vérifie la cohérence des données
    Then le système détecte que 150% est impossible
    And le système ne transmet pas ces données au service de gestion
    And le système informe la poubelle de l'erreur

  Scenario: Le système utilise la sauvegarde directe quand le service est indisponible
    Given le service de gestion ne répond plus
    And le système a reçu une mesure importante
    When le système tente la transmission normale
    And la transmission échoue
    Then le système bascule automatiquement sur la sauvegarde directe
    And les données sont quand même conservées
    And la poubelle reçoit une confirmation