# Smart Waste TCP Server - Documentation Complète

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Démarrage rapide](#démarrage-rapide)
3. [Configuration MongoDB et Initial Data](#configuration-mongodb-et-initial-data)
4. [Configuration](#configuration)
5. [Protocole TCP](#protocole-tcp)
6. [Schéma MongoDB](#schéma-mongodb)
7. [Déploiement Docker](#déploiement-docker)
8. [Tests](#tests)

---

## Vue d'ensemble

**Smart Waste TCP Server** est un serveur TCP multi-threadé conçu pour centraliser la gestion des données de déchets intelligents. Il reçoit les connexions des microcontrôleurs (ESP32/ESP8266) et traite les mesures de capteurs.

### Caractéristiques principales

- ✅ **Architecture multi-threadée** : Modèle thread-par-client avec limites configurables
- ✅ **Protocole texte** : Protocole TCP délimité par espaces avec codes d'erreur standardisés
- ✅ **MongoDB** : Schéma complet avec validation et indexation
- ✅ **Métriques en temps réel** : Suivi des connexions, requêtes, erreurs et transferts
- ✅ **Mises à jour automatiques** : Synchronisation des `lastMeasurement` dans la collection Poubelles

---

## Démarrage rapide

### Prérequis

- Java 17+
- MongoDB 4.4+
- Maven 3.9+

### 1. Configuration de MongoDB

**Option A : Docker (Recommandé)**
```bash
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:latest
```

**Option B : Installation locale**
Voir : https://docs.mongodb.com/manual/installation/

### 2. Configuration du serveur

Créez ou modifiez `config.yml` à la racine du projet :

```yaml
server:
  port: 50010
  maxConnections: 100
  socketTimeout: 30000  # en millisecondes (30 secondes)

mongodb:
  connectionString: "mongodb://localhost:27017"
  databaseName: "smartwaste_dev"
  environment: "dev"

logging:
  enableMetrics: true
  verbose: true
```

### 3. Configuration initiale de MongoDB

Connectez-vous à MongoDB :

```bash
mongosh mongodb://localhost:27017
use smartwaste_dev
```

**Créer les collections avec validation de schéma :**

```javascript
// Users
db.createCollection("Users", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["name", "mail", "role", "password"],
            properties: {
                name: { bsonType: "string" },
                password: { bsonType: "string" },
                mail: { bsonType: "string" },
                phone: { bsonType: "string" },
                role: { enum: ["user", "agent", "admin"] },
                levelOfTrust: { bsonType: "double" }
            }
        }
    }
});
db.Users.createIndex({ "mail": 1 }, { unique: true });

// MapPoints
db.createCollection("MapPoints", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["type", "location"],
            properties: {
                type: { bsonType: "string" },
                isSapue: { bsonType: "bool" },
                certaintyLevel: { bsonType: "double" },
                location: {
                    bsonType: "object",
                    required: ["type", "coordinates"],
                    properties: {
                        type: { enum: ["Point"] },
                        coordinates: {
                            bsonType: "array",
                            minItems: 2,
                            maxItems: 2,
                            items: { bsonType: "double" }
                        }
                    }
                },
                address: { bsonType: "string" },
                hardwareConfig: {
                    bsonType: "object",
                    properties: {
                        ipAddress: { bsonType: "string" },
                        modules: { bsonType: "array", items: { bsonType: "objectId" } },
                        sensors: { bsonType: "array", items: { bsonType: "string" } }
                    }
                },
                lastMeasurement: {
                    bsonType: "object",
                    properties: {
                        date: { bsonType: "date" },
                        measurement: { bsonType: "object" }
                    }
                },
                activeAlerts: {
                    bsonType: "object",
                    properties: {
                        hasIssue: { bsonType: "bool" },
                        issueType: { bsonType: "string" },
                        idReport: { bsonType: "objectId" }
                    }
                }
            }
        }
    }
});
db.MapPoints.createIndex({ "location": "2dsphere" });
db.MapPoints.createIndex({ "type": 1 });

// Modules
db.createCollection("Modules", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["name", "key"],
            properties: {
                name: { bsonType: "string" },
                key: { bsonType: "string" },
                uc: { bsonType: "string" },
                chipsets: { bsonType: "array", items: { bsonType: "object" } },
                ipAddress: { bsonType: "string" },
                firmwareVersion: { bsonType: "string" },
                commissioningDate: { bsonType: "date" },
                isEnabled: { bsonType: "bool" }
            }
        }
    }
});
db.Modules.createIndex({ "key": 1 }, { unique: true });

// Chipsets
db.createCollection("Chipsets", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["name", "caps"],
            properties: {
                name: { bsonType: "string" },
                description: { bsonType: "string" },
                links: { bsonType: "array", items: { bsonType: "string" } },
                caps: { bsonType: "array", items: { bsonType: "string" } },
                config: { bsonType: "object" },
                moduleID: { bsonType: "objectId" }
            }
        }
    }
});

// Releves
db.createCollection("Releves", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["id_Controller", "date", "measurement"],
            properties: {
                id_Controller: { bsonType: "objectId" },
                date: { bsonType: "date" },
                measurement: { bsonType: "object" }
            }
        }
    }
});

// Reports
db.createCollection("Reports", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["author", "status", "location", "type"],
            properties: {
                author: {
                    bsonType: "object",
                    required: ["idUser"],
                    properties: {
                        idUser: { bsonType: "objectId" },
                        name: { bsonType: "string" },
                        role: { bsonType: "string" }
                    }
                },
                cleaner: {
                    bsonType: "object",
                    properties: {
                        idUser: { bsonType: "objectId" },
                        name: { bsonType: "string" },
                        role: { bsonType: "string" }
                    }
                },
                status: { enum: ["Ouvert", "Affecte", "EnCours", "Resolu", "Rejete"] },
                mapPoint: { bsonType: "objectId" },
                type: { enum: ["DepotSauvage", "ProblemePoubelle"] },
                issueType: { bsonType: "string" },
                location: {
                    bsonType: "object",
                    required: ["type", "coordinates"],
                    properties: {
                        type: { enum: ["Point"] },
                        coordinates: { bsonType: "array", items: { bsonType: "double" } }
                    }
                },
                photo: {
                    bsonType: "object",
                    properties: {
                        initialPhoto: { bsonType: "string" },
                        coordinates: { bsonType: "array", items: { bsonType: "double" } },
                        finalPhoto: { bsonType: "string" }
                    }
                },
                history: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        properties: {
                            date: { bsonType: "date" },
                            status: { bsonType: "string" },
                            byUser: { bsonType: "objectId" }
                        }
                    }
                }
            }
        }
    }
});
db.Reports.createIndex({ "location": "2dsphere" });
db.Reports.createIndex({ "status": 1 });
db.Reports.createIndex({ "createdAt": -1 });

// Measurements
db.createCollection("Measurements", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["id_Controller", "date", "measurement"],
            properties: {
                id_Controller: { bsonType: "objectId" },
                date: { bsonType: "date" },
                measurement: { bsonType: "object" }
            }
        }
    }
});

// Tickets
db.createCollection("Tickets", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["subject", "status", "reporter"],
            properties: {
                subject: { bsonType: "string" },
                status: { enum: ["Open", "Pending", "Resolved", "Closed"] },
                reporter: {
                    bsonType: "object",
                    required: ["idUser"],
                    properties: {
                        idUser: { bsonType: "objectId" },
                        pseudo: { bsonType: "string" },
                        role: { bsonType: "string" }
                    }
                },
                conversation: {
                    bsonType: "array",
                    items: {
                        bsonType: "object",
                        required: ["content", "sender"],
                        properties: {
                            messageId: { bsonType: "objectId" },
                            sentAt: { bsonType: "date" },
                            content: { bsonType: "string" },
                            sender: {
                                bsonType: "object",
                                properties: {
                                    idUser: { bsonType: "objectId" },
                                    pseudo: { bsonType: "string" },
                                    role: { bsonType: "string" }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
db.Tickets.createIndex({ "status": 1, "updatedAt": -1 });
db.Tickets.createIndex({ "reporter.idUser": 1, "createdAt": -1 });
```

### 4. Données initiales d'exemple

Voici comment insérer les données de test correctement. **Exécutez ces commandes dans l'ordre** :

**Étape 1 : Créer un utilisateur (agent):**

```javascript
const userResult = db.Users.insertOne({
    name: "Jean Dupont",
    password: "$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/KKK", // hash bcrypt exemple
    mail: "jean.dupont@smartwaste.fr",
    phone: "+33612345678",
    role: "agent",
    levelOfTrust: 0.95
});
const userId = userResult.insertedId;
print("User créé avec ID: " + userId);
```

**Étape 2 : Créer un module (microcontrôleur):**

```javascript
const moduleResult = db.Modules.insertOne({
    name: "Capteur Besançon 1",
    key: "MC-001",
    uc: "ESP32",
    chipsets: [],
    ipAddress: "192.168.1.100",
    firmwareVersion: "1.2.3",
    commissioningDate: new Date("2024-01-15"),
    isEnabled: true
});
const moduleId = moduleResult.insertedId;
print("Module créé avec ID: " + moduleId);
```

**Étape 3 : Créer un chipset (capteur) :**

```javascript
const chipsetResult = db.Chipsets.insertOne({
    name: "BME280",
    description: "Capteur de température, humidité et pression",
    links: ["I2C"],
    caps: ["temperature", "humidity", "pressure"],
    config: {
        address: "0x77",
        oversampling: "16x",
        standbyTime: 0.5
    },
    moduleID: moduleId  // Utiliser l'ID du Module créé à l'étape 2
});
const chipsetId = chipsetResult.insertedId;
print("Chipset créé avec ID: " + chipsetId);
```

**Étape 4 : Mettre à jour le module avec le chipset:**

```javascript
db.Modules.updateOne(
    { _id: moduleId },
    { $set: { chipsets: [{ _id: chipsetId, type: "BME280", links: "I2C" }] } }
);
```

**Étape 5 : Créer un point de localisation (MapPoint - poubelle):**

```javascript
const mapPointResult = db.MapPoints.insertOne({
    type: "Poubelle intelligente",
    isSapue: true,
    certaintyLevel: 1.0,
    location: {
        type: "Point",
        coordinates: [6.0240, 47.2378]  // [longitude, latitude]
    },
    address: "1 Rue de la République, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.100",
        modules: [moduleId],  // Utiliser l'ID du Module
        sensors: ["BME280"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "BME280",
            temperature: 20.5,
            humidity: 65.0,
            pressure: 1013.25,
            fillLevel: 75.5,
            batteryLevel: 87
        }
    },
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idReport: null
    }
});
const mapPointId = mapPointResult.insertedId;
print("MapPoint créé avec ID: " + mapPointId);
```

**Étape 6 : Créer un relevé de mesures (Releves):**

```javascript
db.Releves.insertOne({
    id_Controller: moduleId,  // Utiliser l'ID du Module
    date: new Date(),
    measurement: {
        fillLevel: 75.5,
        weight: 45.2,
        temperature: 20.5,
        humidity: 65.0,
        airQuality: 120,
        batteryLevel: 87,
        confidence: 0.98,
        wasteType: "mixed"
    }
});
print("Relevé créé");
```

**Étape 7 : Créer un signalement (Report):**

```javascript
db.Reports.insertOne({
    author: {
        idUser: userId,  // Utiliser l'ID utilisateur créé à l'étape 1
        name: "Jean Dupont",
        role: "agent"
    },
    cleaner: null,
    status: "Ouvert",
    mapPoint: mapPointId,  // Utiliser l'ID du MapPoint créé à l'étape 5
    type: "ProblemePoubelle",
    issueType: "Poubelle pleine",
    location: {
        type: "Point",
        coordinates: [6.0240, 47.2378]
    },
    photo: {
        initialPhoto: "https://example.com/photo1.jpg",
        finalPhoto: null
    },
    history: [
        {
            date: new Date(),
            status: "Ouvert",
            byUser: userId  // Utiliser l'ID utilisateur
        }
    ]
});
print("Report créé");
```

**Étape 8 : Créer un ticket (Tickets):**

```javascript
db.Tickets.insertOne({
    subject: "Problème avec capteur MC-001",
    status: "Open",
    reporter: {
        idUser: userId,  // Utiliser l'ID utilisateur
        pseudo: "jdupont",
        role: "agent"
    },
    conversation: [
        {
            messageId: ObjectId(),
            sentAt: new Date(),
            content: "Le capteur MC-001 ne répond plus depuis 2 heures",
            sender: {
                idUser: userId,
                pseudo: "jdupont",
                role: "agent"
            }
        }
    ]
});
print("Ticket créé");
```

**Vérifier les données créées:**

```javascript
// Voir tous les users
db.Users.find().pretty();

// Voir tous les modules
db.Modules.find().pretty();

// Voir tous les mappoints
db.MapPoints.find().pretty();

// Vérifier l'index géospatial
db.MapPoints.getIndexes();
```

### 5. Compilation et démarrage

```bash
# Compiler le projet
mvn clean compile

# ---- Maven Run ----
# Run
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main"

# Or with custom config
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main" -Dexec.args="/path/to/config.yml"
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

# update le rapport avec l'image finale après intervention
IMAGE UPDATE <cleanerId> <reportId> <image_base64>

# envoyer une image au serveur d'analyse d'images
IMAGE ANALYSE <reference> <imageData>
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

## Schéma MongoDB

La base de données contient les collections suivantes avec validation de schéma :

### 📁 Users

Stocke les informations des utilisateurs.

```javascript
{
  _id: ObjectId,
  name: String,
  password: String (hash),
  mail: String (unique),
  phone: String,
  role: "user" | "agent" | "admin",
  levelOfTrust: Double
}
```

**Index:**
- `mail` (unique)

---

### 📁 MapPoints

Points de localisation des poubelles et signalements.

```javascript
{
  _id: ObjectId,
  type: String,
  isSapue: Boolean,
  certaintyLevel: Double,
  location: {
    type: "Point",
    coordinates: [longitude, latitude]  // GeoJSON
  },
  address: String,
  hardwareConfig: {
    ipAddress: String,
    modules: [ObjectId],              // Références aux Modules
    sensors: [String]
  },
  lastMeasurement: {
    date: Date,
    measurement: Object               // Mesures actuelles
  },
  activeAlerts: {
    hasIssue: Boolean,
    issueType: String,
    idReport: ObjectId
  }
}
```

**Index:**
- `location` (2dsphere pour géospatialité)
- `type`

---

### 📁 Modules

Microcontrôleurs (ESP32, Arduino, etc.)

```javascript
{
  _id: ObjectId,
  name: String,
  key: String (unique UUID),
  uc: String,                         // Type de µcontrôleur
  chipsets: [
    {
      _id: ObjectId,
      type: String,
      links: String
    }
  ],
  ipAddress: String,
  firmwareVersion: String,
  commissioningDate: Date,
  isEnabled: Boolean
}
```

**Index:**
- `key` (unique)

---

### 📁 Chipsets

Capteurs/composants embarqués dans les modules.

```javascript
{
  _id: ObjectId,
  name: String,
  description: String,
  links: [String],
  caps: [String],                     // Capacités/fonctionnalités
  config: Object,
  moduleID: ObjectId                  // Référence au Module
}
```

---

### 📁 Releves

Relevés de mesures (données historiques des capteurs).

```javascript
{
  _id: ObjectId,
  id_Controller: ObjectId,            // Référence au Module
  date: Date,
  measurement: {
    fillLevel: Double,
    weight: Double,
    temperature: Double,
    humidity: Double,
    airQuality: Double,
    batteryLevel: Double,
    confidence: Double,
    wasteType: String
  }
}
```

---

### 📁 Reports

Signalements et rapports de problèmes.

```javascript
{
  _id: ObjectId,
  author: {
    idUser: ObjectId,
    name: String,
    role: String
  },
  cleaner: {
    idUser: ObjectId,
    name: String,
    role: String
  },
  status: "Ouvert" | "Affecte" | "EnCours" | "Resolu" | "Rejete",
  mapPoint: ObjectId,
  type: "DepotSauvage" | "ProblemePoubelle",
  issueType: String,
  location: {
    type: "Point",
    coordinates: [longitude, latitude]
  },
  photo: {
    initialPhoto: String (URL),
    finalPhoto: String (URL)
  },
  history: [
    {
      date: Date,
      status: String,
      byUser: ObjectId
    }
  ]
}
```

**Index:**
- `location` (2dsphere)
- `status`
- `createdAt` (descendant)

---

### 📁 Measurements

Collection pour les mesures (alias pour Releves).

```javascript
{
  _id: ObjectId,
  id_Controller: ObjectId,
  date: Date,
  measurement: Object
}
```

---

### 📁 Tickets

Support/tickets utilisateurs.

```javascript
{
  _id: ObjectId,
  subject: String,
  status: "Open" | "Pending" | "Resolved" | "Closed",
  reporter: {
    idUser: ObjectId,
    pseudo: String,
    role: String
  },
  conversation: [
    {
      messageId: ObjectId,
      sentAt: Date,
      content: String,
      sender: {
        idUser: ObjectId,
        pseudo: String,
        role: String
      }
    }
  ]
}
```

**Index:**
- `status`, `updatedAt`
- `reporter.idUser`, `createdAt`

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

**Dernière mise à jour:** 6 janvier 2026  
**Version MongoDB:** 4.4+  
**Version Java:** 17+  
**Version Maven:** 3.9+
