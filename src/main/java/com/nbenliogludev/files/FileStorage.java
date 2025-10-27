package com.nbenliogludev.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author nbenliogludev
 */
public interface FileStorage {
    String save(UUID id, InputStream data) throws IOException;

    InputStream open(String storagePath) throws IOException;

    boolean delete(String storagePath) throws IOException;
}