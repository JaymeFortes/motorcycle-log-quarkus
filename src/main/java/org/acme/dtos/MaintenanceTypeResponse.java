package org.acme.dtos;

import org.acme.models.MaintenanceType;

public record MaintenanceTypeResponse(
        Long id,
        String name,
        Integer intervalKm,
        Double intervalEngineHours) {

    public static MaintenanceTypeResponse from(MaintenanceType type) {
        return new MaintenanceTypeResponse(
                type.getId(),
                type.getName(),
                type.getInterval_km(),
                type.getInterval_engine_hours());
    }
}
