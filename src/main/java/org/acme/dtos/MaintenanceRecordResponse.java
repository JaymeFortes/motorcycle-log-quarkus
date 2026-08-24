package org.acme.dtos;

import java.time.Instant;
import java.util.Date;

import org.acme.models.MaintenanceRecord;

public record MaintenanceRecordResponse(
        Long id,
        Long motorcycleId,
        Long maintenanceTypeId,
        String maintenanceTypeName,
        Date serviceDate,
        Integer odometerKm,
        Double engineHours,
        Double cost,
        String notes,
        Instant createdAt) {

    public static MaintenanceRecordResponse from(MaintenanceRecord record) {
        return new MaintenanceRecordResponse(
                record.getId(),
                record.getMotorcycle().getId(),
                record.getMaintenanceType().getId(),
                record.getMaintenanceType().getName(),
                record.getService_date(),
                record.getOdometer_km(),
                record.getEngine_hours(),
                record.getCost(),
                record.getNotes(),
                record.getCreated_at());
    }
}
