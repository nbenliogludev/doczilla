package com.nbenliogludev.tasks;

import com.nbenliogludev.service.FileService;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author nbenliogludev
 */
public final class CleanupJob {
    private static final ScheduledExecutorService EXEC =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "doczilla-cleanup");
                t.setDaemon(true);
                return t;
            });

    private CleanupJob() {}

    public static void start(FileService files, int retentionDays) {
        Runnable task = () -> {
            try {
                int n = files.cleanupExpired(retentionDays);
                if (n > 0) System.out.println("[doczilla] cleanup removed " + n + " expired file(s)");
            } catch (Throwable t) {
                System.err.println("[doczilla] cleanup failed: " + t.getMessage());
            }
        };

        EXEC.scheduleAtFixedRate(task, 60, Duration.ofHours(6).toSeconds(), TimeUnit.SECONDS);
    }
}
