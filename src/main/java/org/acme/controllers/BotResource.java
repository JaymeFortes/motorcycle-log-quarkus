package org.acme.controllers;

import org.acme.dtos.BotMaintenanceRequest;
import org.acme.dtos.BotReply;
import org.acme.services.MaintenanceService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;



@Path("/bot")
public class BotResource {

    @ConfigProperty(name = "motolog.bot.api-key")
    String apiKey;

    @Inject
    MaintenanceService maintenanceService;

    @POST
    @Path("/maintenance")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BotReply register(@HeaderParam("X-API-Key") String key,
                             BotMaintenanceRequest req) {
        if (apiKey == null || !apiKey.equals(key)) {
            throw new WebApplicationException("API key inválida", 401);
        }
        maintenanceService.registerFromBot(req);
        return new BotReply("Manutenção registrada!");
    }
}