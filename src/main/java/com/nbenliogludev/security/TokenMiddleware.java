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
    private static final Set<String> PUBLIC_POSTS = Set.of("/api/login", "/api/register");

    public static void protectApiWithBearer(Javalin app, AuthService auth) {
        app.before(ctx -> {
            final String path = ctx.path();
            final HandlerType method = ctx.method();

            if (!path.startsWith("/api/")) return;

            if (method == HandlerType.OPTIONS) return;

            if (PUBLIC_POSTS.contains(path)) return;

            if (path.equals("/api/health")) return;

            String authz = ctx.header("Authorization");
            if (authz == null || !authz.startsWith("Bearer ")) {
                unauthorized(ctx, "missing bearer");
                return;
            }

            String token = authz.substring(7);
            Optional<User> u = auth.authenticateToken(token);
            if (u.isEmpty()) {
                unauthorized(ctx, "bad token");
                return;
            }

            ctx.attribute(ATTR_USER, u.get());
        });
    }

    private static void unauthorized(Context ctx, String why) {
        ctx.status(401).result("Unauthorized");
        ctx.skipRemainingHandlers();
    }

    public static User currentUser(io.javalin.http.Context ctx) {
        return ctx.attribute(ATTR_USER);
    }
}
