package com.funit.claudegui.controller;

import com.funit.claudegui.model.dto.ClaudeAnalysisRequest;
import com.funit.claudegui.model.dto.ClaudeAnalysisResponse;
import com.funit.claudegui.model.dto.ClaudeStatusResponse;
import com.funit.claudegui.service.ClaudeSimpleService;
import com.funit.claudegui.service.ClaudeMcpService;
import com.funit.claudegui.service.WebSocketOutboundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST API for Claude Code integration
 * Supports both simple and MCP (conversation) modes
 */
@RestController
@RequestMapping("/api/claude")
@CrossOrigin(origins = "http://localhost:4200")
public class ClaudeController {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeController.class);

    @Autowired
    private ClaudeSimpleService claudeSimpleService;

    @Autowired
    private ClaudeMcpService claudeMcpService;

    @Autowired
    private WebSocketOutboundService webSocketService;

    /**
     * Analyze using simple mode (no conversation)
     * POST /api/claude/analyze-simple
     */
    @PostMapping("/analyze-simple")
    public ResponseEntity<ClaudeAnalysisResponse> analyzeSimple(
            @RequestBody ClaudeAnalysisRequest request) {

        logger.info("Simple analysis request: prompt='{}'", request.getPrompt());

        // Validate input
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            ClaudeAnalysisResponse errorResponse = new ClaudeAnalysisResponse();
            errorResponse.setError("Prompt is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Generate analysis ID
        String analysisId = UUID.randomUUID().toString();
        ClaudeAnalysisResponse response = new ClaudeAnalysisResponse(analysisId);
        response.setMode("simple");

        // Execute asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();

                webSocketService.sendClaudeProgress(analysisId, "Starting analysis...");

                // Execute Claude analysis
                String result = claudeSimpleService.executeAnalysis(request.getPrompt());

                long duration = System.currentTimeMillis() - startTime;

                // Send completion via WebSocket
                webSocketService.sendClaudeComplete(analysisId, result, duration);

            } catch (Exception e) {
                logger.error("Error in simple analysis: {}", e.getMessage(), e);
                webSocketService.sendClaudeError(analysisId, e.getMessage());
            }
        });

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Analyze using MCP mode (conversation support)
     * POST /api/claude/analyze-mcp
     */
    @PostMapping("/analyze-mcp")
    public ResponseEntity<ClaudeAnalysisResponse> analyzeMcp(
            @RequestBody ClaudeAnalysisRequest request) {

        logger.info("MCP analysis request: prompt='{}', conversationId={}",
                   request.getPrompt(), request.getConversationId());

        // Validate input
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            ClaudeAnalysisResponse errorResponse = new ClaudeAnalysisResponse();
            errorResponse.setError("Prompt is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Generate analysis ID (use existing conversationId if provided)
        String analysisId = request.getConversationId() != null
            ? request.getConversationId()
            : UUID.randomUUID().toString();

        ClaudeAnalysisResponse response = new ClaudeAnalysisResponse(analysisId);
        response.setMode("mcp");
        response.setConversationId(analysisId);

        // Execute asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();

                webSocketService.sendClaudeProgress(analysisId, "Starting conversation...");

                // Execute Claude MCP analysis
                String result = claudeMcpService.executeAnalysis(
                    request.getPrompt(),
                    analysisId,
                    request.getConversationId()
                );

                long duration = System.currentTimeMillis() - startTime;

                // Send completion via WebSocket
                webSocketService.sendClaudeComplete(analysisId, result, duration);

            } catch (Exception e) {
                logger.error("Error in MCP analysis: {}", e.getMessage(), e);
                webSocketService.sendClaudeError(analysisId, e.getMessage());
            }
        });

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get Claude Code status
     * GET /api/claude/status
     */
    @GetMapping("/status")
    public ResponseEntity<ClaudeStatusResponse> getStatus() {
        ClaudeStatusResponse status = new ClaudeStatusResponse();

        boolean claudeAvailable = claudeSimpleService.isClaudeAvailable();
        status.setClaudeAvailable(claudeAvailable);
        status.setSimpleMode(claudeAvailable);
        status.setMcpMode(claudeAvailable);

        return ResponseEntity.ok(status);
    }
}
