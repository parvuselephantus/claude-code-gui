package com.funit.claudegui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending WebSocket messages to clients
 */
@Service
public class WebSocketOutboundService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketOutboundService.class);

    private static final String TOPIC_CLAUDE_PROGRESS = "/topic/claude_analysis_progress/";
    private static final String TOPIC_CLAUDE_COMPLETE = "/topic/claude_analysis_complete/";
    private static final String TOPIC_CLAUDE_ERROR = "/topic/claude_analysis_error/";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send Claude analysis progress update
     */
    public void sendClaudeProgress(String analysisId, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend(TOPIC_CLAUDE_PROGRESS + analysisId, data);
    }

    /**
     * Send Claude analysis completion
     */
    public void sendClaudeComplete(String analysisId, String result, Long durationMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("analysisId", analysisId);
        data.put("result", result);
        data.put("completed", true);
        data.put("durationMs", durationMs);
        data.put("timestamp", System.currentTimeMillis());
        String topic = TOPIC_CLAUDE_COMPLETE + analysisId;
        logger.info("Sending Claude completion to topic: {}", topic);
        messagingTemplate.convertAndSend(topic, data);
        logger.info("Claude completion sent successfully");
    }

    /**
     * Send Claude analysis error
     */
    public void sendClaudeError(String analysisId, String error) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", error);
        data.put("failed", true);
        data.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend(TOPIC_CLAUDE_ERROR + analysisId, data);
    }
}
