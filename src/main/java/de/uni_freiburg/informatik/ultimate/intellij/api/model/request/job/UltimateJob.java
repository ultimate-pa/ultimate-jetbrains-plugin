package de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job;

public class UltimateJob {

    private final JobAction action;
    private final String code;
    private final String toolchainId;
    private final String codeFileExtension;
    private final JobSettings userSettings;
    private final String toolchain;

    public UltimateJob(JobAction action, String code, String toolchainId, String codeFileExtension, JobSettings userSettings, String toolchain) {
        this.action = action;
        this.code = code;
        this.toolchainId = toolchainId;
        this.codeFileExtension = codeFileExtension;
        this.userSettings = userSettings;
        this.toolchain = toolchain;
    }

    public JobAction getAction() {
        return action;
    }

    public String getCode() {
        return code;
    }

    public String getToolchainId() {
        return toolchainId;
    }

    public String getCodeFileExtension() {
        return codeFileExtension;
    }

    public JobSettings getUserSettings() {
        return userSettings;
    }

    public String getToolchain() {
        return toolchain;
    }

    @Override
    public String toString() {
        return "UltimateJob{" +
                "action=" + action +
                ", code='" + code + '\'' +
                ", toolchainId='" + toolchainId + '\'' +
                ", codeFileExtension='" + codeFileExtension + '\'' +
                ", userSettings=" + userSettings +
                ", toolchain='" + toolchain + '\'' +
                '}';
    }
}
