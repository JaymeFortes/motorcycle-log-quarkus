package org.acme.dtos;

import java.time.Instant;

import org.acme.models.Motorcycle;

/**
 * Formato de retorno das rotas de moto. Expoe "ownerId" (so o id), nunca o
 * User inteiro - evita vazar e-mail/role/password_hash do dono junto com
 * os dados da moto.
 */
public record MotorcycleResponse(
        Long id,
        String brand,
        String model,
        Integer modelYear,
        String plate,
        Integer currentKm,
        Double currentEngineHours,
        Instant createdAt,
        Long ownerId) {

    public static MotorcycleResponse from(Motorcycle motorcycle) {
        return new MotorcycleResponse(
                motorcycle.getId(),
                motorcycle.getBrand(),
                motorcycle.getModel(),
                motorcycle.getModel_year(),
                motorcycle.getPlate(),
                motorcycle.getCurrent_km(),
                motorcycle.getCurrent_engine_hours(),
                motorcycle.getCreated_at(),
                motorcycle.getOwner().getId());
    }
}
