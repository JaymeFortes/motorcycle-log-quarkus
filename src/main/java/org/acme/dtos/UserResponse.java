package org.acme.dtos;

import java.time.Instant;

import org.acme.models.User;

/**
 * Formato de retorno de POST /auth/register. Existe para nunca deixar o
 * password_hash (nem qualquer outro detalhe interno) vazar na resposta HTTP -
 * o resource nunca serializa a entidade User diretamente.
 */
public record UserResponse(Long id, String name, String email, String role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreated_at());
    }
}
