package org.acme.controllers;

import org.acme.dtos.UserResponse;
import org.acme.services.UserService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    // Primeira rota protegida do projeto: exige JWT valido (401 sem token /
    // token invalido) E que o "role" do usuario logado seja "admin" (403 se
    // for um "user" normal). O valor comparado aqui vem do claim "groups" do
    // JWT, que AuthService.login() preenche a partir do campo @Roles de User.
    @GET
    @Path("{id}")
    @RolesAllowed("admin")
    public Response getUserById(@PathParam("id") Long id) {
        UserResponse response = UserResponse.from(userService.getUserById(id));
        return Response.ok().entity(response).build();
    }
}
