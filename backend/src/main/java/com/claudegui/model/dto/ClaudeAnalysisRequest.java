package com.claudegui.model.dto;

public class ClaudeAnalysisRequest {
    private String prompt;
    private String mode; // "simple" or "mcp"
    private String conversationId; // for MCP mode

    public ClaudeAnalysisRequest() {
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
