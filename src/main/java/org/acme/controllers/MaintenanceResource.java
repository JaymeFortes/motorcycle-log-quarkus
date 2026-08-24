package org.acme.controllers;

import org.acme.dtos.CreateMaintenanceRecordRequest;
import org.acme.dtos.MaintenanceRecordResponse;
import org.acme.services.MaintenanceService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/motorcycles/{id}/maintenances")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class MaintenanceResource {

    @Inject
    private MaintenanceService maintenanceService;

    @POST
    public Response registerMaintenance(@PathParam("id") Long motorcycleId, @Valid CreateMaintenanceRecordRequest request) {
        MaintenanceRecordResponse response = maintenanceService.registerMaintenance(motorcycleId, request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public Response listMaintenances(@PathParam("id") Long motorcycleId) {
        return Response.ok(maintenanceService.listMaintenances(motorcycleId)).build();
    }
}
