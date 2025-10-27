package com.nbenliogludev.routes;

import com.nbenliogludev.config.AppConfig;
import com.nbenliogludev.service.AuthService;
import io.javalin.Javalin;

import java.util.Map;

/**
 * @author nbenliogludev
 */
public class Routes {
    public static void wire(Javalin app, AppConfig cfg, AuthService auth) {
        app.get("/health", ctx -> ctx.result("OK"));

        app.post("/api/register", ctx -> {
            var json = ctx.bodyAsClass(Map.class);
            var u = (String) json.get("username");
            var p = (String) json.get("password");
            try {
                var reg = auth.register(u, p); // creates user + token
                ctx.status(201).json(Map.of(
                        "username", reg.user().username(),
                        "token", reg.token(),
                        "token_type", "Bearer"
                ));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (IllegalStateException e) {
                ctx.status(409).json(Map.of("error", e.getMessage())); // username taken
            }
        });

        app.post("/api/login", ctx -> {
            var json = ctx.bodyAsClass(Map.class);
            var u = (String) json.get("username");
            var p = (String) json.get("password");
            var userOpt = auth.authenticatePassword(u, p);
            if (userOpt.isEmpty()) { ctx.status(401).result("Unauthorized"); return; }
            String token = auth.issueNewToken(userOpt.get());
            ctx.json(Map.of("token", token, "token_type", "Bearer"));
        });

        app.get("/", ctx -> ctx.redirect("/index.html"));
    }
}
