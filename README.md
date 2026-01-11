# Smart Waste TCP Server - Documentation Complète


## Déploiement Docker

### 1. Build Docker Image
```bash
docker build -t sapue-server-tcp:latest .
```

### 2. Run Container
```bash
docker run --network sae -p 50010:50010 sapue-server-tcp:latest
```

## Build & Lancer le Serveur

### Avec Maven (Développement)

```bash
# Compiler le projet
mvn clean compile

# Lancer avec la configuration par défaut (config.yml)
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main"

# Ou avec une configuration personnalisée
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main" -Dexec.args="./config.yml"
```

## Utilisation du Serveur

### Test de connexion simple

Une fois le serveur lancé, vous pouvez communiquer avec lui via TCP sur le port 50010 :

#### Via netcat (Linux/Mac)

```bash
# Se connecter au serveur
nc localhost 50010

# Taper les commandes
PING MC-001
REGISTER MC-001 192.168.1.100
HELP
```

#### Via telnet (Windows/Linux/Mac)

```bash
telnet localhost 50010

# Taper les commandes
PING MC-001
REGISTER MC-001 192.168.1.100
```

### Exemples de commandes

**1. Enregistrer un microcontrôleur**
```bash
REGISTER MC-001 192.168.1.100
# Réponse: OK
```

**2. Envoyer des mesures de capteurs**
```bash
DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25 battery:87
# Réponse: OK
```

**3. Récupérer la configuration**
```bash
CONFIG_GET MC-001
# Réponse: OK sensorType:BME280 enabled:true
```

**4. Mettre à jour la configuration**
```bash
CONFIG_UPDATE MC-001 samplingInterval:600 enabled:true
# Réponse: OK
```

**5. Envoyer l'état du capteur**
```bash
STATUS MC-001 batteryLevel:87 uptime:3600
# Réponse: OK
```

**6. Ping (keep-alive)**
```bash
PING MC-001
# Réponse: OK
```

**7. Déconnexion**
```bash
DISCONNECT MC-001
# Réponse: OK
```

### Voir les logs du serveur

Les logs s'affichent dans la console. Vous devriez voir :

```
[INFO] Server listening on port 50010
[INFO] Connected client from 192.168.1.100
[INFO] Command received: DATA MC-001 BME280 temperature:22.5
[INFO] Data stored successfully
```

---

## Configuration

### Fichier `config.yml`

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `server.port` | int | 50010 | Port d'écoute du serveur |
| `server.maxConnections` | int | 100 | Nombre maximum de clients simultanés |
| `server.socketTimeout` | int | 30000 | Délai d'expiration socket (ms) |
| `mongodb.connectionString` | string | - | URL de connexion MongoDB |
| `mongodb.databaseName` | string | smartwaste_dev | Nom de la base de données |
| `mongodb.environment` | string | dev | Environnement (dev/prod) |
| `logging.enableMetrics` | boolean | true | Activer les métriques |
| `logging.verbose` | boolean | false | Logs détaillés |

---

## Protocole TCP

Le serveur utilise un **protocole texte délimité par espaces**. Chaque commande suit le format :

```bash
COMMAND REFERENCE [PARAMETERS]
```

Où les paramètres clé-valeur sont séparés par `:` (ex: `key:value`)

### Commandes disponibles

#### 1. REGISTER

**Enregistrer un microcontrôleur**

```bash
REGISTER <reference> <ipAddress>
```

**Paramètres:**
- `reference` : Identifiant unique du module (ex: MC-001)
- `ipAddress` : Adresse IP du microcontrôleur

**Exemple:**
```bash
REGISTER MC-001 192.168.1.100
```

**Réponses:**
- `OK` : Enregistrement réussi
- `ERR_INVALID_VALUE` : Format invalide
- `ERR_ALREADY_REGISTERED` : Module déjà connecté
- `ERR_DEVICE_NOT_FOUND` : Module inexistant en base de données

---

#### 2. DATA

**Envoyer des données de capteurs**

```bash
DATA <reference> <sensorType> <key>:<value> [<key>:<value> ...]
```

**Paramètres:**
- `reference` : Identifiant du module
- `sensorType` : Type de capteur (ex: BME280, DHT22)
- Mesures supportées :
  - `fillLevel` / `fill_level` : Niveau de remplissage (%)
  - `weight` : Poids (kg)
  - `temperature` : Température (°C)
  - `humidity` : Humidité (%)
  - `airQuality` / `air_quality` : Qualité de l'air
  - `batteryLevel` / `battery_level` / `battery` : Niveau batterie (%)
  - `wasteType` / `waste_type` : Type de déchet (texte)
  - `confidence` : Confiance de la mesure

**Exemple:**
```bash
DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25 battery:87
```

**Réponses:**
- `OK` : Données stockées avec succès
- `ERR_MISSING_PARAMS` : Paramètres manquants
- `ERR_DEVICE_NOT_REGISTERED` : Module non enregistré
- `ERR_DEVICE_NOT_FOUND` : Module inexistant
- `ERR_INVALID_VALUE` : Valeur invalide

**Comportement:**
- Les données sont stockées dans la collection `Releves`
- La collection `Poubelles` est mise à jour automatiquement avec `lastMeasurement`

---

#### 3. CONFIG_GET

**Récupérer la configuration actuelle**

```bash
CONFIG_GET <reference>
```

**Exemple:**
```bash
CONFIG_GET MC-001
```

**Réponses:**
- `OK sensorType:none enabled:false` : Configuration actuelle

---

#### 4. CONFIG_UPDATE

**Mettre à jour la configuration des capteurs**

```bash
CONFIG_UPDATE <reference> <key>:<value> [<key>:<value> ...]
```

**Exemple:**
```bash
CONFIG_UPDATE MC-001 samplingInterval:600 enabled:true
```

**Réponses:**
- `OK` : Configuration mise à jour

---

#### 5. STATUS

**Envoyer l'état du microcontrôleur**

```bash
STATUS <reference> <key>:<value> [<key>:<value> ...]
```

**Paramètres supportés:**
- `batteryLevel` / `battery` : Niveau batterie
- `uptime` : Temps de fonctionnement (secondes)
- `freeMemory` : Mémoire libre (bytes)

**Exemple:**
```bash
STATUS MC-001 batteryLevel:87 uptime:3600 freeMemory:45000
```

**Réponses:**
- `OK` : État stocké
- `ERR_DEVICE_NOT_REGISTERED` : Module non enregistré
- `ERR_DEVICE_NOT_FOUND` : Module inexistant

---

#### 6. PING

**Signal de maintien de connexion (keep-alive)**

```bash
PING <reference>
```

**Exemple:**
```bash
PING MC-001
```

**Réponses:**
- `OK` : Connexion active

---

#### 7. DISCONNECT

**Déconnexion gracieuse**

```bash
DISCONNECT <reference>
```

**Exemple:**
```bash
DISCONNECT MC-001
```

**Réponses:**
- `OK` : Déconnexion acceptée

---

#### 8. HELP

**Afficher l'aide des commandes**

```bash
HELP [COMMAND]
```

**Exemple:**
```bash
HELP REGISTER
HELP        # Affiche toutes les commandes
```

#### 9. IMAGE
**Envoyer une image (base64) à la bdd ou service d'analyse**

```bash
# envoyer une image à la base de données - créant la première partie du rapport
IMAGE DATABASE <userId> <longitude>:<latitude> <nb_bboxes> <bbox_data> <image_base64>

# update le rapport avec l'image finale après intervention - rajoutant le deuxième partie du rapport
IMAGE UPDATE <cleanerId> <reportId> <image_base64>

# envoyer une image au serveur d'analyse d'images
IMAGE ANALYSE <imageData>
```

---

### Codes d'erreur

| Code | Signification |
|------|---------------|
| `OK` | Succès |
| `ERR_MALFORMED_REQUEST` | Requête mal formée |
| `ERR_INVALID_COMMAND` | Commande inconnue |
| `ERR_MISSING_PARAMS` | Paramètres manquants |
| `ERR_INVALID_VALUE` | Valeur invalide |
| `ERR_DEVICE_NOT_FOUND` | Module inexistant en base |
| `ERR_DEVICE_NOT_REGISTERED` | Module non enregistré auprès du serveur |
| `ERR_ALREADY_REGISTERED` | Module déjà connecté |
| `ERR_DATABASE_ERROR` | Erreur d'accès à la base de données |
| `ERR_INTERNAL_ERROR` | Erreur interne du serveur |

---

## Tests

Les tests utilisent Cucumber (BDD) et JUnit.

```bash
# Lancer tous les tests
mvn test

# Tests spécifiques
mvn test -Dcucumber.features="src/test/resources/fr/smart_waste/sapue/<features file name>"
# exemple: 
# mvn test -Dcucumber.features="src/test/resources/fr/smart_waste/sapue/APICommunication.feature"

# ou plusieurs test à la fois
mvn test -Dcucumber.features="src/test/resources/fr/smart_waste/sapue/<features file n°1 name>,<features file n°2 name>,<features file n°3 name>"
# exemple: 
# mvn test -Dcucumber.features="src/test/resources/fr/smart_waste/sapue/APICommunication.feature,src/test/resources/fr/smart_waste/sapue/BinMonitoringClient.feature,src/test/resources/fr/smart_waste/sapue/ProtocolEdgeCases.feature"
```

**Scénarios disponibles:**
- `APICommunication.feature` : Communication avec l'API
- `BinMonitoringClient.feature` : Suivi des poubelles
- `ProtocolEdgeCases.feature` : Cas limites du protocole
- `ErrorRecovery.feature` : Récupération d'erreurs
- `PerformanceLoad.feature` : Tests de charge
- `MediaAnalysisServer.feature` : Analyse multimédia
- `MongoDBManipulation.feature` : Opérations base de données

---
