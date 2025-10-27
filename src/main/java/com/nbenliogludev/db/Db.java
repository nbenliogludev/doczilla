package com.nbenliogludev.db;

import com.nbenliogludev.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import javax.sql.DataSource;

/**
 * @author nbenliogludev
 */
public class Db {
    private final HikariDataSource ds;
    private final Jdbi jdbi;

    private Db(HikariDataSource ds) {
        this.ds = ds;
        this.jdbi = Jdbi.create(ds);
    }

    public static Db init(AppConfig cfg) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(cfg.dbUrl());
        hc.setUsername(cfg.dbUser());
        hc.setPassword(cfg.dbPass());
        hc.setMaximumPoolSize(10);
        return new Db(new HikariDataSource(hc));
    }

    public DataSource getDs() { return ds; }
    public Jdbi jdbi() { return jdbi; }
}
