package org.acme.dtos;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateMaintenanceRecordRequest(
        @NotNull Long maintenanceTypeId,
        @NotNull @PastOrPresent(message = "serviceDate nao pode ser uma data futura") Date serviceDate,
        @PositiveOrZero Integer odometerKm,
        @PositiveOrZero Double engineHours,
        @PositiveOrZero Double cost,
        String notes) {
}
