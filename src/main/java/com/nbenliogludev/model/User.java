package com.nbenliogludev.model;

import java.time.LocalDateTime;

/**
 * @author nbenliogludev
 */
public record User(
        long id,
        String username,
        String passwordHash,
        String apiToken,
        LocalDateTime tokenCreatedAt
) {}
