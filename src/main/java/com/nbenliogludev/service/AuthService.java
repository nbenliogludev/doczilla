package com.nbenliogludev.service;

import com.nbenliogludev.model.User;
import com.nbenliogludev.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

/**
 * @author nbenliogludev
 */
public class AuthService {
    private final UserRepository users;
    private final SecureRandom rnd = new SecureRandom();
    private final HexFormat hex = HexFormat.of();

    public AuthService(UserRepository users) { this.users = users; }

    public void bootstrapIfEmpty(String username, String password) {
        if (users.count() == 0L) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            String token = generateToken();
            users.insert(username, hash, token);
        }
    }

    public Optional<User> authenticatePassword(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return users.findByUsername(username)
                .filter(u -> BCrypt.checkpw(password, u.passwordHash()));
    }

    public Optional<User> authenticateToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return users.findByToken(token);
    }

    public String issueNewToken(User u) {
        String token = generateToken();
        users.updateToken(u.id(), token);
        return token;
    }

    private String generateToken() {
        byte[] buf = new byte[24];
        rnd.nextBytes(buf);
        return hex.formatHex(buf);
    }

    public record RegisterResult(User user, String token) {}

    public RegisterResult register(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("username is required");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("password must be at least 6 chars");

        if (users.existsByUsername(username))
            throw new IllegalStateException("username already taken");

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String token = generateToken();
        User created = users.insert(username, hash, token);
        return new RegisterResult(created, token);
    }
}
