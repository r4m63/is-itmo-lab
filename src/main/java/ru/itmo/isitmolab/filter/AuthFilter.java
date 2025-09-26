package ru.itmo.isitmolab.filter;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import ru.itmo.isitmolab.service.SessionService;

import java.io.IOException;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    HttpServletRequest request;


    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        final String path = ctx.getUriInfo().getPath();
        final String method = ctx.getMethod();

        // check auth/*
        if (path.equals("auth") || path.startsWith("/auth")) {
            return;
        }

        // check OPTION preflight CORS
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        // check session
        HttpSession session = request.getSession(false);
        boolean ok = session != null && session.getAttribute(SessionService.ATTR_USER_ID) != null;

        if (ok) {
            return;
        }

        // 401
        ctx.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("message", "Unauthorized"))
                        .build()
        );
    }

}
