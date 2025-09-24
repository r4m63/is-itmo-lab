package ru.itmo.isitmolab.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
public class TestController {

    public record Message(String message) {}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message ping() {
        return new Message("ok");
    }

}
