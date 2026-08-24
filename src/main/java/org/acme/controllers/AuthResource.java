package org.acme.controllers;

import org.acme.dtos.LoginRequest;
import org.acme.dtos.LoginResponse;
import org.acme.dtos.RegisterRequest;
import org.acme.dtos.UserResponse;
import org.acme.services.AuthService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Rotas publicas de autenticacao (nao exigem JWT). Fina de proposito: so
 * valida o corpo (@Valid) e delega tudo para AuthService.
 */
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        UserResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/login")
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }
}
