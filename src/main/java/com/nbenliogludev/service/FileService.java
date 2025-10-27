package com.nbenliogludev.service;

import com.nbenliogludev.model.FileRecord;
import com.nbenliogludev.repository.FileRepository;
import com.nbenliogludev.files.FileStorage;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * @author nbenliogludev
 */
public class FileService {
    private final FileRepository repo;
    private final FileStorage storage;

    public FileService(FileRepository repo, FileStorage storage) {
        this.repo = repo;
        this.storage = storage;
    }

    public FileRecord store(String name, String ct, long size, InputStream data) throws IOException {
        UUID id = UUID.randomUUID();
        String token = UUID.randomUUID().toString().replace("-", "");
        String path = storage.save(id, data);
        FileRecord rec = new FileRecord(
                id, name, ct != null ? ct : "application/octet-stream",
                size, path, token, LocalDateTime.now(), null, 0
        );
        repo.insert(rec);
        return rec;
    }

    public Optional<FileRecord> findByToken(String token) { return repo.findByToken(token); }

    public void touchDownload(UUID id) { repo.touchDownload(id); }

    public boolean delete(FileRecord r) throws IOException {
        boolean ok = storage.delete(r.storagePath());
        repo.deleteById(r.id());
        return ok;
    }
}
