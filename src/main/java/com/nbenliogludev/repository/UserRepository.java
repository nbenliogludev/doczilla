package com.nbenliogludev.repository;

import com.nbenliogludev.db.Db;
import com.nbenliogludev.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author nbenliogludev
 */
public class UserRepository {
    private final Db db;
    public UserRepository(Db db) { this.db = db; }

    public long count() {
        return db.jdbi().withHandle(h ->
                h.createQuery("select count(*) from users").mapTo(Long.class).one()
        );
    }

    public boolean existsByUsername(String username) {
        return db.jdbi().withHandle(h ->
                h.createQuery("select 1 from users where username=:u")
                        .bind("u", username)
                        .mapTo(Integer.class)
                        .findOne()
                        .isPresent()
        );
    }

    public User insert(String username, String passwordHash, String apiToken) {
        db.jdbi().useHandle(h ->
                h.createUpdate("""
            insert into users (username, password_hash, api_token, token_created_at)
            values (:u, :ph, :t, :tc)
        """)
                        .bind("u", username)
                        .bind("ph", passwordHash)
                        .bind("t", apiToken)
                        .bind("tc", java.time.LocalDateTime.now())
                        .execute()
        );

        // Read back (works on H2 and Postgres the same)
        return db.jdbi().withHandle(h ->
                h.createQuery("""
            select id, username, password_hash, api_token, token_created_at
            from users where username = :u
        """)
                        .bind("u", username)
                        .map((rs, c) -> new com.nbenliogludev.model.User(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getString("api_token"),
                                rs.getObject("token_created_at", java.time.LocalDateTime.class)
                        ))
                        .one()
        );
    }

    public void updateToken(long id, String token) {
        db.jdbi().useHandle(h ->
                h.createUpdate("""
                update users set api_token=:t, token_created_at=:tc where id=:id
            """)
                        .bind("t", token)
                        .bind("tc", LocalDateTime.now())
                        .bind("id", id)
                        .execute()
        );
    }

    public Optional<User> findByUsername(String username) {
        return db.jdbi().withHandle(h ->
                h.createQuery("""
                select id, username, password_hash, api_token, token_created_at
                from users where username=:u
            """)
                        .bind("u", username)
                        .map((rs, c) -> new User(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getString("api_token"),
                                rs.getObject("token_created_at", LocalDateTime.class)
                        )).findOne()
        );
    }

    public Optional<User> findByToken(String token) {
        return db.jdbi().withHandle(h ->
                h.createQuery("""
                select id, username, password_hash, api_token, token_created_at
                from users where api_token=:t
            """)
                        .bind("t", token)
                        .map((rs, c) -> new User(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getString("api_token"),
                                rs.getObject("token_created_at", LocalDateTime.class)
                        )).findOne()
        );
    }
}
