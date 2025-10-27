package com.nbenliogludev;

import com.nbenliogludev.config.AppConfig;
import com.nbenliogludev.db.Db;
import com.nbenliogludev.repository.UserRepository;
import com.nbenliogludev.service.AuthService;
import com.nbenliogludev.routes.Routes;
import io.javalin.Javalin;
import org.flywaydb.core.Flyway;

/**
 * @author nbenliogludev
 */
public class Main {
    public static void main(String[] args) {
        AppConfig cfg = AppConfig.load();

        Db db = Db.init(cfg);
        Flyway.configure()
                .dataSource(db.getDs())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        UserRepository users = new UserRepository(db);
        AuthService auth = new AuthService(users);
        auth.bootstrapIfEmpty(cfg.authBootstrapUser(), cfg.authBootstrapPass());

        Javalin app = Javalin.create(conf -> {
            conf.http.defaultContentType = "application/json";
            conf.bundledPlugins.enableRouteOverview("/__routes");
            conf.staticFiles.add("/public");
        });

        Routes.wire(app, cfg, auth);

        app.start(cfg.port());
    }
}