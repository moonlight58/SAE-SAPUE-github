# Smart Waste TCP Server - Documentation Complète

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Prérequis](#prérequis)
3. [Installation et Configuration](#installation-et-configuration)
4. [Build du Projet](#build-du-projet)
5. [Lancer le Serveur](#lancer-le-serveur)
6. [Utilisation du Serveur](#utilisation-du-serveur)
7. [Protocole TCP](#protocole-tcp)
8. [Configuration](#configuration)
9. [Schéma MongoDB](#schéma-mongodb)
10. [Déploiement Docker](#déploiement-docker)
11. [Tests](#tests)
12. [Dépannage](#dépannage)

---

## Vue d'ensemble

**Smart Waste TCP Server** est un serveur TCP multi-threadé conçu pour centraliser la gestion des données de déchets intelligents. Il reçoit les connexions des microcontrôleurs (ESP32/ESP8266) et traite les mesures de capteurs.

### Caractéristiques principales

- ✅ **Architecture multi-threadée** : Modèle thread-par-client avec limites configurables
- ✅ **Protocole texte** : Protocole TCP délimité par espaces avec codes d'erreur standardisés
- ✅ **MongoDB** : Schéma complet avec validation et indexation
- ✅ **Métriques en temps réel** : Suivi des connexions, requêtes, erreurs et transferts
- ✅ **Mises à jour automatiques** : Synchronisation des `lastMeasurement` dans la collection Poubelles

### 🚀 Quickstart (30 secondes)

```bash
# 1. Lancer MongoDB en Docker
docker run -d --name mongodb -p 27017:27017 mongo:latest

# 2. Compiler le projet
mvn clean package -DskipTests

# 3. Initialiser la base de données (voir section Installation)
mongosh mongodb://localhost:27017 < init-db.js

# 4. Lancer le serveur
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main"

# 5. Tester la connexion dans un autre terminal
echo "PING MC-001" | nc localhost 50010

# ou avec telnet
telnet localhost 50010

```

Pour une setup complète et détaillée, continuez avec la section [Installation et Configuration](#installation-et-configuration).

---

## Prérequis

- **Java 17+** : [Télécharger Java](https://www.oracle.com/java/technologies/downloads/)
- **MongoDB 4.4+** : [Télécharger MongoDB](https://www.mongodb.com/try/download/community)
- **Maven 3.9+** : [Télécharger Maven](https://maven.apache.org/download.cgi)
- **Git** (optionnel) : Pour cloner le projet

**Vérifier les versions installées:**
```bash
java -version
mvn -version
mongosh --version  # ou mongo --version pour les anciennes versions
```

---

## Build du Projet

### Compiler avec Maven

```bash
# Compiler le projet (télécharge les dépendances)
mvn clean compile

# Ou compiler + packager en JAR
mvn clean package -DskipTests

# Ou compiler + packager + lancer les tests
mvn clean package
```

### Vérifier que le build est OK

```bash
# Lister les fichiers générés
ls -la target/
```

---

## Lancer le Serveur

### Méthode 1 : Avec Maven (Développement)

```bash
# Lancer avec la configuration par défaut (config.yml)
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main"

# Ou avec une configuration personnalisée
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main" -Dexec.args="./config.yml"
```

### Méthode 2 : Avec Docker (Production)

```bash
# Build l'image Docker
docker build -t sapue-server:latest .

# Lancer le serveur dans un conteneur
docker run -d \
  --name sapue-server \
  -p 50010:50010 \
  --link mongodb:mongodb \
  -e MONGO_URI="mongodb://mongodb:27017" \
  -e DB_NAME="smartwaste_dev" \
  sapue-server:latest

# Vérifier les logs
docker logs sapue-server

# Arrêter le serveur
docker stop sapue-server
docker rm sapue-server
```

### Méthode 3 : Avec Docker Compose (Recommandé)

```bash
# Lancer MongoDB + serveur
docker-compose up -d

# Arrêter les services
docker-compose down

# Voir les logs
docker-compose logs -f sapue-server
```

### Vérifier que le serveur est actif

```bash
# Le serveur écoute sur le port 50010 (par défaut)
netstat -an | grep 50010

# Ou tester la connexion
telnet localhost 50010

# Ou avec nc (netcat)
nc -zv localhost 50010
```

---

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

## Déploiement Docker

### Docker simple

```bash
docker build -t sapue-server .
docker run -d \
  --name sapue-server \
  -p 50010:50010 \
  -e MONGO_URI="mongodb://host.docker.internal:27017" \
  -e DB_NAME="smartwaste_dev" \
  sapue-server
```

### Docker Compose

Voir `docker-compose.yml` pour un déploiement avec MongoDB intégré.

```bash
docker-compose up -d
```

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

## Dépannage

### Le serveur ne démarre pas

**Problème : "Port 50010 already in use"**
```bash
# Trouver quel processus utilise le port
lsof -i :50010          # Linux/Mac
netstat -ano | findstr :50010  # Windows

# Tuer le processus (remplacer PID par le numéro)
kill -9 <PID>           # Linux/Mac
taskkill /PID <PID> /F  # Windows

# Ou changer le port dans config.yml
server:
  port: 50011  # Changer le port
```

**Problème : "MongoDB connection refused"**
```bash
# Vérifier que MongoDB est lancé
docker ps | grep mongodb

# Ou relancer MongoDB
docker run -d --name mongodb -p 27017:27017 mongo:latest

# Vérifier la connexion
mongosh mongodb://localhost:27017
```
