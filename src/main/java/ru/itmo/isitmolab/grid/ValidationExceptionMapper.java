package ru.itmo.isitmolab.grid;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException e) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("error", "validation");
        payload.put("details", e.getConstraintViolations().stream()
                .map(ValidationExceptionMapper::format)
                .collect(Collectors.toList()));
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload).build();
    }

    private static Map<String, String> format(ConstraintViolation<?> v){
        Map<String, String> m = new HashMap<>();
        m.put("path", String.valueOf(v.getPropertyPath()));
        m.put("message", v.getMessage());
        return m;
    }
}

@Provider
class GenericExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("error", "server");
        payload.put("message", e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
