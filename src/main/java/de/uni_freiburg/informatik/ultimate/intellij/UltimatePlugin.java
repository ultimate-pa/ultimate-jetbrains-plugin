package de.uni_freiburg.informatik.ultimate.intellij;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FileContentUtil;
import de.uni_freiburg.informatik.ultimate.intellij.api.UltimateApiService;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.ConfiguredPluginSetting;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job.JobAction;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job.JobSettings;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job.UltimateJob;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting.PluginSetting;
import de.uni_freiburg.informatik.ultimate.intellij.config.UltimateSettingsState;
import de.uni_freiburg.informatik.ultimate.intellij.window.WindowFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static de.uni_freiburg.informatik.ultimate.intellij.window.tab.DebugConsoleTab.logToDebugConsole;

@Service
public final class UltimatePlugin {

    private final NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Ultimate");

    private final UltimateApiService apiService = new UltimateApiService();

    public UltimateApiService getApiService() {
        return apiService;
    }

    public void handleAnalysis(Project project, Collection<VirtualFile> files) {
        PluginSetting[] settings = apiService.getDefaultSettings();
        String toolchain = apiService.getDefaultToolchain();
        UltimateSettingsState state = UltimateSettingsState.getInstance();
        Map<String, String> idValueMap = state.getSettingValues();

        List<ConfiguredPluginSetting> config = Arrays.stream(settings).map(s -> {
            String rawValue = idValueMap.get(s.getId());
            Object typedValue;
            if (rawValue == null) {
                typedValue = s.getDefaultValue();
            } else {
                try {
                    typedValue = switch (s.getType()) {
                        case INT -> Integer.parseInt(rawValue);
                        case BOOL -> Boolean.parseBoolean(rawValue);
                        case STRING -> rawValue;
                    };
                } catch (Exception ex) {
                    // Fallback to default on parse errors
                    typedValue = s.getDefaultValue();
                    logToDebugConsole("Invalid setting value for " + s.getId() + ", using default. Error: " + ex.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
                }
            }
            return s.toConfigured(typedValue);
        }).toList();

        if (files == null || files.isEmpty()) {
            notificationGroup.createNotification("No files to analyze", "Please select at least one file.", NotificationType.WARNING).notify(project);
            return;
        }
        if (toolchain == null || toolchain.isBlank()) {
            notificationGroup.createNotification("Toolchain not available", "Default toolchain could not be loaded.", NotificationType.ERROR).notify(project);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Analyzing files", true) {
            private final Map<VirtualFile, String> runningJobIds = new ConcurrentHashMap<>();

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText("Submitting files...");
                logToDebugConsole("Submitting to " + UltimateSettingsState.getInstance().getApiUrl() + " with config: " + config, ConsoleViewContentType.LOG_DEBUG_OUTPUT);

                AtomicInteger completed = new AtomicInteger();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (VirtualFile file : files) {
                    CompletableFuture<Void> future = new CompletableFuture<>();

                    // skip files if the task is already canceled
                    if (indicator.isCanceled()) {
                        future.complete(null);
                        futures.add(future);
                        continue;
                    }

                    // get contents from the file
                    String code = ReadAction.compute(() -> {
                        Document document = FileDocumentManager.getInstance().getDocument(file);
                        return document != null ? document.getText() : null;
                    });

                    if (code == null) {
                        ApplicationManager.getApplication().invokeLater(() -> notificationGroup.createNotification("Cannot read file", "Skipping: " + file.getPresentableUrl(), NotificationType.WARNING).notify(project));
                        updateProgress(indicator, completed.incrementAndGet(), files.size());
                        future.complete(null);
                        futures.add(future);
                        continue;
                    }

                    UltimateJob job = new UltimateJob(JobAction.EXECUTE, code, "cAutomizer", ".c", new JobSettings(config), toolchain);
                    apiService.queueJob(job).thenAccept(jobInfo -> {
                        runningJobIds.put(file, jobInfo.id());

                        if (indicator.isCanceled()) {
                            apiService.stopJobPolling(jobInfo.id());
                            updateProgress(indicator, completed.incrementAndGet(), files.size());
                            future.complete(null);
                            return;
                        }

                        apiService.startJobPollingTask(jobInfo.id(), 5,
                                result -> {
                                    ApplicationManager.getApplication().invokeLater(() -> {
                                        ResultService resultService = ApplicationManager.getApplication().getService(ResultService.class);
                                        resultService.setResults(file, result.results());
                                        FileContentUtil.reparseFiles(project, List.of(file), false);
                                        WindowFactory.update();

                                        runningJobIds.remove(file);
                                        updateProgress(indicator, completed.incrementAndGet(), files.size());
                                        future.complete(null);
                                    });
                                },
                                error -> {
                                    ApplicationManager.getApplication().invokeLater(() -> notificationGroup.createNotification("Polling failed", "Job " + jobInfo.id() + " stopped due to an error: " + error.getMessage(), NotificationType.ERROR).notify(project));

                                    runningJobIds.remove(file);
                                    updateProgress(indicator, completed.incrementAndGet(), files.size());
                                    future.complete(null);
                                });
                    }).exceptionally(ex -> {
                        ApplicationManager.getApplication().invokeLater(() -> notificationGroup.createNotification("Job submission error", ex.getMessage(), NotificationType.ERROR).notify(project));

                        updateProgress(indicator, completed.incrementAndGet(), files.size());
                        future.complete(null);
                        return null;
                    });

                    futures.add(future);
                }

                indicator.setText("Submitted " + files.size() + " file(s). Waiting for Ultimate response...");
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Block for progress indicator
            }

            @Override
            public void onCancel() {
                // Stop polling for all running jobs and keep any results already received
                for (String jobId : runningJobIds.values()) {
                    apiService.stopJobPolling(jobId);
                }
                runningJobIds.clear();
                notificationGroup.createNotification("Analysis cancelled", "Stopped remaining jobs. Displaying available results.", NotificationType.INFORMATION).notify(getProject());
            }
        });
    }

    private void updateProgress(ProgressIndicator indicator, int count, int total) {
        double fraction = (double) count / Math.max(1, total);
        indicator.setFraction(fraction);
        indicator.setText(count == total ? "Analysis complete" : "Finished " + count + " / " + total);
    }

    public static String getPluginVersion() {
        return PluginManagerCore.getPlugin(PluginId.getId("de.uni_freiburg.informatik.ultimate.intellij")).getVersion();
    }
}