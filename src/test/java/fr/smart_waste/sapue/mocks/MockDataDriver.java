package fr.smart_waste.sapue.mocks;

import fr.smart_waste.sapue.dataaccess.DataDriver;
import fr.smart_waste.sapue.model.*;
import org.bson.types.ObjectId;

import java.util.*;

public class MockDataDriver implements DataDriver {
    
    public Map<String, Modules> modules = new HashMap<>();
    public Map<ObjectId, Chipsets> chipsets = new HashMap<>();
    public Map<ObjectId, Reports> reports = new HashMap<>();
    public Map<ObjectId, MapPoints> mapPoints = new HashMap<>();
    public Map<ObjectId, Measurements> measurements = new HashMap<>();
    public Map<ObjectId, Users> users = new HashMap<>();
    
    public Measurements lastInsertedMeasurement;
    
    public boolean shouldFailNextInsert = false;
    public boolean available = true;
    public boolean fallbackAvailable = true;
    public boolean usedFallback = false;

    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public boolean isAvailable() {
        return available;
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

    // ========== MapPoints Operations (Mock) ==========

    public void addMapPoint(MapPoints mapPoint) {
        if (mapPoint.getId() == null) mapPoint.setId(new ObjectId());
        mapPoints.put(mapPoint.getId(), mapPoint);
    }

    @Override
    public ObjectId insertMapPoint(MapPoints mapPoint) {
        if (shouldFailNextInsert) return null;
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
        // Simple mock: return all map points
        return new ArrayList<>(mapPoints.values());
    }

    @Override
    public MapPoints findMapPointByModule(String moduleKey) {
        Modules module = modules.get(moduleKey);
        if (module == null) return null;
        for (MapPoints mp : mapPoints.values()) {
            if (mp.getModules() != null && mp.getModules().contains(module.getId())) {
                return mp;
            }
        }
        return null;
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

    // ========== Module Operations (Mock) ==========

    @Override
    public ObjectId insertModule(Modules module) {
        if (module == null) return null;
        if (module.getId() == null) module.setId(new ObjectId());
        modules.put(module.getKey(), module);
        return module.getId();
    }

    @Override
    public Modules findModuleById(ObjectId id) {
        return modules.values().stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Modules findModuleByKey(String key) {
        return modules.get(key);
    }

    @Override
    public boolean updateModule(Modules module) {
        if (module == null || module.getKey() == null) return false;
        modules.put(module.getKey(), module);
        return true;
    }

    @Override
    public boolean deleteModule(ObjectId id) {
        Modules m = findModuleById(id);
        if (m == null) return false;
        modules.remove(m.getKey());
        return true;
    }

    @Override
    public List<Modules> findAllModules() {
        return new ArrayList<>(modules.values());
    }

    // ========== Chipsets Operations (Mock) ==========

    @Override
    public ObjectId insertChipset(Chipsets chipset) {
        if (chipset == null) return null;
        if (chipset.getId() == null) chipset.setId(new ObjectId());
        chipsets.put(chipset.getId(), chipset);
        return chipset.getId();
    }

    @Override
    public Chipsets findChipsetById(ObjectId id) {
        return chipsets.get(id);
    }

    @Override
    public List<Chipsets> findChipsetsByModuleId(ObjectId moduleId) {
        return chipsets.values().stream()
                .filter(c -> moduleId != null && moduleId.equals(c.getModuleID()))
                .toList();
    }

    @Override
    public boolean updateChipset(Chipsets chipset) {
        if (chipset == null || chipset.getId() == null) return false;
        chipsets.put(chipset.getId(), chipset);
        return true;
    }

    @Override
    public boolean deleteChipset(ObjectId id) {
        return chipsets.remove(id) != null;
    }

    @Override
    public List<Chipsets> findAllChipsets() {
        return new ArrayList<>(chipsets.values());
    }

    // ========== Reports Operations (Mock) ==========

    public void addReport(Reports report) {
        if (report.getId() == null) report.setId(new ObjectId());
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
        return reports.values().stream()
                .filter(r -> status != null && status.equalsIgnoreCase(r.getStatus()))
                .toList();
    }

    @Override
    public List<Reports> findReportsByMapPoint(ObjectId mapPointId) {
        return reports.values().stream()
                .filter(r -> mapPointId != null && mapPointId.equals(r.getMapPoint()))
                .toList();
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

    // ========== Measurements Operations (Mock) ==========

    @Override
    public ObjectId insertMeasurement(Measurements measurement) {
        if (shouldFailNextInsert) return null;
        if (!available) {
            if (fallbackAvailable) {
                lastInsertedMeasurement = measurement;
                usedFallback = true;
                return new ObjectId();
            }
            return null;
        }
        if (measurement.getId() == null) measurement.setId(new ObjectId());
        measurements.put(measurement.getId(), measurement);
        lastInsertedMeasurement = measurement;
        return measurement.getId();
    }

    @Override
    public Measurements findMeasurementById(ObjectId id) {
        return measurements.get(id);
    }

    @Override
    public List<Measurements> findMeasurementsByController(ObjectId idController) {
        return measurements.values().stream()
                .filter(m -> idController != null && idController.equals(m.getId_Controller()))
                .toList();
    }

    @Override
    public List<Measurements> findMeasurementsByModuleId(ObjectId idController, java.util.Date startDate, java.util.Date endDate) {
        // Mock implementation that filters by controller and date range
        return measurements.values().stream()
                .filter(m -> idController != null && idController.equals(m.getId_Controller()))
                .filter(m -> {
                    if (m.getDate() == null) return false;
                    if (startDate != null && m.getDate().before(startDate)) return false;
                    if (endDate != null && m.getDate().after(endDate)) return false;
                    return true;
                })
                .toList();
    }

    @Override
    public boolean updateMeasurement(Measurements measurement) {
        if (measurement == null || measurement.getId() == null) return false;
        measurements.put(measurement.getId(), measurement);
        return true;
    }

    @Override
    public boolean deleteMeasurement(ObjectId id) {
        return measurements.remove(id) != null;
    }

    @Override
    public List<Measurements> findAllMeasurements() {
        return new ArrayList<>(measurements.values());
    }

    @Override
    public List<Measurements> findMeasurementsByDateRange(java.util.Date startDate, java.util.Date endDate) {
        return measurements.values().stream()
                .filter(m -> {
                    if (m.getDate() == null) return false;
                    if (startDate != null && m.getDate().before(startDate)) return false;
                    if (endDate != null && m.getDate().after(endDate)) return false;
                    return true;
                })
                .toList();
    }


    // ========== Users Operations (Mock) ==========

    @Override
    public Users findUserById(ObjectId id) {
        return users.get(id);
    }

    @Override
    public Users findUserByMail(String mail) {
        return users.values().stream()
                .filter(u -> mail != null && mail.equalsIgnoreCase(u.getMail()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ObjectId insertUser(Users user) {
        if (user == null) return null;
        if (user.getId() == null) user.setId(new ObjectId());
        users.put(user.getId(), user);
        return user.getId();
    }

    @Override
    public boolean updateUser(Users user) {
        if (user == null || user.getId() == null) return false;
        users.put(user.getId(), user);
        return true;
    }

    @Override
    public boolean deleteUser(ObjectId id) {
        return users.remove(id) != null;
    }

    @Override
    public List<Users> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    // ========== AnalyseMedia Operations (Mock) ==========

    @Override
    public ObjectId insertAnalyseMedia(AnalyseMedia analyseMedia) {
        return new ObjectId();
    }

    @Override
    public AnalyseMedia findAnalyseMediaById(ObjectId id) {
        return null;
    }

    @Override
    public boolean updateAnalyseMedia(AnalyseMedia analyseMedia) {
        return true;
    }

    @Override
    public boolean deleteAnalyseMedia(ObjectId id) {
        return true;
    }

    @Override
    public List<AnalyseMedia> findAllAnalyseMedias() {
        return new ArrayList<>();
    }

    @Override
    public String getHexaIconByWasteBinType(String wasteBinType) {
        // Mock: return a default hex icon for testing
        if (wasteBinType == null || wasteBinType.isEmpty()) {
            return "00";
        }
        // Simple mock that returns predictable values for testing
        switch (wasteBinType.toLowerCase()) {
            case "jaune":
                return "01";
            case "verte":
                return "02";
            case "grise":
                return "03";
            case "marron":
                return "04";
            default:
                return "00";
        }
    }

    @Override
    public void close() {}
}
