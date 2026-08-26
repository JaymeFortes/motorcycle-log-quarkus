package org.acme.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BotMaintenanceRequest(
        Long chatId,
        Long maintenanceTypeId,
        LocalDate serviceDate,
        Integer odometerKm,
        Double engineHours,
        BigDecimal cost,
        String notes) {
}