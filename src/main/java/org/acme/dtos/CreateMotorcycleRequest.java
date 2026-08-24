package org.acme.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Corpo esperado por POST /motorcycles. Sem "owner": o dono nunca vem do
 * corpo da requisicao - quem cria a moto e sempre o usuario autenticado
 * (resolvido via UserService.getAuthenticatedUser() no service), nunca um
 * valor que o cliente poderia forjar mandando o id/e-mail de outra pessoa.
 * Sem "id"/"createdAt" tambem, porque quem gera isso e o servidor.
 */
public record CreateMotorcycleRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull @PositiveOrZero Integer modelYear,
        @NotBlank String plate,
        @NotNull @PositiveOrZero Integer currentKm,
        @NotNull @PositiveOrZero Double currentEngineHours) {
}
