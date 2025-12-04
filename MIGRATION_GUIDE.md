# Guide de Migration - Nouvelle Structure Poubelles

## Vue d'ensemble

La nouvelle structure remplace MapPoint par une collection **Poubelles** plus complète, tout en conservant les collections Microcontrolleur, Releves, Signalements et AnalyseMedia.

## Changements principaux

1. **Collection "poubelles"** remplace "mapPoints"
2. Ajout de `lastMeasurement` pour avoir la dernière mesure directement accessible
3. Ajout de `activeAlerts` pour gérer les alertes en temps réel
4. Lien vers Microcontrolleur via références (ex: ["MC-001"])

---

## Étape 1 : Créer les données initiales

### 1.1 Créer un Microcontrolleur

```javascript
// Connexion à MongoDB
mongosh mongodb://localhost:27017

// Utiliser la base de données
use smartwaste_dev

// Créer un microcontrôleur
db.microcontrolleurs.insertOne({
    reference: "MC-001",
    mapPoint: null,  // Plus utilisé avec nouvelle structure
    ipAddress: "192.168.1.100",
    configSensor: {
        sensorType: "BME280",
        enabled: true,
        samplingInterval: 300,
        parameters: {
            tempOffset: 0.5,
            pressureUnit: "hPa"
        }
    }
})

// Copier l'_id retourné (ex: ObjectId("674f5e8a1234567890abcdef"))
```

### 1.2 Créer une Poubelle liée au Microcontrolleur

```javascript
// Créer une poubelle avec le microcontrôleur MC-001
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 1.0,
    location: {
        type: "Point",
        coordinates: [6.0240, 47.2378]  // [longitude, latitude] - Besançon
    },
    adress: "1 Rue de la République, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.100",
        microcontroller: ["MC-001"],  // Référence au MC
        sensors: ["BME280"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "BME280",
            temperature: 20.5,
            humidity: 65.0,
            pressure: 1013.25
        }
    },
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idSignalement: null
    }
})
```

### 1.3 Créer plusieurs Poubelles (exemple complet)

```javascript
// Poubelle 1 - Sans alerte
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 1.0,
    location: {
        type: "Point",
        coordinates: [6.0245, 47.2385]
    },
    adress: "Place de la Révolution, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.101",
        microcontroller: ["MC-002"],
        sensors: ["BME280", "HX711"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "HX711",
            weight: 45.5,
            fillLevel: 75
        }
    },
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idSignalement: null
    }
})

// Poubelle 2 - Avec alerte (pleine)
db.poubelles.insertOne({
    type: "Poubelle",
    isSapue: true,
    certaintyLevel: 0.95,
    location: {
        type: "Point",
        coordinates: [6.0250, 47.2390]
    },
    adress: "Avenue Foch, 25000 Besançon",
    hardwareConfig: {
        ipAddress: "192.168.1.102",
        microcontroller: ["MC-003"],
        sensors: ["HCSR04", "MQ135"]
    },
    lastMeasurement: {
        date: new Date(),
        measurement: {
            sensorType: "HCSR04",
            distance: 10,
            fillLevel: 92
        }
    },
    activeAlerts: {
        hasIssue: true,
        issueType: "Plein",
        idSignalement: null  // Sera ajouté quand un signalement est créé
    }
})

// Bac de recyclage - Pas SAPUE
db.poubelles.insertOne({
    type: "Bac",
    isSapue: false,
    certaintyLevel: 0.8,
    location: {
        type: "Point",
        coordinates: [6.0255, 47.2395]
    },
    adress: "Rue Pasteur, 25000 Besançon",
    hardwareConfig: {
        ipAddress: null,
        microcontroller: [],
        sensors: []
    },
    lastMeasurement: null,
    activeAlerts: {
        hasIssue: false,
        issueType: null,
        idSignalement: null
    }
})
```

---

## Étape 2 : Créer les Microcontrolleurs correspondants

```javascript
// MC-002
db.microcontrolleurs.insertOne({
    reference: "MC-002",
    mapPoint: null,
    ipAddress: "192.168.1.101",
    configSensor: {
        sensorType: "HX711",
        enabled: true,
        samplingInterval: 600,
        parameters: {
            calibrationFactor: 2000
        }
    }
})

// MC-003
db.microcontrolleurs.insertOne({
    reference: "MC-003",
    mapPoint: null,
    ipAddress: "192.168.1.102",
    configSensor: {
        sensorType: "HCSR04",
        enabled: true,
        samplingInterval: 300,
        parameters: {
            triggerPin: 5,
            echoPin: 18
        }
    }
})
```

---

## Étape 3 : Créer un index géospatial (IMPORTANT)

```javascript
// Créer un index 2dsphere pour les requêtes géospatiales
db.poubelles.createIndex({ "location": "2dsphere" })
```

---

## Étape 4 : Tester l'intégration avec le serveur TCP

### 4.1 Démarrer le serveur

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="fr.smart_waste.sapue.Main"
```

### 4.2 Se connecter avec telnet

```bash
telnet localhost 8888
```

### 4.3 Tester les commandes

```bash
# 1. S'enregistrer
REGISTER MC-001 192.168.1.100
# Réponse attendue: OK

# 2. Envoyer des données
DATA MC-001 BME280 temperature:22.5 humidity:65.0 pressure:1013.25
# Réponse attendue: OK
# ⚠️ Ceci va mettre à jour automatiquement lastMeasurement dans Poubelle !

# 3. Vérifier la mise à jour dans MongoDB
```

### 4.4 Vérifier dans MongoDB

```javascript
// Vérifier que lastMeasurement a été mis à jour
db.poubelles.findOne({ "hardwareConfig.microcontroller": "MC-001" })

// Résultat attendu:
// {
//   ...
//   lastMeasurement: {
//     date: ISODate("2025-12-04T..."),
//     measurement: {
//       sensorType: "BME280",
//       temperature: 22.5,
//       humidity: 65.0,
//       pressure: 1013.25
//     }
//   }
// }
```

---

## Étape 5 : Simuler une alerte automatique

### 5.1 Créer un Signalement

```javascript
// Créer un signalement (à partir de l'app mobile par exemple)
db.signalements.insertOne({
    type: "dépôt sauvage",
    user: ObjectId("674f5e8a1234567890abcdef"),  // ID utilisateur
    position: [6.0250, 47.2390],
    imagePath: "/uploads/signalement_001.jpg"
})

// Copier l'_id du signalement
```

### 5.2 Créer l'alerte dans la Poubelle

```javascript
// Trouver la poubelle concernée
var poubelleId = db.poubelles.findOne({ 
    location: { 
        $near: {
            $geometry: { type: "Point", coordinates: [6.0250, 47.2390] },
            $maxDistance: 50  // 50 mètres
        }
    }
})._id

// Mettre à jour avec l'alerte
db.poubelles.updateOne(
    { _id: poubelleId },
    { 
        $set: {
            "activeAlerts.hasIssue": true,
            "activeAlerts.issueType": "Dépôt sauvage",
            "activeAlerts.idSignalement": ObjectId("ID_DU_SIGNALEMENT")
        }
    }
)
```

---

## Requêtes utiles

### Trouver toutes les poubelles avec alertes actives

```javascript
db.poubelles.find({ "activeAlerts.hasIssue": true })
```

### Trouver les poubelles près d'un point

```javascript
db.poubelles.find({
    location: {
        $near: {
            $geometry: { type: "Point", coordinates: [6.0240, 47.2378] },
            $maxDistance: 1000  // 1 km
        }
    }
})
```

### Trouver les poubelles d'un microcontrôleur

```javascript
db.poubelles.findOne({ "hardwareConfig.microcontroller": "MC-001" })
```

### Trouver les poubelles sans microcontrôleur (détectées par IA)

```javascript
db.poubelles.find({ 
    "hardwareConfig.microcontroller": { $size: 0 },
    certaintyLevel: { $lt: 1.0 }
})
```

---

## Points clés à retenir

1. **lastMeasurement est mis à jour automatiquement** quand le serveur TCP reçoit des données via la commande DATA
2. **activeAlerts doit être mis à jour** quand un signalement est créé (à implémenter dans l'API Node)
3. **Microcontrolleur et Poubelle sont liés** via la référence (ex: "MC-001")
4. **L'index géospatial est crucial** pour les requêtes de proximité
5. **certaintyLevel** indique la confiance de détection (1.0 = confirmée, < 1.0 = détectée par IA)

---

## Migration depuis ancienne structure

Si vous avez des données dans MapPoints :

```javascript
// Script de migration (à adapter selon vos données)
db.mapPoints.find().forEach(function(mapPoint) {
    
    // Trouver le microcontrolleur lié
    var mc = db.microcontrolleurs.findOne({ mapPoint: mapPoint._id });
    
    if (mc) {
        // Créer la poubelle correspondante
        db.poubelles.insertOne({
            type: mapPoint.type,
            isSapue: mapPoint.isSAPUE,
            certaintyLevel: 1.0,
            location: {
                type: "Point",
                coordinates: mapPoint.position
            },
            adress: "À renseigner",
            hardwareConfig: {
                ipAddress: mc.ipAddress,
                microcontroller: [mc.reference],
                sensors: [mc.configSensor.sensorType]
            },
            lastMeasurement: null,
            activeAlerts: {
                hasIssue: false,
                issueType: null,
                idSignalement: null
            }
        });
    }
});
```

---

## Prochaines étapes

1. ✅ Créer les données de test dans MongoDB
2. ✅ Tester l'enregistrement et l'envoi de données
3. ⏳ Implémenter la mise à jour automatique des alertes dans l'API Node
4. ⏳ Créer l'API REST pour les opérations CRUD sur Poubelles
5. ⏳ Intégrer avec le front-end Vue.js