package de.uni_freiburg.informatik.ultimate.intellij.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job.UltimateJob;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting.PluginSetting;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.UltimateVersion;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.status.JobInfo;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.status.JobStatus;
import de.uni_freiburg.informatik.ultimate.intellij.config.UltimateSettingsState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static de.uni_freiburg.informatik.ultimate.intellij.window.tab.DebugConsoleTab.logToDebugConsole;

public class UltimateApiService implements Disposable {

    private final ApiClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentMap<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();

    public UltimateApiService() {
        this.client = new ApiClient(UltimateSettingsState.getInstance().getApiUrl());
    }

    public PluginSetting[] getDefaultSettings() {
        try (InputStream inputStream = UltimateApiService.class.getClassLoader().getResourceAsStream("cAutomizer.json")) {
            if (inputStream == null) {
                logToDebugConsole("Resource cAutomizer.json not found", ConsoleViewContentType.ERROR_OUTPUT);
                return new PluginSetting[0];
            }
            return mapper.readValue(inputStream, PluginSetting[].class);
        } catch (IOException e) {
            logToDebugConsole(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
            return new PluginSetting[0];
        }
    }

    public String getDefaultToolchain() {
        try (InputStream inputStream = UltimateApiService.class.getClassLoader().getResourceAsStream("cAutomizer.xml")) {
            if (inputStream == null) {
                logToDebugConsole("Resource cAutomizer.xml not found", ConsoleViewContentType.ERROR_OUTPUT);
                return null;
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logToDebugConsole(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
            return null;
        }
    }

    public CompletableFuture<UltimateVersion> getVersion() {
        return getVersion(client);
    }

    public CompletableFuture<UltimateVersion> getVersion(String baseUrl) {
        ApiClient temp = new ApiClient(baseUrl);
        return getVersion(temp);
    }

    private CompletableFuture<UltimateVersion> getVersion(ApiClient apiClient) {
        return apiClient.get("/version").thenApply(response -> {
            if (response.statusCode() != 200) {
                logToDebugConsole("GET /version returned status " + response.statusCode(), ConsoleViewContentType.ERROR_OUTPUT);
                return null;
            }
            try {
                return mapper.readValue(response.body(), UltimateVersion.class);
            } catch (JsonProcessingException e) {
                logToDebugConsole(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
                return null;
            }
        });
    }

    public CompletableFuture<JobInfo> queueJob(UltimateJob job) {
        logToDebugConsole("Queued " + job.toString().replaceAll("[\\n\\r]", ""), ConsoleViewContentType.LOG_DEBUG_OUTPUT);
        Map<String, String> formData = new HashMap<>();

        try {
            formData.put("action", job.getAction().getAction());
            formData.put("code", job.getCode());
            formData.put("toolchain[id]", job.getToolchainId());
            formData.put("code_file_extension", job.getCodeFileExtension());
            formData.put("user_settings", mapper.writeValueAsString(job.getUserSettings().pluginSettings));
            formData.put("ultimate_toolchain_xml", job.getToolchain());
        } catch (JsonProcessingException e) {
            logToDebugConsole(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
            return CompletableFuture.failedFuture(e);
        }

        return client.postForm("", formData).thenCompose(response -> {
            if (response.statusCode() != 200) {
                logToDebugConsole("queueJob returned status " + response.statusCode(), ConsoleViewContentType.ERROR_OUTPUT);
                return CompletableFuture.failedFuture(new IllegalStateException());
            }

            try {
                JobInfo info = mapper.readValue(response.body(), JobInfo.class);
                return CompletableFuture.completedFuture(info);
            } catch (JsonProcessingException e) {
                logToDebugConsole(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    public CompletableFuture<JobInfo> pollJob(String id) {
        return client.get("/job/get/" + id).thenCompose(response -> {
            if (response.statusCode() != 200) {
                logToDebugConsole("pollJob(" + id + ") returned status " + response.statusCode(), ConsoleViewContentType.ERROR_OUTPUT);
                return CompletableFuture.failedFuture(new IllegalStateException());
            }

            try {
                JobInfo info = mapper.readValue(response.body(), JobInfo.class);
                return CompletableFuture.completedFuture(info);
            } catch (JsonProcessingException e) {
                logToDebugConsole("pollJob(" + id + ") failed: " + e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    public void startJobPollingTask(String jobId, long intervalSeconds, Consumer<JobInfo> onDone, Consumer<Throwable> onError) {
        if (jobs.containsKey(jobId)) return;

        Runnable[] runnerRef = new Runnable[1];
        Runnable runner = () -> pollJob(jobId).thenAccept(job -> {
            if (job == null) return;
            logToDebugConsole("Polled job: " + job.toString().replaceAll("[\\n\\r]", ""), ConsoleViewContentType.LOG_DEBUG_OUTPUT);
            if (job.status() != JobStatus.SCHEDULED) {
                stopJobPolling(jobId);
                onDone.accept(job);
            } else {
                // schedule the next poll request only after this one completed
                ScheduledFuture<?> next = scheduler.schedule(runnerRef[0], intervalSeconds, TimeUnit.SECONDS);
                jobs.put(jobId, next);
            }
        }).exceptionally(ex -> {
            logToDebugConsole("Polling error for job " + jobId + ": " + ex.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
            stopJobPolling(jobId);
            if (onError != null) onError.accept(ex);
            return null;
        });

        runnerRef[0] = runner;

        ScheduledFuture<?> first = scheduler.schedule(runner, 0, TimeUnit.SECONDS);
        jobs.put(jobId, first);
    }

    public void stopJobPolling(String jobId) {
        ScheduledFuture<?> task = jobs.remove(jobId);
        if (task != null) {
            task.cancel(true);
        }
    }

    public void setBaseUrl(String baseUrl) {
        client.setBaseUrl(baseUrl);
    }

    @Override
    public void dispose() {
        // Stop all polling tasks and shutdown executor. Scheduler will leak resources if the plugin is disabled otherwise.
        jobs.keySet().forEach(this::stopJobPolling);
        scheduler.shutdownNow();
    }

}
