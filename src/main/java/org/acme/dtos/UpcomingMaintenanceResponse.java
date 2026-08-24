package org.acme.dtos;

/**
 * Um item da lista de GET /motorcycles/{id}/maintenances/upcoming (UC10).
 * remainingKm/remainingEngineHours podem ser negativos - isso e o que
 * significa "vencido" (ja passou do intervalo previsto).
 */
public record UpcomingMaintenanceResponse(
        Long maintenanceTypeId,
        String maintenanceTypeName,
        Integer dueAtKm,
        Integer remainingKm,
        Double dueAtEngineHours,
        Double remainingEngineHours,
        Status status) {

    public enum Status {
        NEAR,
        OVERDUE
    }
}
