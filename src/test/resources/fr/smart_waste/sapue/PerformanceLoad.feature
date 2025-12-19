# language: en
Feature: Performance and Load Testing
  En tant que système de gestion des déchets
  Je veux gérer des charges élevées et des volumes de données importants
  Afin d'assurer des performances acceptables en production

  Background:
    Given le système central est en fonctionnement
    And la base de données est disponible
    And les métriques de performance sont activées

  Scenario: Single microcontroller sends high-frequency data
    Given la poubelle "MC-001" est connectée
    And l'intervalle d'échantillonnage est de 10 secondes
    When la poubelle envoie 100 mesures en 1000 secondes
    Then toutes les mesures sont stockées correctement
    And le temps de réponse moyen est inférieur à 200ms
    And aucune mesure n'est perdue
    And les métriques montrent 100 requêtes réussies

  Scenario: Multiple microcontrollers send data simultaneously
    Given 10 microcontrôleurs sont connectés
    When chaque microcontrôleur envoie des données en même temps
    Then le système traite toutes les requêtes en parallèle
    And chaque requête reçoit une réponse en moins de 500ms
    And toutes les données sont stockées dans la base
    And les métriques montrent 10 connexions actives

  Scenario: Burst of data from single device
    Given la poubelle "MC-001" est connectée
    When la poubelle envoie 50 mesures en 5 secondes
    Then le système accepte toutes les requêtes
    And les données sont mises en file d'attente si nécessaire
    And toutes les mesures sont finalement stockées
    And le système ne retourne pas d'erreur de rate limit

  Scenario: Sustained high data rate from fleet
    Given 50 microcontrôleurs sont connectés
    And chacun envoie une mesure toutes les 30 secondes
    When le système fonctionne pendant 10 minutes
    Then environ 1000 mesures sont reçues (50 × 20)
    And le taux de réussite est supérieur à 99%
    And le temps de réponse moyen reste stable
    And la mémoire système reste dans des limites acceptables

  Scenario: Peak load during daily collection time
    Given 80 microcontrôleurs sont connectés
    When tous envoient des données en pic (8h du matin)
    And le trafic augmente de 300% par rapport à la normale
    Then le système continue de répondre
    And les temps de réponse augmentent mais restent < 1s
    And aucune connexion n'est abandonnée
    And le système log les conditions de pic

  Scenario: Insert 1000 measurements in batch
    Given le système doit insérer 1000 mesures historiques
    When les insertions sont effectuées séquentiellement
    Then toutes les insertions réussissent
    And l'opération complète en moins de 30 secondes
    And chaque "poubelle.lastMeasurement" est correctement mise à jour
    And la cohérence des données est maintenue

  Scenario: Query large historical dataset
    Given 10000 mesures existent dans la collection "releves"
    When on recherche toutes les mesures d'une poubelle pour 1 mois
    Then le système retourne les résultats en moins de 5 secondes
    And les résultats sont correctement triés
    And la mémoire utilisée reste raisonnable
    And la pagination peut être appliquée si nécessaire

  Scenario: Update multiple Poubelles simultaneously
    Given 100 poubelles existent dans le système
    When un script met à jour les alertes de toutes les poubelles
    Then toutes les mises à jour réussissent
    And l'opération complète en moins de 10 secondes
    And aucune corruption de données ne se produit
    And les index sont utilisés efficacement

  Scenario: Delete old measurements (data cleanup)
    Given 50000 mesures anciennes (> 1 an) existent
    When le système exécute une purge des données anciennes
    Then les anciennes mesures sont supprimées
    And l'opération complète en moins de 60 secondes
    And les mesures récentes ne sont pas affectées
    And l'espace disque est récupéré

  Scenario: Aggregate statistics across all bins
    Given 500 poubelles ont des mesures récentes
    When le système calcule les statistiques globales
    Then les moyennes de remplissage par zone sont calculées
    And le nombre total de poubelles pleines est compté
    And l'opération complète en moins de 3 secondes
    And les résultats sont précis

  Scenario: System reaches 80% of maximum connections
    Given la limite max est de 100 connexions
    When 80 microcontrôleurs sont connectés
    Then le système fonctionne normalement
    And les temps de réponse restent acceptables
    And les métriques montrent 80 connexions actives
    And de nouvelles connexions sont encore acceptées

  Scenario: System handles connection churn
    Given 50 microcontrôleurs se connectent
    When 10 se déconnectent et 10 nouveaux se connectent chaque minute
    Then le système maintient environ 50 connexions actives
    And les ressources sont correctement libérées
    And aucune fuite mémoire ne se produit
    And les performances restent stables

  Scenario: Database load increases gradually
    Given le système fonctionne avec un trafic normal
    When le nombre de requêtes augmente de 10% toutes les minutes
    Then le système continue de répondre
    And MongoDB gère l'augmentation de charge
    And les temps de réponse augmentent proportionnellement
    When la charge dépasse la capacité
    Then le système commence à rejeter les connexions gracieusement

  Scenario: Memory usage under sustained load
    Given le système fonctionne avec 75 connexions actives
    When chaque connexion envoie des données toutes les 30 secondes
    And le système fonctionne pendant 1 heure
    Then l'utilisation mémoire reste stable
    And aucune fuite mémoire n'est détectée
    And le garbage collector fonctionne correctement
    And les métriques montrent une utilisation mémoire constante

  Scenario: CPU usage during peak load
    Given le système traite 100 requêtes simultanées
    When l'utilisation CPU atteint 80%
    Then le système continue de traiter les requêtes
    And les requêtes prioritaires sont traitées en premier
    And le système ne crash pas
    And un avertissement de charge élevée est logué

  Scenario: Measure data throughput rate
    Given 50 microcontrôleurs envoient des données en continu
    When le système mesure le débit sur 5 minutes
    Then le débit entrant est calculé (Ko/s)
    And le débit sortant est calculé (Ko/s)
    And les métriques sont accessibles
    And le débit reste dans les limites réseau

  Scenario: Large data payload handling
    Given une poubelle envoie une mesure avec beaucoup de paramètres
    When le payload fait 5 Ko (limite raisonnable)
    Then le système accepte et traite les données
    And le temps de réponse est proportionnel à la taille
    And la bande passante est gérée correctement

  Scenario: Network latency impact
    Given la latence réseau est de 100ms
    When une poubelle envoie des données
    Then le temps de réponse total inclut la latence
    And le système ne timeout pas prématurément
    And les données sont stockées correctement

  Scenario: MongoDB index usage verification
    Given des index existent sur les champs critiques
    When une recherche par "microcontroller" est effectuée
    Then l'index est utilisé (pas de collection scan)
    And la requête s'exécute en moins de 50ms
    And les métriques MongoDB confirment l'usage d'index

  Scenario: Write operations performance
    Given le système effectue des écritures fréquentes
    When 1000 insertions consécutives sont effectuées
    Then le temps moyen par insertion est < 20ms
    And MongoDB utilise le write concern approprié
    And aucun goulet d'étranglement n'est détecté

  Scenario: Read operations performance
    Given 10000 documents existent dans "releves"
    When 100 lectures aléatoires sont effectuées
    Then le temps moyen par lecture est < 10ms
    And les lectures utilisent les index appropriés
    And le cache MongoDB est utilisé efficacement

  Scenario: Concurrent read and write operations
    Given 20 threads lisent des données simultanément
    And 10 threads écrivent des données simultanément
    When les opérations s'exécutent pendant 60 secondes
    Then aucun deadlock ne se produit
    And toutes les lectures retournent des données cohérentes
    And toutes les écritures réussissent
    And les performances restent acceptables

  Scenario: Maximum connections stress test
    Given le système accepte jusqu'à 100 connexions
    When exactement 100 microcontrôleurs se connectent
    Then toutes les connexions sont acceptées
    And le système fonctionne normalement
    When un 101ème microcontrôleur tente de se connecter
    Then la connexion est refusée avec "ERR_MAX_CONNECTIONS"

  Scenario: Rapid connect-disconnect cycles
    Given un microcontrôleur se connecte et se déconnecte rapidement
    When ce cycle se répète 100 fois en 60 secondes
    Then toutes les connexions/déconnexions réussissent
    And les ressources sont correctement libérées
    And aucune fuite de ressources ne se produit
    And le système reste stable

  Scenario: Data validation overhead
    Given le système valide toutes les données entrantes
    When 1000 requêtes avec données valides sont envoyées
    Then la validation n'ajoute pas plus de 10ms par requête
    And toutes les validations réussissent
    And les performances restent acceptables

  Scenario: System identifies performance bottlenecks
    Given le système est sous charge élevée
    When les métriques sont analysées
    Then les goulots d'étranglement sont identifiés
    And les métriques montrent où le temps est passé
    And des recommandations peuvent être faites

  Scenario: Resource usage scaling
    Given 10 connexions utilisent X ressources
    When le nombre de connexions passe à 50
    Then l'utilisation des ressources est proportionnelle
    And aucune croissance exponentielle n'est observée
    And le système reste stable

  Scenario: Response time degradation analysis
    Given le système fonctionne avec différentes charges
    When on compare les temps de réponse à 10%, 50%, 90% de charge
    Then la dégradation est linéaire ou sous-linéaire
    And le système ne montre pas de dégradation soudaine
    And les métriques permettent de prédire le comportement