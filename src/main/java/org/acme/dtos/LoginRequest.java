package org.acme.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo esperado por POST /auth/login. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
