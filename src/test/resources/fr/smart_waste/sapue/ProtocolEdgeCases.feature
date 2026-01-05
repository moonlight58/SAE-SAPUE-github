Feature: TCP Protocol Edge Cases and Robustness
  En tant que serveur TCP
  Je veux gérer tous les cas limites et malformations de protocole
  Afin d'assurer la robustesse et la sécurité du système

  Background:
    Given le système central est en fonctionnement (Protocol)
    And la base de données est disponible

  Scenario: REGISTER with minimum valid reference length
    Given "MC-1" existe dans la base
    When un client envoie "REGISTER MC-1 192.168.1.100"
    And la référence fait exactement 3 caractères
    Then le système accepte l'enregistrement

  Scenario: REGISTER with maximum valid reference length
    Given "MC-VERY-LONG-REFERENCE-NAME-12345678901234567890" existe dans la base
    When un client envoie "REGISTER MC-VERY-LONG-REFERENCE-NAME-12345678901234567890 192.168.1.100"
    And la référence fait exactement 50 caractères
    Then le système valide la longueur
    And le système accepte si la référence existe en base

  Scenario: REGISTER with reference containing special characters
    Given "MC_001" existe dans la base
    And "MC-001" existe dans la base
    When un client envoie "REGISTER MC_001 192.168.1.100"
    Then le système accepte les underscores
    When un client envoie "REGISTER MC-001 192.168.1.100"
    Then le système accepte les hyphens
    When un client envoie "REGISTER MC@001 192.168.1.100"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: REGISTER with invalid IP addresses
    When un client envoie "REGISTER MC-001 256.168.1.100"
    Then le système rejette avec "ERR_INVALID_VALUE"
    When un client envoie "REGISTER MC-001 192.168.1"
    Then le système rejette avec "ERR_INVALID_VALUE"
    When un client envoie "REGISTER MC-001 192.168.1.100.1"
    Then le système rejette avec "ERR_INVALID_VALUE"
    When un client envoie "REGISTER MC-001 not-an-ip"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: REGISTER with extra whitespace
    Given "MC-001" existe dans la base
    When un client envoie "REGISTER  MC-001  192.168.1.100"
    Then le système normalise les espaces multiples
    And le système traite la commande correctement

  Scenario: REGISTER with trailing spaces
    Given "MC-001" existe dans la base
    When un client envoie "REGISTER MC-001 192.168.1.100   "
    Then le système supprime les espaces de fin
    And le système traite la commande correctement

  Scenario: REGISTER while already registered
    Given "MC-001" est déjà enregistré et connecté
    When le même client tente de se réenregistrer
    Then le système retourne "ERR_ALREADY_REGISTERED"
    And la connexion existante reste active

  Scenario: REGISTER with reference not in database
    When un client envoie "REGISTER UNKNOWN-MC 192.168.1.100"
    And "UNKNOWN-MC" n'existe pas dans la collection microcontrolleurs
    Then le système retourne "ERR_DEVICE_NOT_FOUND"
    And la connexion n'est pas enregistrée

  Scenario: DATA with missing sensor type
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 temperature:22.5"
    Then le système rejette avec "ERR_MISSING_PARAMS"

  Scenario: DATA with unknown sensor type
    Given "MC-001" a un configSensor.sensorType "BME280"
    When un client envoie "DATA MC-001 UNKNOWN_SENSOR temperature:22.5"
    Then le système rejette avec "ERR_SENSOR_NOT_FOUND"

  Scenario: DATA with sensor type mismatch
    Given "MC-001" a un configSensor.sensorType "BME280"
    When un client envoie "DATA MC-001 HX711 weight:45.5"
    Then le système rejette avec "ERR_SENSOR_NOT_FOUND"

  Scenario: DATA with empty value
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature:"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: DATA with empty key
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 :22.5"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: DATA with invalid number format
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature:abc"
    Then le système rejette avec "ERR_INVALID_VALUE"
    When un client envoie "DATA MC-001 BME280 temperature:22.5.6"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: DATA with extreme values
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature:9999999"
    Then le système stocke la valeur (pas de validation de plage)
    And un avertissement peut être logué

  Scenario: DATA with negative values
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature:-40.0"
    Then le système accepte les valeurs négatives
    And les données sont stockées correctement

  Scenario: DATA with floating point precision
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature:22.123456789"
    Then le système stocke la valeur avec précision double
    And aucune perte de précision significative ne se produit

  Scenario: DATA with many parameters
    Given "MC-001" est enregistré
    When un client envoie 20 paires key:value dans une commande DATA
    Then le système traite tous les paramètres
    And toutes les valeurs reconnues sont stockées
    And les valeurs non reconnues génèrent des avertissements

  Scenario: DATA with duplicate keys
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature:22.5 temperature:23.0"
    Then le système utilise la dernière valeur (23.0)
    And un avertissement de doublon peut être logué

  Scenario: DATA with malformed key:value separator
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 temperature=22.5"
    Then le système rejette avec "ERR_INVALID_FORMAT"
    When un client envoie "DATA MC-001 BME280 temperature 22.5"
    Then le système rejette avec "ERR_INVALID_FORMAT"

  Scenario: CONFIG_GET for device without configuration
    Given "MC-001" est enregistré
    And "MC-001" n'a pas de configSensor dans la base
    When un client envoie "CONFIG_GET MC-001"
    Then le système retourne "OK sensorType:none enabled:false"

  Scenario: CONFIG_GET with extra parameters (ignored)
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_GET MC-001 extra:param"
    Then les paramètres supplémentaires sont ignorés
    And le système retourne la configuration correctement

  Scenario: CONFIG_UPDATE with boolean as string
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_UPDATE MC-001 enabled:true"
    Then le système parse "true" comme boolean
    And la configuration est mise à jour
    When un client envoie "CONFIG_UPDATE MC-001 enabled:TRUE"
    Then le système accepte aussi (case-insensitive)

  Scenario: CONFIG_UPDATE with invalid boolean
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_UPDATE MC-001 enabled:yes"
    Then le système parse "yes" comme string (pas boolean)
    And stocke dans parameters

  Scenario: CONFIG_UPDATE with negative samplingInterval
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_UPDATE MC-001 samplingInterval:-100"
    Then le système accepte mais peut loguer un avertissement

  Scenario: CONFIG_UPDATE with zero samplingInterval
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_UPDATE MC-001 samplingInterval:0"
    Then le système accepte la valeur
    And la configuration est mise à jour

  Scenario: CONFIG_UPDATE with float for integer field
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_UPDATE MC-001 samplingInterval:300.5"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: CONFIG_UPDATE with custom parameters
    Given "MC-001" est enregistré
    When un client envoie "CONFIG_UPDATE MC-001 customParam:customValue"
    Then le système stocke dans configSensor.parameters
    And la valeur est accessible via CONFIG_GET

  Scenario: STATUS with battery level > 100%
    Given "MC-001" est enregistré
    When un client envoie "STATUS MC-001 battery:150"
    Then le système stocke la valeur (validation optionnelle)
    And un avertissement peut être logué

  Scenario: STATUS with negative battery
    Given "MC-001" est enregistré
    When un client envoie "STATUS MC-001 battery:-10"
    Then le système stocke la valeur
    And un avertissement est logué

  Scenario: STATUS with all valid metrics
    Given "MC-001" est enregistré
    When un client envoie "STATUS MC-001 battery:87 uptime:3600 freeMemory:45000 rssi:-65 voltage:3.7"
    Then toutes les métriques sont stockées
    And le système retourne "OK"

  Scenario: Command with lowercase
    Given "MC-001" existe dans la base
    When un client envoie "register MC-001 192.168.1.100"
    Then le système convertit en majuscules
    And traite la commande normalement

  Scenario: Command with mixed case
    Given "MC-001" existe dans la base
    When un client envoie "ReGiStEr MC-001 192.168.1.100"
    Then le système convertit en majuscules
    And traite la commande normalement

  Scenario: Empty command
    When un client envoie ""
    Then le système rejette avec "ERR_MALFORMED_REQUEST"

  Scenario: Command with only whitespace
    When un client envoie "   "
    Then le système rejette avec "ERR_MALFORMED_REQUEST"

  Scenario: Command with line breaks
    When un client envoie "REGISTER MC-001\n192.168.1.100"
    Then le système traite jusqu'au line break
    And rejette probablement avec "ERR_MISSING_PARAMS"

  Scenario: Very long command line
    When un client envoie une commande de 10000 caractères
    Then le système peut limiter la longueur

  Scenario: Two clients register with same reference simultaneously
    When "Client A" et "Client B" envoient "REGISTER MC-001" en même temps
    Then un seul client est accepté
    And l'autre reçoit "ERR_ALREADY_REGISTERED"
    And aucune condition de course ne se produit

  Scenario: Client sends commands rapidly
    Given "MC-001" est enregistré
    When le client envoie 10 commandes DATA en moins de 1 seconde
    Then toutes les commandes sont traitées
    And toutes les données sont stockées correctement
    And l'ordre d'exécution est préservé

  Scenario: Client sends command during disconnect
    Given "MC-001" est enregistré
    When le client commence à envoyer une commande DATA
    And la connexion est fermée pendant la transmission
    Then le système détecte la déconnexion
    And la commande partielle est abandonnée
    And les ressources sont libérées

  Scenario: Reference with only numbers
    Given "123" existe dans la base
    When un client envoie "REGISTER 123 192.168.1.100"
    Then le système accepte (alphanumeric valide)

  Scenario: Reference with Unicode characters
    When un client envoie "REGISTER MC-ÃƒÂ©Ã -001 192.168.1.100"
    Then le système rejette avec "ERR_INVALID_VALUE"

  Scenario: Reference starting with hyphen
    Given "-MC001" existe dans la base
    When un client envoie "REGISTER -MC001 192.168.1.100"
    Then le système accepte (regex permet hyphen)

  Scenario: Reference with consecutive hyphens
    Given "MC--001" existe dans la base
    When un client envoie "REGISTER MC--001 192.168.1.100"
    Then le système accepte (pas de restriction)

  Scenario: Send DATA before REGISTER
    When un client non enregistré envoie "DATA MC-001 BME280 temperature:22.5"
    Then le système rejette avec "ERR_DEVICE_NOT_REGISTERED"

  Scenario: Send CONFIG_GET before REGISTER
    When un client non enregistré envoie "CONFIG_GET MC-001"
    Then le système rejette avec "ERR_DEVICE_NOT_REGISTERED"

  Scenario: Multiple DISCONNECT commands
    Given "MC-001" est enregistré
    When le client envoie "DISCONNECT MC-001"
    Then le système retourne "OK"
    When le client envoie à nouveau "DISCONNECT MC-001"
    Then la connexion est déjà fermée

  Scenario: Data with special characters in string values
    Given "MC-001" est enregistré
    When un client envoie "DATA MC-001 BME280 location:Rue_de_l'Ã©glise"
    Then le système rejette si caractères non-ASCII

  Scenario: Command with null bytes
    When un client envoie une commande contenant '\0'
    Then le système traite jusqu'au null byte

  Scenario: Command with tab characters
    When un client envoie "REGISTER\tMC-001\t192.168.1.100"
    Then le système peut traiter les tabs comme espaces
