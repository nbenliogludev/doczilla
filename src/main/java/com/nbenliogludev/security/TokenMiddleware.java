package com.nbenliogludev.security;

import com.nbenliogludev.model.User;
import com.nbenliogludev.service.AuthService;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;

import java.util.Optional;

/**
 * @author nbenliogludev
 */
public class TokenMiddleware {
    public static final String ATTR_USER = "auth.user";

    public static void protectApiWithBearer(Javalin app, AuthService auth) {
        app.before(ctx -> {
            final String path = ctx.path();
            if (!path.startsWith("/api/")) return;

            // allowlist auth endpoints
            if (path.equals("/api/login") || path.equals("/api/register")) return;

            // allow CORS preflight etc.
            if (ctx.method() == HandlerType.OPTIONS) return;

            String authz = ctx.header("Authorization");
            if (authz == null || !authz.startsWith("Bearer ")) {
                ctx.status(401).result("Unauthorized");
                return;
            }

            String token = authz.substring(7);
            Optional<User> u = auth.authenticateToken(token);
            if (u.isEmpty()) {
                ctx.status(401).result("Unauthorized");
                return;
            }

            // attach user for handlers
            ctx.attribute(ATTR_USER, u.get());
        });
    }

    public static User currentUser(io.javalin.http.Context ctx) {
        return ctx.attribute(ATTR_USER);
    }
}
