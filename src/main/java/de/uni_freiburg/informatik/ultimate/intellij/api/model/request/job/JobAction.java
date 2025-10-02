package de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job;

public enum JobAction {

    EXECUTE("execute");

    private final String action;

    JobAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
