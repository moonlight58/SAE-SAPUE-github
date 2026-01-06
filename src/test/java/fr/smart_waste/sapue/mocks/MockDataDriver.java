package fr.smart_waste.sapue.mocks;

import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.*;
import org.bson.types.ObjectId;

import java.util.*;

public class MockDataDriver implements DataDriver {
    
    public Map<ObjectId, Poubelles> poubelles = new HashMap<>();
    public Map<String, Modules> modules = new HashMap<>();
    public Map<ObjectId, Chipsets> chipsets = new HashMap<>();
    public Releves lastInsertedReleve;
    
    public boolean shouldFailNextInsert = false;
    public boolean available = true;
    public boolean fallbackAvailable = true;
    public boolean usedFallback = false;

    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public void setFallbackAvailable(boolean fallbackAvailable) {
        this.fallbackAvailable = fallbackAvailable;
    }

    public void addModule(Modules module) {
        modules.put(module.getKey(), module);
    }
    
    public void addChipset(Chipsets chipset) {
        chipsets.put(chipset.getId(), chipset);
    }
    
    public void addPoubelle(Poubelles p) {
        poubelles.put(p.getId(), p);
    }

    @Override
    public ObjectId insertPoubelle(Poubelles poubelle) { return null; }

    @Override
    public Poubelles findPoubelleById(ObjectId id) { return poubelles.get(id); }

    @Override
    public Poubelles findPoubelleByModule(String moduleKey) {
        return poubelles.values().stream()
            .filter(p -> p.getHardwareConfig() != null && 
                         p.getHardwareConfig().getMicrocontroller() != null &&
                         p.getHardwareConfig().getMicrocontroller().contains(moduleKey))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean updatePoubelle(Poubelles poubelle) { return true; }

    @Override
    public boolean updateLastMeasurement(ObjectId id, Poubelles.LastMeasurement lastMeasurement) { return true; }

    @Override
    public boolean updateActiveAlerts(ObjectId id, Poubelles.ActiveAlerts activeAlerts) { return true; }

    @Override
    public boolean deletePoubelle(ObjectId id) { return true; }

    @Override
    public List<Poubelles> findAllPoubelles() { return new ArrayList<>(poubelles.values()); }

    @Override
    public List<Poubelles> findPoubellesWithActiveAlerts() { return new ArrayList<>(); }

    @Override
    public ObjectId insertModule(Modules module) { return null; }

    @Override
    public Modules findModuleById(ObjectId id) { return null; }

    @Override
    public Modules findModuleByKey(String key) {
        return modules.get(key);
    }

    @Override
    public boolean updateModule(Modules module) { return true; }

    @Override
    public boolean deleteModule(ObjectId id) { return true; }

    @Override
    public List<Modules> findAllModules() { return new ArrayList<>(); }

    @Override
    public ObjectId insertChipset(Chipsets chipset) { return new ObjectId(); }

    @Override
    public Chipsets findChipsetById(ObjectId id) { 
        return chipsets.get(id); 
    }

    @Override
    public List<Chipsets> findChipsetsByModuleId(ObjectId moduleId) { 
        return chipsets.values().stream()
            .filter(c -> c.getModuleID() != null && c.getModuleID().equals(moduleId))
            .toList();
    }

    @Override
    public boolean updateChipset(Chipsets chipset) { return true; }

    @Override
    public boolean deleteChipset(ObjectId id) { return true; }

    @Override
    public List<Chipsets> findAllChipsets() { return new ArrayList<>(chipsets.values()); }

    @Override
    public ObjectId insertSignalements(Signalements signalement) { return new ObjectId(); }

    @Override
    public Signalements findSignalementsById(ObjectId id) { return null; }

    @Override
    public boolean updateSignalements(Signalements signalement) { return true; }

    @Override
    public boolean deleteSignalements(ObjectId id) { return true; }

    @Override
    public List<Signalements> findAllSignalements() { return new ArrayList<>(); }

    @Override
    public ObjectId insertReleve(Releves releves) {
        if (shouldFailNextInsert) return null;
        if (!available) {
            if (fallbackAvailable) {
                lastInsertedReleve = releves;
                usedFallback = true;
                return new ObjectId();
            }
            return null;
        }
        lastInsertedReleve = releves;
        return new ObjectId();
    }

    @Override
    public Releves findReleveById(ObjectId id) { return null; }

    @Override
    public List<Releves> findRelevesByPoubelle(ObjectId idPoubelle) { return new ArrayList<>(); }

    @Override
    public boolean updateReleve(Releves releves) { return true; }

    @Override
    public boolean deleteReleve(ObjectId id) { return true; }

    @Override
    public List<Releves> findAllReleves() { return new ArrayList<>(); }

    @Override
    public ObjectId insertAnalyseMedia(AnalyseMedia analyseMedia) { return new ObjectId(); }

    @Override
    public AnalyseMedia findAnalyseMediaById(ObjectId id) { return null; }

    @Override
    public boolean updateAnalyseMedia(AnalyseMedia analyseMedia) { return true; }

    @Override
    public boolean deleteAnalyseMedia(ObjectId id) { return true; }

    @Override
    public List<AnalyseMedia> findAllAnalyseMedias() { return new ArrayList<>(); }

    @Override
    public void close() {}
}
