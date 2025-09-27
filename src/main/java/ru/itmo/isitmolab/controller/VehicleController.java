package ru.itmo.isitmolab.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import ru.itmo.isitmolab.dto.GridRequest;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.service.VehicleService;

import java.util.List;
import java.util.Map;


@Path("/vehicle")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class VehicleController {

//    @Context
//    private SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @Inject
    VehicleService vehicleService;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @POST
    public Response create(@Valid VehicleDto dto) {
        Long id = vehicleService.createNewVehicle(dto, request);
        return Response.status(Response.Status.CREATED)
                .entity(Map.of("id", id))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid VehicleDto dto) {
        vehicleService.updateVehicle(id, dto);
        return Response.noContent().build();
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

    @GET
    public Response listAll(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("1000") int limit
    ) {
        List<VehicleDto> res = vehicleService.getAllVehicles();
        return Response.ok(res).build();
    }

    @POST
    @Path("/query")
    public Response queryVehicles(@Valid GridRequest req) {
        var result = vehicleService.queryVehicles(req);
        return Response.ok(result).build();
    }

}
