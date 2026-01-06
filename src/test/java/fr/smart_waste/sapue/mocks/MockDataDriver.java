package fr.smart_waste.sapue.mocks;

import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.*;
import org.bson.types.ObjectId;

import java.util.*;

public class MockDataDriver implements DataDriver {
    
    public Map<ObjectId, Poubelles> poubelles = new HashMap<>();
    public Map<String, Modules> modules = new HashMap<>();
    public Map<ObjectId, Chipsets> chipsets = new HashMap<>();
    public Map<ObjectId, Reports> reports = new HashMap<>();
    public Map<ObjectId, MapPoints> mapPoints = new HashMap<>();
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

    // ==========================================
    // Reports Operations (Mock)
    // ==========================================

    public void addReport(Reports report) {
        reports.put(report.getId(), report);
    }

    @Override
    public ObjectId insertReport(Reports report) {
        if (report == null) return null;
        if (report.getId() == null) report.setId(new ObjectId());
        reports.put(report.getId(), report);
        return report.getId();
    }

    @Override
    public Reports findReportById(ObjectId id) {
        return reports.get(id);
    }

    @Override
    public List<Reports> findReportsByStatus(String status) {
        List<Reports> result = new ArrayList<>();
        for (Reports r : reports.values()) {
            if (status.equals(r.getStatus())) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public List<Reports> findReportsByMapPoint(ObjectId mapPointId) {
        List<Reports> result = new ArrayList<>();
        for (Reports r : reports.values()) {
            if (mapPointId != null && mapPointId.equals(r.getMapPoint())) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public boolean updateReport(Reports report) {
        if (report == null || report.getId() == null) return false;
        reports.put(report.getId(), report);
        return true;
    }

    @Override
    public boolean deleteReport(ObjectId id) {
        return reports.remove(id) != null;
    }

    @Override
    public List<Reports> findAllReports() {
        return new ArrayList<>(reports.values());
    }

    // ==========================================
    // MapPoints Operations (Mock)
    // ==========================================

    public void addMapPoint(MapPoints mapPoint) {
        mapPoints.put(mapPoint.getId(), mapPoint);
    }

    @Override
    public ObjectId insertMapPoint(MapPoints mapPoint) {
        if (mapPoint == null) return null;
        if (mapPoint.getId() == null) mapPoint.setId(new ObjectId());
        mapPoints.put(mapPoint.getId(), mapPoint);
        return mapPoint.getId();
    }

    @Override
    public MapPoints findMapPointById(ObjectId id) {
        return mapPoints.get(id);
    }

    @Override
    public List<MapPoints> findMapPointsByType(String type) {
        List<MapPoints> result = new ArrayList<>();
        for (MapPoints mp : mapPoints.values()) {
            if (type != null && type.equals(mp.getType())) {
                result.add(mp);
            }
        }
        return result;
    }

    @Override
    public List<MapPoints> findMapPointsNear(double longitude, double latitude, double maxDistanceMeters) {
        // Simple mock: return all map points (no geospatial calculation)
        return new ArrayList<>(mapPoints.values());
    }

    @Override
    public MapPoints findMapPointByModule(String moduleKey) {
        // Find map point that contains this module in its hardwareConfig
        for (MapPoints mp : mapPoints.values()) {
            if (mp.getHardwareConfig() != null && mp.getHardwareConfig().getModules() != null) {
                // Need to check if any module in the list matches the key
                Modules module = modules.get(moduleKey);
                if (module != null && mp.getHardwareConfig().getModules().contains(module.getId())) {
                    return mp;
                }
            }
        }
        return null;
    }

    @Override
    public boolean updateMapPointLastMeasurement(ObjectId mapPointId, MapPoints.LastMeasurement lastMeasurement) {
        MapPoints mp = mapPoints.get(mapPointId);
        if (mp == null) return false;
        mp.setLastMeasurement(lastMeasurement);
        return true;
    }

    @Override
    public boolean updateMapPoint(MapPoints mapPoint) {
        if (mapPoint == null || mapPoint.getId() == null) return false;
        mapPoints.put(mapPoint.getId(), mapPoint);
        return true;
    }

    @Override
    public boolean deleteMapPoint(ObjectId id) {
        return mapPoints.remove(id) != null;
    }

    @Override
    public List<MapPoints> findAllMapPoints() {
        return new ArrayList<>(mapPoints.values());
    }

    @Override
    public List<MapPoints> findMapPointsWithActiveAlerts() {
        List<MapPoints> result = new ArrayList<>();
        for (MapPoints mp : mapPoints.values()) {
            if (mp.getActiveAlerts() != null && Boolean.TRUE.equals(mp.getActiveAlerts().getHasIssue())) {
                result.add(mp);
            }
        }
        return result;
    }

    @Override
    public void close() {}
}
