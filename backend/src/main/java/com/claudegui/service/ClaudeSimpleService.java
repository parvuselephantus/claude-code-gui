package com.claudegui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Simple Claude integration - spawns process with prompt
 * No MCP, no conversation state
 */
@Service
public class ClaudeSimpleService {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeSimpleService.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Execute a simple Claude analysis
     */
    public String executeAnalysis(String userPrompt) throws Exception {
        logger.info("Executing simple Claude analysis for prompt: {}", userPrompt);

        // Execute Claude Code process
        String jsonResult = executeClaudeProcess(userPrompt);

        // Parse and extract result
        return parseClaudeResult(jsonResult);
    }

    /**
     * Execute Claude Code process and capture output
     */
    private String executeClaudeProcess(String prompt) throws Exception {
        long startTime = System.currentTimeMillis();

        ProcessBuilder pb = new ProcessBuilder(
            "claude",
            "--print",
            "--output-format", "json"
        );

        pb.redirectErrorStream(false);

        logger.info("Starting Claude process...");
        Process process = pb.start();

        // Write prompt to stdin
        process.getOutputStream().write(prompt.getBytes());
        process.getOutputStream().close();

        // Read stdout (JSON response)
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Read stderr (logs)
        StringBuilder errors = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errors.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        long duration = System.currentTimeMillis() - startTime;

        if (exitCode != 0) {
            logger.error("Claude process failed with exit code: {}", exitCode);
            logger.error("Stderr: {}", errors.toString());
            throw new RuntimeException("Claude process failed: " + errors.toString());
        }

        logger.info("Claude process completed in {}ms", duration);

        String result = output.toString();
        if (result.isEmpty()) {
            throw new RuntimeException("Claude returned empty response");
        }

        return result;
    }

    /**
     * Parse Claude JSON response and extract result text
     */
    private String parseClaudeResult(String jsonOutput) throws Exception {
        JsonNode root = objectMapper.readTree(jsonOutput);

        // Handle error response
        if (root.has("type") && "error".equals(root.get("type").asText())) {
            String error = root.has("error") ? root.get("error").asText() : "Unknown error";
            throw new RuntimeException("Claude error: " + error);
        }

        // Extract result text
        if (root.has("result")) {
            return root.get("result").asText();
        }

        throw new RuntimeException("Unexpected Claude response format: " + jsonOutput);
    }

    /**
     * Check if Claude Code is available on the system
     */
    public boolean isClaudeAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("claude", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.warn("Claude Code not available: {}", e.getMessage());
            return false;
        }
    }
}
