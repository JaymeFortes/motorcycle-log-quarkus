package org.acme.controllers;

import org.acme.dtos.ForgotPasswordRequest;
import org.acme.dtos.LoginRequest;
import org.acme.dtos.LoginResponse;
import org.acme.dtos.RegisterRequest;
import org.acme.dtos.ResetPasswordRequest;
import org.acme.dtos.UserResponse;
import org.acme.services.AuthService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Rotas publicas de autenticacao (nao exigem JWT). Fina de proposito: so
 * valida o corpo (@Valid) e delega tudo para AuthService.
 *
 * @NotNull ao lado de @Valid em cada parametro: @Valid sozinho so valida os
 * CAMPOS de dentro do objeto, e nao dispara nada se o corpo da requisicao
 * chegar vazio/malformado (o record inteiro vira null). Sem o @NotNull, esse
 * null passava direto pro service e explodia em NullPointerException (500)
 * em vez do 422 esperado para entrada invalida.
 */
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(@NotNull @Valid RegisterRequest request) {
        UserResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/login")
    public LoginResponse login(@NotNull @Valid LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/forgot-password")
    public Response forgotPassword(@NotNull @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        // 200 sempre, exista ou nao o e-mail - a decisao de nao vazar essa
        // informacao ja foi tomada dentro de AuthService.forgotPassword.
        return Response.ok().build();
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@NotNull @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Response.ok().build();
    }
}
