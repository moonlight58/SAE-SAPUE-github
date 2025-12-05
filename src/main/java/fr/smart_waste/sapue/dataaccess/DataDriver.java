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
     * Find poubelle by microcontroller reference
     * @param mcReference Microcontroller reference (e.g., "MC-001")
     * @return Poubelles object or null if not found
     */
    Poubelles findPoubelleByMicrocontroller(String mcReference);

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


    // ========== Microcontrolleur Operations ==========

    /**
     * Insert a new microcontrolleur
     * @param microcontrolleur Microcontrolleur object to insert
     * @return ObjectId of inserted microcontrolleur, null if failed
     */
    ObjectId insertMicrocontrolleur(Microcontrolleur microcontrolleur);

    /**
     * Find microcontrolleur by ID
     * @param id Microcontrolleur ObjectId
     * @return Microcontrolleur object or null if not found
     */
    Microcontrolleur findMicrocontrolleurById(ObjectId id);

    /**
     * Find microcontrolleur by reference
     * @param reference Microcontrolleur reference string
     * @return Microcontrolleur object or null if not found
     */
    Microcontrolleur findMicrocontrolleurByReference(String reference);

    /**
     * Update an existing microcontrolleur
     * @param microcontrolleur Microcontrolleur object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateMicrocontrolleur(Microcontrolleur microcontrolleur);

    /**
     * Delete a microcontrolleur by ID
     * @param id Microcontrolleur ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteMicrocontrolleur(ObjectId id);

    /**
     * Get all microcontrolleurs
     * @return List of all microcontrolleurs
     */
    List<Microcontrolleur> findAllMicrocontrolleurs();


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
     * Find all releves for a specific microcontrolleur
     * @param idControlleur Microcontrolleur ObjectId
     * @return List of releves
     */
    List<Releves> findRelevesByControlleur(ObjectId idControlleur);

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