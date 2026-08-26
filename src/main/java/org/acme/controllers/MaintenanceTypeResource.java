package org.acme.controllers;

import java.util.List;

import org.acme.dtos.MaintenanceTypeResponse;
import org.acme.services.MaintenanceService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Catalogo fixo de tipos de manutencao (seed via import.sql), so leitura. */
@Path("/maintenance-types")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class MaintenanceTypeResource {

    @Inject
    MaintenanceService maintenanceService;

    @GET
    public List<MaintenanceTypeResponse> list() {
        return maintenanceService.listMaintenanceTypes();
    }
}
