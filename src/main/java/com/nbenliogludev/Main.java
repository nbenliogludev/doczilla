package com.nbenliogludev;

import com.nbenliogludev.config.AppConfig;
import com.nbenliogludev.db.Db;
import com.nbenliogludev.files.FileStorage;
import com.nbenliogludev.files.LocalFileStorage;
import com.nbenliogludev.repository.FileRepository;
import com.nbenliogludev.repository.UserRepository;
import com.nbenliogludev.security.TokenMiddleware;
import com.nbenliogludev.service.AuthService;
import com.nbenliogludev.service.FileService;
import com.nbenliogludev.routes.Routes;
import com.nbenliogludev.tasks.CleanupJob;
import io.javalin.Javalin;
import org.flywaydb.core.Flyway;

/**
 * @author nbenliogludev
 */
public class Main {
    public static void main(String[] args) {
        AppConfig cfg = AppConfig.load();

        Db db = Db.init(cfg);
        Flyway.configure().dataSource(db.getDs()).locations("classpath:db/migration").load().migrate();

        var users = new UserRepository(db);
        var auth  = new AuthService(users);
        auth.bootstrapIfEmpty(cfg.authBootstrapUser(), cfg.authBootstrapPass());

        FileRepository fileRepo = new FileRepository(db);
        FileStorage fileStorage = new LocalFileStorage(cfg.uploadDir());
        FileService fileService = new FileService(fileRepo, fileStorage);

        Javalin app = Javalin.create(conf -> {
            conf.http.defaultContentType = "application/json";
            conf.bundledPlugins.enableRouteOverview("/__routes");
            conf.staticFiles.add("/public");
        });

        TokenMiddleware.protectApiWithBearer(app, auth);

        Routes.wire(app, cfg, auth, db, fileService);

        CleanupJob.start(fileService, cfg.retentionDays());

        app.start(cfg.port());
    }
}