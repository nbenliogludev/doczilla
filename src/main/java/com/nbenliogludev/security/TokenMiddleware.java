package com.nbenliogludev.security;

import com.nbenliogludev.model.User;
import com.nbenliogludev.service.AuthService;
import io.javalin.Javalin;

/**
 * @author nbenliogludev
 */
public class TokenMiddleware {
    public static final String ATTR_USER = "auth.user";

    public static void protectApiWithBearer(Javalin app, AuthService authService) {
        app.before("/api/*", ctx -> {
            var tokenOpt = BearerTokenUtil.parseBearer(ctx.header("Authorization"));
            if (tokenOpt.isEmpty()) {
                ctx.header("WWW-Authenticate", "Bearer");
                ctx.status(401).result("Unauthorized");
                return;
            }
            var userOpt = authService.authenticateToken(tokenOpt.get());
            if (userOpt.isEmpty()) {
                ctx.header("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                ctx.status(401).result("Unauthorized");
                return;
            }
            ctx.attribute(ATTR_USER, userOpt.get());
        });
    }

    public static User currentUser(io.javalin.http.Context ctx) {
        return ctx.attribute(ATTR_USER);
    }
}
