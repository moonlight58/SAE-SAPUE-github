package fr.smart_waste.sapue.dataaccess;

import fr.smart_waste.sapue.model.*;
import org.bson.types.ObjectId;
import java.util.List;

/**
 * Interface defining all data access operations for Smart Waste application.
 * Implementations can use direct MongoDB access or API calls.
 */
public interface DataDriver {

    // ========== Poubelles Operations ==========

    /**
     * Insert a new poubelle
     * @param poubelle Poubelles object to insert
     * @return ObjectId of inserted poubelle, null if failed
     */
    ObjectId insertPoubelle(Poubelles poubelle);

    /**
     * Find poubelle by ID
     * @param id Poubelles ObjectId
     * @return Poubelles object or null if not found
     */
    Poubelles findPoubelleById(ObjectId id);

    /**
     * Find poubelle by module key
     * @param moduleKey Module key (UUID)
     * @return Poubelles object or null if not found
     */
    Poubelles findPoubelleByModule(String moduleKey);

    /**
     * Update an existing poubelle
     * @param poubelle Poubelles object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updatePoubelle(Poubelles poubelle);

    /**
     * Update last measurement for a poubelle
     * @param id Poubelles ObjectId
     * @param lastMeasurement LastMeasurement object
     * @return true if updated successfully, false otherwise
     */
    boolean updateLastMeasurement(ObjectId id, Poubelles.LastMeasurement lastMeasurement);

    /**
     * Update active alerts for a poubelle
     * @param id Poubelles ObjectId
     * @param activeAlerts ActiveAlerts object
     * @return true if updated successfully, false otherwise
     */
    boolean updateActiveAlerts(ObjectId id, Poubelles.ActiveAlerts activeAlerts);

    /**
     * Delete a poubelle by ID
     * @param id Poubelles ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deletePoubelle(ObjectId id);

    /**
     * Get all poubelles
     * @return List of all poubelles
     */
    List<Poubelles> findAllPoubelles();

    /**
     * Find all poubelles with active alerts
     * @return List of poubelles with hasIssue = true
     */
    List<Poubelles> findPoubellesWithActiveAlerts();


    // ========== Module Operations ==========

    /**
     * Insert a new module
     * @param module Modules object to insert
     * @return ObjectId of inserted module, null if failed
     */
    ObjectId insertModule(Modules module);

    /**
     * Find module by ID
     * @param id Module ObjectId
     * @return Modules object or null if not found
     */
    Modules findModuleById(ObjectId id);

    /**
     * Find module by key (UUID)
     * @param key Module key string (UUID)
     * @return Modules object or null if not found
     */
    Modules findModuleByKey(String key);

    /**
     * Update an existing module
     * @param module Modules object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateModule(Modules module);

    /**
     * Delete a module by ID
     * @param id Module ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteModule(ObjectId id);

    /**
     * Get all modules
     * @return List of all modules
     */
    List<Modules> findAllModules();


    // ========== Chipsets Operations ==========

    /**
     * Insert a new chipset
     * @param chipset Chipsets object to insert
     * @return ObjectId of inserted chipset, null if failed
     */
    ObjectId insertChipset(Chipsets chipset);

    /**
     * Find chipset by ID
     * @param id Chipset ObjectId
     * @return Chipsets object or null if not found
     */
    Chipsets findChipsetById(ObjectId id);

    /**
     * Find all chipsets for a specific module
     * @param moduleId Module ObjectId
     * @return List of chipsets
     */
    List<Chipsets> findChipsetsByModuleId(ObjectId moduleId);

    /**
     * Update an existing chipset
     * @param chipset Chipsets object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateChipset(Chipsets chipset);

    /**
     * Delete a chipset by ID
     * @param id Chipset ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteChipset(ObjectId id);

    /**
     * Get all chipsets
     * @return List of all chipsets
     */
    List<Chipsets> findAllChipsets();

    // ==========================================
    // Reports operations (formerly Signalements)
    // ==========================================

    ObjectId insertReport(Reports report);
    Reports findReportById(ObjectId id);
    List<Reports> findReportsByStatus(String status);
    List<Reports> findReportsByMapPoint(ObjectId mapPointId);
    boolean updateReport(Reports report);
    boolean deleteReport(ObjectId id);
    List<Reports> findAllReports();

    // ==========================================
    // MapPoints operations (formerly Poubelles)
    // ==========================================

    ObjectId insertMapPoint(MapPoints mapPoint);
    MapPoints findMapPointById(ObjectId id);
    List<MapPoints> findMapPointsByType(String type);
    List<MapPoints> findMapPointsNear(double longitude, double latitude, double maxDistanceMeters);
    MapPoints findMapPointByModule(String moduleKey);
    boolean updateMapPointLastMeasurement(ObjectId mapPointId, MapPoints.LastMeasurement lastMeasurement);
    boolean updateMapPoint(MapPoints mapPoint);
    boolean deleteMapPoint(ObjectId id);
    List<MapPoints> findAllMapPoints();
    List<MapPoints> findMapPointsWithActiveAlerts();

    // ========== Users Operations ==========

    /**
     * Find user by ID
     * @param id User ObjectId
     * @return Users object or null if not found
     */
    Users findUserById(ObjectId id);

    /**
     * Find user by email
     * @param mail User email
     * @return Users object or null if not found
     */
    Users findUserByMail(String mail);

    /**
     * Insert a new user
     * @param user Users object to insert
     * @return ObjectId of inserted user, null if failed
     */
    ObjectId insertUser(Users user);

    /**
     * Update an existing user
     * @param user Users object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateUser(Users user);

    /**
     * Delete a user by ID
     * @param id User ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteUser(ObjectId id);

    /**
     * Get all users
     * @return List of all users
     */
    List<Users> findAllUsers();

    // ========== Signalement Operations ==========

    /**
     * Insert a new signalement
     * @param signalement Signalement object to insert
     * @return ObjectId of inserted signalement, null if failed
     */
    ObjectId insertSignalements(Signalements signalement);

    /**
     * Find signalement by ID
     * @param id Signalements ObjectId
     * @return Signalements object or null if not found
     */
    Signalements findSignalementsById(ObjectId id);

    /**
     * Update an existing signalement
     * @param signalement Signalements object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateSignalements(Signalements signalement);

    /**
     * Delete a signalement by ID
     * @param id Signalements ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteSignalements(ObjectId id);

    /**
     * Get all signalements
     * @return List of all signalements
     */
    List<Signalements> findAllSignalements();


    // ========== Releve Operations ==========

    /**
     * Insert a new releve (sensor reading)
     * @param releves Releve object to insert
     * @return ObjectId of inserted releve, null if failed
     */
    ObjectId insertReleve(Releves releves);

    /**
     * Find releve by ID
     * @param id Releve ObjectId
     * @return Releve object or null if not found
     */
    Releves findReleveById(ObjectId id);

    /**
     * Find all releves for a specific poubelle (changed from idControlleur to idPoubelle)
     * @param idPoubelle Poubelle ObjectId
     * @return List of releves
     */
    List<Releves> findRelevesByPoubelle(ObjectId idPoubelle);

    /**
     * Update an existing releve
     * @param releves Releve object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateReleve(Releves releves);

    /**
     * Delete a releve by ID
     * @param id Releve ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteReleve(ObjectId id);

    /**
     * Get all releves
     * @return List of all releves
     */
    List<Releves> findAllReleves();


    // ========== AnalyseMedia Operations ==========

    /**
     * Insert a new analyse media
     * @param analyseMedia AnalyseMedia object to insert
     * @return ObjectId of inserted analyse media, null if failed
     */
    ObjectId insertAnalyseMedia(AnalyseMedia analyseMedia);

    /**
     * Find analyse media by ID
     * @param id AnalyseMedia ObjectId
     * @return AnalyseMedia object or null if not found
     */
    AnalyseMedia findAnalyseMediaById(ObjectId id);

    /**
     * Update an existing analyse media
     * @param analyseMedia AnalyseMedia object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateAnalyseMedia(AnalyseMedia analyseMedia);

    /**
     * Delete an analyse media by ID
     * @param id AnalyseMedia ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteAnalyseMedia(ObjectId id);

    /**
     * Get all analyse medias
     * @return List of all analyse medias
     */
    List<AnalyseMedia> findAllAnalyseMedias();

    // ========== Connection Management ==========

    /**
     * Close database connection and cleanup resources
     */
    void close();

}