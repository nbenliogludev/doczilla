package com.nbenliogludev.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

/**
 * @author nbenliogludev
 */
public final class LocalFileStorage implements FileStorage {
    private final Path baseDir;

    public LocalFileStorage(String baseDir) {
        this.baseDir = Path.of(baseDir);
    }

    @Override
    public String save(java.util.UUID id, InputStream data) throws IOException {
        Files.createDirectories(baseDir);
        Path dest = baseDir.resolve(id.toString());
        // Replace existing to allow re-uploads with same ID (unlikely but safe).
        Files.copy(data, dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    @Override
    public InputStream open(String storagePath) throws IOException {
        return Files.newInputStream(Path.of(storagePath));
    }

    @Override
    public boolean delete(String storagePath) throws IOException {
        return Files.deleteIfExists(Path.of(storagePath));
    }
}
