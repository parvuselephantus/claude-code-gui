package com.funit.claudegui.model.dto;

public class ClaudeStatusResponse {
    private boolean claudeAvailable;
    private boolean simpleMode;
    private boolean mcpMode;
    private String error;

    public ClaudeStatusResponse() {
    }

    public boolean isClaudeAvailable() {
        return claudeAvailable;
    }

    public void setClaudeAvailable(boolean claudeAvailable) {
        this.claudeAvailable = claudeAvailable;
    }

    public boolean isSimpleMode() {
        return simpleMode;
    }

    public void setSimpleMode(boolean simpleMode) {
        this.simpleMode = simpleMode;
    }

    public boolean isMcpMode() {
        return mcpMode;
    }

    public void setMcpMode(boolean mcpMode) {
        this.mcpMode = mcpMode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
