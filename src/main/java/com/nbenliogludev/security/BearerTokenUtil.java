package com.nbenliogludev.security;

import java.util.Optional;

/**
 * @author nbenliogludev
 */
public class BearerTokenUtil {
    public static Optional<String> parseBearer(String authorizationHeader) {
        if (authorizationHeader == null) return Optional.empty();
        String pfx = "Bearer ";
        if (!authorizationHeader.regionMatches(true, 0, pfx, 0, pfx.length())) return Optional.empty();
        String token = authorizationHeader.substring(pfx.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
