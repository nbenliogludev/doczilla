package com.nbenliogludev.repository;

import com.nbenliogludev.db.Db;
import com.nbenliogludev.model.FileRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author nbenliogludev
 */
public class FileRepository {
    private final Db db;
    public FileRepository(Db db) { this.db = db; }

    public void insert(FileRecord rec) {
        db.jdbi().useHandle(h -> h.createUpdate("""
            insert into files (id, original_name, content_type, size_bytes, storage_path,
                               download_token, created_at, last_download_at, download_count)
            values (:id, :n, :ct, :sz, :p, :t, :ca, :lda, :dc)
        """)
                .bind("id", rec.id())
                .bind("n", rec.originalName())
                .bind("ct", rec.contentType())
                .bind("sz", rec.sizeBytes())
                .bind("p", rec.storagePath())
                .bind("t", rec.downloadToken())
                .bind("ca", rec.createdAt())
                .bind("lda", rec.lastDownloadAt())
                .bind("dc", rec.downloadCount())
                .execute());
    }

    public Optional<FileRecord> findByToken(String token) {
        return db.jdbi().withHandle(h ->
                h.createQuery("""
                select id, original_name, content_type, size_bytes, storage_path, download_token,
                       created_at, last_download_at, download_count
                  from files where download_token = :t
            """)
                        .bind("t", token)
                        .map((rs, c) -> new FileRecord(
                                UUID.fromString(rs.getString("id")),
                                rs.getString("original_name"),
                                rs.getString("content_type"),
                                rs.getLong("size_bytes"),
                                rs.getString("storage_path"),
                                rs.getString("download_token"),
                                rs.getObject("created_at", LocalDateTime.class),
                                rs.getObject("last_download_at", LocalDateTime.class),
                                rs.getLong("download_count")
                        ))
                        .findOne()
        );
    }

    public void touchDownload(UUID id) {
        db.jdbi().useHandle(h -> h.createUpdate("""
            update files
               set last_download_at = current_timestamp,
                   download_count   = download_count + 1
             where id = :id
        """).bind("id", id).execute());
    }

    public int deleteById(UUID id) {
        return db.jdbi().withHandle(h ->
                h.createUpdate("delete from files where id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    public List<FileRecord> findExpired(LocalDateTime cutoff) {
        return db.jdbi().withHandle(h ->
                h.createQuery("""
                select id, original_name, content_type, size_bytes, storage_path, download_token,
                       created_at, last_download_at, download_count
                  from files
                 where coalesce(last_download_at, created_at) < :cutoff
            """)
                        .bind("cutoff", cutoff)
                        .map((rs, c) -> new FileRecord(
                                UUID.fromString(rs.getString("id")),
                                rs.getString("original_name"),
                                rs.getString("content_type"),
                                rs.getLong("size_bytes"),
                                rs.getString("storage_path"),
                                rs.getString("download_token"),
                                rs.getObject("created_at", LocalDateTime.class),
                                rs.getObject("last_download_at", LocalDateTime.class),
                                rs.getLong("download_count")
                        ))
                        .list()
        );
    }
}