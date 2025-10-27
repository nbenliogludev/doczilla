package com.nbenliogludev.routes;

import com.nbenliogludev.config.AppConfig;
import com.nbenliogludev.db.Db;
import com.nbenliogludev.model.FileRecord;
import com.nbenliogludev.service.AuthService;
import com.nbenliogludev.service.FileService;
import io.javalin.Javalin;
import io.javalin.http.ContentType;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author nbenliogludev
 */
public class Routes {
    public static void wire(Javalin app, AppConfig cfg, AuthService auth, Db db, FileService files) {
        app.get("/health", ctx -> ctx.result("OK"));

        app.post("/api/register", ctx -> {
            var json = ctx.bodyAsClass(Map.class);
            var u = (String) json.get("username");
            var p = (String) json.get("password");
            try {
                var reg = auth.register(u, p);
                ctx.status(201).json(Map.of(
                        "username", reg.user().username(),
                        "token", reg.token(),
                        "token_type", "Bearer"
                ));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            } catch (IllegalStateException e) {
                ctx.status(409).json(Map.of("error", e.getMessage()));
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

        app.post("/api/upload", ctx -> {
            var f = ctx.uploadedFile("file");
            if (f == null) { ctx.status(400).json(Map.of("error","file is required")); return; }
            try (InputStream in = f.content()) {
                String ct = f.contentType() != null ? f.contentType() : "application/octet-stream";
                FileRecord rec = files.store(f.filename(), ct, f.size(), in);
                ctx.json(Map.of(
                        "id", rec.id().toString(),
                        "name", rec.originalName(),
                        "size", rec.sizeBytes(),
                        "downloadUrl", "/d/" + rec.downloadToken()
                ));
            }
        });

        app.get("/d/{token}", ctx -> {
            String token = ctx.pathParam("token");
            var opt = files.findByToken(token);
            if (opt.isEmpty()) { ctx.status(404).result("Not found"); return; }
            FileRecord r = opt.get();
            files.touchDownload(r.id());
            String name = URLEncoder.encode(r.originalName(), StandardCharsets.UTF_8);
            ctx.header("Content-Disposition", "attachment; filename*=UTF-8''" + name);
            ctx.contentType(r.contentType() != null ? r.contentType() : ContentType.APPLICATION_OCTET_STREAM.toString());
            ctx.result(Files.newInputStream(Path.of(r.storagePath())));
        });

        app.get("/", ctx -> ctx.redirect("/index.html"));
    }
}