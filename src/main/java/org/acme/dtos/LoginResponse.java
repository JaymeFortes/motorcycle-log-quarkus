package org.acme.dtos;

/**
 * Retorno de POST /auth/login: o JWT que o cliente deve enviar depois como
 * "Authorization: Bearer {token}", e por quantos segundos ele vale.
 */
public record LoginResponse(String token, long expiresIn) {
}
