package fr.smart_waste.sapue.dataaccess;

import fr.smart_waste.sapue.model.*;
import org.bson.types.ObjectId;
import java.util.List;

/**
 * Interface defining all data access operations for Smart Waste application.
 * Implementations can use direct MongoDB access or API calls.
 */
public interface DataDriver {

    // ========== MapPoints Operations (formerly Poubelles) ==========

    /**
     * Insert a new MapPoint
     * @param mapPoint MapPoints object to insert
     * @return ObjectId of inserted MapPoint, null if failed
     */
    ObjectId insertMapPoint(MapPoints mapPoint);

    /**
     * Find MapPoint by ID
     * @param id MapPoints ObjectId
     * @return MapPoints object or null if not found
     */
    MapPoints findMapPointById(ObjectId id);

    /**
     * Find MapPoints by type
     * @param type MapPoint type string
     * @return List of MapPoints
     */
    List<MapPoints> findMapPointsByType(String type);

    /**
     * Find MapPoints near a location
     * @param longitude Longitude
     * @param latitude Latitude
     * @param maxDistanceMeters Maximum distance in meters
     * @return List of nearby MapPoints
     */
    List<MapPoints> findMapPointsNear(double longitude, double latitude, double maxDistanceMeters);

    /**
     * Find MapPoint by module key
     * @param moduleKey Module key (UUID)
     * @return MapPoints object or null if not found
     */
    MapPoints findMapPointByModule(String moduleKey);

    /**
     * Update an existing MapPoint
     * @param mapPoint MapPoints object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateMapPoint(MapPoints mapPoint);

    /**
     * Add a new measurement to the history of a MapPoint
     * @param mapPointId MapPoints ObjectId
     * @param measurement LastMeasurement object to add
     * @return true if added successfully, false otherwise
     */
    boolean addMapPointMeasurement(ObjectId mapPointId, MapPoints.LastMeasurement measurement);

    /**
     * Delete a MapPoint by ID
     * @param id MapPoints ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteMapPoint(ObjectId id);

    /**
     * Get all MapPoints
     * @return List of all MapPoints
     */
    List<MapPoints> findAllMapPoints();

    /**
     * Find all MapPoints with active alerts
     * @return List of MapPoints with hasIssue = true
     */
    List<MapPoints> findMapPointsWithActiveAlerts();


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


    // ========== Reports Operations (formerly Signalements) ==========

    /**
     * Insert a new report
     * @param report Reports object to insert
     * @return ObjectId of inserted report, null if failed
     */
    ObjectId insertReport(Reports report);

    /**
     * Find report by ID
     * @param id Reports ObjectId
     * @return Reports object or null if not found
     */
    Reports findReportById(ObjectId id);

    /**
     * Find reports by status
     * @param status Status string
     * @return List of reports
     */
    List<Reports> findReportsByStatus(String status);

    /**
     * Find reports by MapPoint ID
     * @param mapPointId MapPoints ObjectId
     * @return List of reports
     */
    List<Reports> findReportsByMapPoint(ObjectId mapPointId);

    /**
     * Update an existing report
     * @param report Reports object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateReport(Reports report);

    /**
     * Delete a report by ID
     * @param id Reports ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteReport(ObjectId id);

    /**
     * Get all reports
     * @return List of all reports
     */
    List<Reports> findAllReports();


    // ========== Measurements Operations (formerly Releves) ==========

    /**
     * Insert a new measurement
     * @param measurement Measurements object to insert
     * @return ObjectId of inserted measurement, null if failed
     */
    ObjectId insertMeasurement(Measurements measurement);

    /**
     * Find measurement by ID
     * @param id Measurements ObjectId
     * @return Measurements object or null if not found
     */
    Measurements findMeasurementById(ObjectId id);

    /**
     * Find all measurements for a specific controller (Module)
     * @param idController Modules ObjectId
     * @return List of measurements
     */
    List<Measurements> findMeasurementsByController(ObjectId idController);

    /**
     * Find measurements for a specific controller within a date range
     * @param idController Modules ObjectId
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of measurements within the date range
     */
    List<Measurements> findMeasurementsByModuleId(ObjectId idController, java.util.Date startDate, java.util.Date endDate);

    /**
     * Update an existing measurement
     * @param measurement Measurements object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateMeasurement(Measurements measurement);

    /**
     * Delete a measurement by ID
     * @param id Measurements ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteMeasurement(ObjectId id);

    /**
     * Get all measurements
     * @return List of all measurements
     */
    List<Measurements> findAllMeasurements();


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

    List<AnalyseMedia> findAllAnalyseMedias();

    /**
     * Get the hexa icon by waste bin type
     * @param wasteBinType Waste bin type
     * @return Hexa icon
     */
    String getHexaIconByWasteBinType(String wasteBinType);

    // ========== Connection Management ==========

    /**
     * Close database connection and cleanup resources
     */
    void close();

}