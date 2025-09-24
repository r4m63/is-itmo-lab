package ru.itmo.isitmolab.grid;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Vehicle;

import java.util.stream.Collectors;

@Path("/vehicles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class VehicleController {

    @Inject VehicleRepository repo;
    @Inject VehicleService service;

    @POST @Path("/query")
    public GridResponse<VehicleDto> query(@QueryParam("startRow") @DefaultValue("0") int startRow,
                                          @QueryParam("endRow")   @DefaultValue("50") int endRow,
                                          GridQueryRequest req) {
        var res = repo.query(req, startRow, endRow);
        var rows = res.data.stream().map(VehicleMapper::toDto).collect(Collectors.toList());
        return new GridResponse<>(rows, res.total);
    }

    @POST
    public Response create(@Valid Vehicle v){
        var saved = service.create(v);
        return Response.status(Response.Status.CREATED).entity(VehicleMapper.toDto(saved)).build();
    }

    @GET @Path("/{id}")
    public Response get(@PathParam("id") Long id){
        var v = service.get(id);
        if (v == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(VehicleMapper.toDto(v)).build();
    }

    @PUT @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid Vehicle patch){
        var updated = service.update(id, patch);
        return Response.ok(VehicleMapper.toDto(updated)).build();
    }

    @DELETE @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id){
        service.delete(id);
        return Response.noContent().build();
    }
}