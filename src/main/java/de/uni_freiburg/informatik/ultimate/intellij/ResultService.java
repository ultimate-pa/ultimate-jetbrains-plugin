package de.uni_freiburg.informatik.ultimate.intellij;

import com.intellij.openapi.components.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@State(name = "ResultService", storages = @Storage(StoragePathMacros.CACHE_FILE))
public final class ResultService implements PersistentStateComponent<ResultService.State> {

    public static class State {
        public Map<String, List<UltimateResult>> results = new HashMap<>();
    }

    private final State state = new State();

    public void setResults(VirtualFile file, List<UltimateResult> results) {
        state.results.put(file.getPath(), results);
    }

    public List<UltimateResult> getResults(VirtualFile file) {
        if(file == null) return Collections.emptyList();
        return state.results.getOrDefault(file.getPath(), Collections.emptyList());
    }

    public boolean hasResults(VirtualFile file) {
        return (file != null && state.results.containsKey(file.getPath()));
    }

    public Map<VirtualFile, List<UltimateResult>> getAllResults() {
        Map<VirtualFile, List<UltimateResult>> resolved = new HashMap<>();
        for (Map.Entry<String, List<UltimateResult>> entry : state.results.entrySet()) {
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + entry.getKey());
            if (file != null) {
                resolved.put(file, entry.getValue());
            }
        }
        return resolved;
    }

    public void clearResults() {
        state.results.clear();
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(State state) {
        this.state.results = state.results;
        clearResults(); // when attempting to save results, the cache sometimes gets corrupted with invalid UltimateResults. Not sure if this is a local issue
    }
}