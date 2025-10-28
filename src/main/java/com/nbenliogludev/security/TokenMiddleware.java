package com.nbenliogludev.security;

import com.nbenliogludev.model.User;
import com.nbenliogludev.service.AuthService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

import java.util.Optional;
import java.util.Set;

/**
 * @author nbenliogludev
 */
public class TokenMiddleware {
    public static final String ATTR_USER = "auth.user";
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/login", "/api/register"
    );

    public static void protectApiWithBearer(Javalin app, AuthService auth) {
        app.before(ctx -> {
            final String path = ctx.path();
            final HandlerType method = ctx.method();

            if (!path.startsWith("/api/")) return;

            if (method == HandlerType.OPTIONS) return;

            if (PUBLIC_ENDPOINTS.contains(path)) return;

            var tokenOpt = BearerTokenUtil.parseBearer(ctx.header("Authorization"));
            if (tokenOpt.isEmpty()) {
                unauthorized(ctx, "missing bearer");
                return;
            }

            Optional<User> user = auth.authenticateToken(tokenOpt.get());
            if (user.isEmpty()) {
                unauthorized(ctx, "invalid token");
                return;
            }

            ctx.attribute(ATTR_USER, user.get());
        });
    }

    private static void unauthorized(Context ctx, String why) {
        ctx.header("WWW-Authenticate", "Bearer");
        ctx.status(401).result("Unauthorized");
        ctx.skipRemainingHandlers(); // IMPORTANT: stop pipeline in Javalin 6
    }

    public static User currentUser(io.javalin.http.Context ctx) {
        return ctx.attribute(ATTR_USER);
    }
}