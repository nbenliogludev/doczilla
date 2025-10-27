package com.nbenliogludev.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author nbenliogludev
 */
public record FileRecord(
        UUID id,
        String originalName,
        String contentType,
        long sizeBytes,
        String storagePath,
        String downloadToken,
        LocalDateTime createdAt,
        LocalDateTime lastDownloadAt,
        long downloadCount
) {}
