package org.acme.dtos;

/**
 * Corpo esperado por PATCH /motorcycles/{id}. Diferente de
 * CreateMotorcycleRequest (usado tambem no PUT, que exige o objeto inteiro),
 * aqui NENHUM campo e obrigatorio - de proposito, sem @NotBlank/@NotNull.
 * Um campo null significa "nao mexer nesse campo"; so os campos que vierem
 * preenchidos sao aplicados (ver MotorcycleService.patchMotorcycle).
 */
public record UpdateMotorcycleRequest(
        String brand,
        String model,
        Integer modelYear,
        String plate,
        Integer currentKm,
        Double currentEngineHours) {
}
