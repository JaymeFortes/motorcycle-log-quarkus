package org.acme.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo esperado por POST /auth/register. As anotacoes de Bean Validation
 * sao checadas automaticamente pelo Quarkus quando o parametro do metodo do
 * resource e anotado com @Valid; violacoes viram ConstraintViolationException,
 * que o ValidationExceptionMapper converte em 422 (ver ExceptionMapper).
 */
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "password deve ter no minimo 8 caracteres") String password) {
}
