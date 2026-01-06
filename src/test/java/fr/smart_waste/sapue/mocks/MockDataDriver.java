package fr.smart_waste.sapue.mocks;

import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.*;
import org.bson.types.ObjectId;

import java.util.*;

public class MockDataDriver implements DataDriver {
    
    public Map<ObjectId, Poubelles> poubelles = new HashMap<>();
    public Map<String, Microcontrolleur> mcs = new HashMap<>();
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

    public void addMicrocontrolleur(Microcontrolleur mc) {
        mcs.put(mc.getReference(), mc);
    }
    
    public void addPoubelle(Poubelles p) {
        poubelles.put(p.getId(), p);
    }

    @Override
    public ObjectId insertPoubelle(Poubelles poubelle) { return null; }

    @Override
    public Poubelles findPoubelleById(ObjectId id) { return poubelles.get(id); }

    @Override
    public Poubelles findPoubelleByMicrocontroller(String mcReference) {
        return poubelles.values().stream()
            .filter(p -> p.getHardwareConfig() != null && 
                         p.getHardwareConfig().getMicrocontroller() != null &&
                         p.getHardwareConfig().getMicrocontroller().contains(mcReference))
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
    public ObjectId insertMicrocontrolleur(Microcontrolleur microcontrolleur) { return null; }

    @Override
    public Microcontrolleur findMicrocontrolleurById(ObjectId id) { return null; }

    @Override
    public Microcontrolleur findMicrocontrolleurByReference(String reference) {
        return mcs.get(reference);
    }

    @Override
    public boolean updateMicrocontrolleur(Microcontrolleur microcontrolleur) { return true; }

    @Override
    public boolean deleteMicrocontrolleur(ObjectId id) { return true; }

    @Override
    public List<Microcontrolleur> findAllMicrocontrolleurs() { return new ArrayList<>(); }

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
