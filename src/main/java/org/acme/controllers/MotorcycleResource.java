package org.acme.controllers;

import org.acme.dtos.CreateMotorcycleRequest;
import org.acme.dtos.MotorcycleResponse;
import org.acme.services.MotorcycleService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// @Authenticated na classe (nao @RolesAllowed): nao importa o role do
// usuario aqui, so que ele esteja logado - toda rota de /motorcycles vai
// exigir isso, entao fica no nivel da classe pra valer automaticamente
// pros proximos endpoints (GET, PUT, DELETE) sem precisar repetir.
@Path("/motorcycles")
@Authenticated // so que o usuario esteja logado - toda rota de /motorcycles vai exigir isso, entao fica no nivel da classe pra valer automaticamente pros proximos endpoints (GET, PUT, DELETE) sem precisar repetir.
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MotorcycleResource {

    @Inject
    private MotorcycleService motorcycleService;

    @POST
    public Response register(@NotNull @Valid CreateMotorcycleRequest request) {
        MotorcycleResponse response = motorcycleService.createMotorcycle(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
