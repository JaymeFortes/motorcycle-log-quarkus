package org.acme.controllers;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Por padrao o RESTEasy Reactive responde 400 quando o Bean Validation
 * (@Valid nos DTOs) rejeita o corpo da requisicao. O contrato do MVP exige
 * 422 para esse caso, entao interceptamos a excecao aqui e trocamos o
 * status, mantendo as mensagens de cada violacao no corpo da resposta.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("violations", errors))
                .build();
    }
}
