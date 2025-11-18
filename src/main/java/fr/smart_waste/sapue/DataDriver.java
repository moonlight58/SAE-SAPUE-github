package fr.smart_waste.sapue

import com.smartwaste.server.model.*;
import org.bson.types.ObjectId;
import java.util.List;

/**
 * Interface defining all data access operations for Smart Waste application.
 * Implementations can use direct MongoDB access or API calls.
 */
public interface DataDriver {
    
    // ========== User Operations ==========
    
    /**
     * Insert a new user
     * @param user User object to insert
     * @return ObjectId of inserted user, null if failed
     */
    ObjectId insertUser(User user);
    
    /**
     * Find user by ID
     * @param id User ObjectId
     * @return User object or null if not found
     */
    User findUserById(ObjectId id);
    
    /**
     * Find user by pseudo
     * @param pseudo User pseudo
     * @return User object or null if not found
     */
    User findUserByPseudo(String pseudo);
    
    /**
     * Update an existing user
     * @param user User object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateUser(User user);
    
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
    List<User> findAllUsers();
    
    
    // ========== Bin Operations ==========
    
    /**
     * Insert a new bin
     * @param bin Bin object to insert
     * @return ObjectId of inserted bin, null if failed
     */
    ObjectId insertBin(Bin bin);
    
    /**
     * Find bin by ID
     * @param id Bin ObjectId
     * @return Bin object or null if not found
     */
    Bin findBinById(ObjectId id);
    
    /**
     * Update an existing bin
     * @param bin Bin object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateBin(Bin bin);
    
    /**
     * Delete a bin by ID
     * @param id Bin ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteBin(ObjectId id);
    
    /**
     * Get all bins
     * @return List of all bins
     */
    List<Bin> findAllBins();
    
    
    // ========== Microcontroller Operations ==========
    
    /**
     * Insert a new microcontroller
     * @param microcontroller Microcontroller object to insert
     * @return ObjectId of inserted microcontroller, null if failed
     */
    ObjectId insertMicrocontroller(Microcontroller microcontroller);
    
    /**
     * Find microcontroller by ID
     * @param id Microcontroller ObjectId
     * @return Microcontroller object or null if not found
     */
    Microcontroller findMicrocontrollerById(ObjectId id);
    
    /**
     * Find microcontroller by reference
     * @param reference Microcontroller reference string
     * @return Microcontroller object or null if not found
     */
    Microcontroller findMicrocontrollerByReference(String reference);
    
    /**
     * Update an existing microcontroller
     * @param microcontroller Microcontroller object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateMicrocontroller(Microcontroller microcontroller);
    
    /**
     * Delete a microcontroller by ID
     * @param id Microcontroller ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteMicrocontroller(ObjectId id);
    
    /**
     * Get all microcontrollers
     * @return List of all microcontrollers
     */
    List<Microcontroller> findAllMicrocontrollers();
    
    
    // ========== Signalement Operations ==========
    
    /**
     * Insert a new signalement
     * @param signalement Signalement object to insert
     * @return ObjectId of inserted signalement, null if failed
     */
    ObjectId insertSignalement(Signalement signalement);
    
    /**
     * Find signalement by ID
     * @param id Signalement ObjectId
     * @return Signalement object or null if not found
     */
    Signalement findSignalementById(ObjectId id);
    
    /**
     * Update an existing signalement
     * @param signalement Signalement object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateSignalement(Signalement signalement);
    
    /**
     * Delete a signalement by ID
     * @param id Signalement ObjectId
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteSignalement(ObjectId id);
    
    /**
     * Get all signalements
     * @return List of all signalements
     */
    List<Signalement> findAllSignalements();
    
    
    // ========== Releve Operations ==========
    
    /**
     * Insert a new releve (sensor reading)
     * @param releve Releve object to insert
     * @return ObjectId of inserted releve, null if failed
     */
    ObjectId insertReleve(Releve releve);
    
    /**
     * Find releve by ID
     * @param id Releve ObjectId
     * @return Releve object or null if not found
     */
    Releve findReleveById(ObjectId id);
    
    /**
     * Find all releves for a specific microcontroller
     * @param idControlleur Microcontroller ObjectId
     * @return List of releves
     */
    List<Releve> findRelevesByControlleur(ObjectId idControlleur);
    
    /**
     * Update an existing releve
     * @param releve Releve object with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean updateReleve(Releve releve);
    
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
    List<Releve> findAllReleves();
    
    
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
