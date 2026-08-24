package org.acme.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo esperado por POST /auth/reset-password. */
public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, message = "newPassword deve ter no minimo 8 caracteres") String newPassword) {
}
