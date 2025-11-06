package com.redhat.sast.api.util.dvc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.redhat.sast.api.exceptions.DvcException;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ProcessExecutor {

    private static final AtomicBoolean isProcessRunning = new AtomicBoolean(false);

    public static void runDvcCommand(String dvcRepoUrl, String batchYamlPath, String version, Path tempFile)
            throws InterruptedException, IOException {

        if (isProcessRunning.get()) {
            throw new DvcException("DVC command is already running...");
        }
        isProcessRunning.set(true);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "dvc", "get", dvcRepoUrl, batchYamlPath, "--rev", version, "-o", tempFile.toString(), "--force");
        Process process = processBuilder.start();
        // read stderr for error messages
        String error = new String(process.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        if (!finished) {
            LOGGER.error("DVC command timed out after 60 seconds");
            throw new DvcException("DVC command timed out after 60 seconds");
        }
        int exitCode = process.exitValue();

        if (exitCode != 0) {
            LOGGER.error("DVC command failed with exit code {}: {}", exitCode, error);
            throw new DvcException("Failed to fetch data from DVC (exit code " + exitCode + "): " + error);
        }

        // force kill process if still running
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        isProcessRunning.set(false);
    }
}
