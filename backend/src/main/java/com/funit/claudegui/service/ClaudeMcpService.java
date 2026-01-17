package com.funit.claudegui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing Claude Code CLI with conversation support
 * MCP mode without external server - just conversation history
 */
@Service
public class ClaudeMcpService {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeMcpService.class);

    @Autowired
    private WebSocketOutboundService webSocketService;

    // Conversation storage: conversationId -> List of messages
    private final Map<String, List<ConversationMessage>> conversations = new ConcurrentHashMap<>();

    public static class ConversationMessage {
        public String role;
        public String content;

        public ConversationMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * Execute analysis with conversation support
     */
    public String executeAnalysis(String prompt, String analysisId, String conversationId) {
        logger.info("Starting MCP analysis: analysisId={}, conversationId={}", analysisId, conversationId);

        try {
            String userPrompt = prompt;
            String enhancedPrompt = buildPromptWithHistory(userPrompt, conversationId);

            webSocketService.sendClaudeProgress(analysisId, "Initializing Claude...");

            ProcessBuilder pb = new ProcessBuilder(
                "claude",
                "--print",
                "--output-format", "text",
                "--dangerously-skip-permissions"
            );

            pb.redirectErrorStream(false);

            Process process = pb.start();

            try (BufferedWriter stdinWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                stdinWriter.write(enhancedPrompt);
                stdinWriter.newLine();
                stdinWriter.flush();
            } catch (IOException e) {
                logger.error("Failed to write prompt to stdin: {}", e.getMessage());
                throw new RuntimeException("Failed to write prompt to Claude: " + e.getMessage(), e);
            }

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder result = new StringBuilder();
            StringBuilder errors = new StringBuilder();

            Thread stdoutThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdoutReader.readLine()) != null) {
                        result.append(line).append("\n");
                        String preview = line.substring(0, Math.min(100, line.length()));
                        webSocketService.sendClaudeProgress(analysisId, "Processing: " + preview);
                    }
                } catch (IOException e) {
                    logger.error("Error reading stdout: {}", e.getMessage());
                }
            });

            Thread stderrThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stderrReader.readLine()) != null) {
                        errors.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading stderr: {}", e.getMessage());
                }
            });

            stdoutThread.start();
            stderrThread.start();

            boolean finished = process.waitFor(10, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                stdoutThread.join(2000);
                stderrThread.join(2000);
                throw new RuntimeException("Claude process timed out after 10 minutes");
            }

            stdoutThread.join(5000);
            stderrThread.join(5000);

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new RuntimeException("Claude process failed with exit code " + exitCode + ". Stderr: " + errors.toString());
            }

            String resultText = result.toString().trim();
            if (resultText.isEmpty()) {
                throw new RuntimeException("Claude returned empty response");
            }

            // Store conversation history
            String conversationKey = (conversationId != null && !conversationId.isEmpty()) ? conversationId : analysisId;
            conversations.computeIfAbsent(conversationKey, k -> new ArrayList<>());
            List<ConversationMessage> history = conversations.get(conversationKey);
            history.add(new ConversationMessage("user", userPrompt));
            history.add(new ConversationMessage("assistant", resultText));

            return resultText;

        } catch (Exception e) {
            logger.error("MCP analysis error: {}", e.getMessage(), e);
            throw new RuntimeException("MCP analysis failed: " + e.getMessage(), e);
        }
    }

    private String buildPromptWithHistory(String userPrompt, String conversationId) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a helpful AI assistant.\n\n");

        if (conversationId != null && !conversationId.isEmpty()) {
            List<ConversationMessage> history = conversations.get(conversationId);
            if (history != null && !history.isEmpty()) {
                prompt.append("CONVERSATION HISTORY:\n");
                for (ConversationMessage msg : history) {
                    if ("user".equals(msg.role)) {
                        prompt.append("User: ").append(msg.content).append("\n");
                    } else {
                        prompt.append("Assistant: ").append(msg.content).append("\n");
                    }
                }
                prompt.append("\n");
            }
        }

        prompt.append("USER REQUEST:\n");
        prompt.append(userPrompt).append("\n");

        return prompt.toString();
    }

    /**
     * Clear conversation history for a given conversation ID
     */
    public void clearConversation(String conversationId) {
        if (conversationId != null) {
            conversations.remove(conversationId);
            logger.info("Cleared conversation: {}", conversationId);
        }
    }

    /**
     * Get conversation history
     */
    public List<ConversationMessage> getConversationHistory(String conversationId) {
        return conversations.getOrDefault(conversationId, new ArrayList<>());
    }
}
