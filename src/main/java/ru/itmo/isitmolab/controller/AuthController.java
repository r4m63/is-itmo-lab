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
import ru.itmo.isitmolab.dto.CredsDto;
import ru.itmo.isitmolab.service.AuthService;

import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthController {

    @Inject
    AuthService authService;

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @POST
    @Path("/login")
    public Response login(@Valid CredsDto creds) {
        authService.login(creds, request, response);
        return Response.ok(Map.of("status", "ok")).build();
    }

    @GET
    @Path("/check-session")
    public Response check() {
        return authService.isSessionActive(request)
                ? Response.ok(Map.of("status", "ok")).build()
                : Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("message", "No active session")).build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        authService.logout(request, response);
        return Response.ok(Map.of("status", "ok")).build();
    }

}
