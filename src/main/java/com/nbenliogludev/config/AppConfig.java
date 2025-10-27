package com.nbenliogludev.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author nbenliogludev
 */
public record AppConfig(
        int port,
        String uploadDir,
        int retentionDays,
        String dbUrl,
        String dbUser,
        String dbPass,
        String authBootstrapUser,
        String authBootstrapPass
) {
    public static AppConfig load() {
        Config c = ConfigFactory.load();
        return new AppConfig(
                c.getInt("server.port"),
                c.getString("server.uploadDir"),
                c.getInt("server.retentionDays"),
                c.getString("db.url"),
                c.getString("db.user"),
                c.getString("db.pass"),
                c.hasPath("auth.bootstrap.username") ? c.getString("auth.bootstrap.username") : "admin",
                c.hasPath("auth.bootstrap.password") ? c.getString("auth.bootstrap.password") : "admin"
        );
    }
}
