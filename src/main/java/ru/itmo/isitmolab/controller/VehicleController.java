package ru.itmo.isitmolab.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.service.VehicleService;


@Path("/vehicle")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class VehicleController {

//    @Context
//    private SecurityContext securityContext;

    @Inject
    VehicleService vehicleService;

    @POST
    public Response create(@Valid VehicleDto dto) {
        vehicleService.createNewVehicle(dto);
        return Response.status(Response.Status.CREATED)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid VehicleDto dto) {
        vehicleService.updateVehicle(id, dto);
        return Response.status(Response.Status.OK)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        var res = vehicleService.getVehicleById(id);
        return Response.status(Response.Status.OK)
                .entity(res)
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        vehicleService.deleteVehicleById(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
