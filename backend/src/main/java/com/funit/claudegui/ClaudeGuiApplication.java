package com.funit.claudegui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class ClaudeGuiApplication {

    public static void main(String[] args) {
        // Load configuration and set port dynamically
        int port = loadPortFromConfig();
        System.setProperty("server.port", String.valueOf(port));

        SpringApplication.run(ClaudeGuiApplication.class, args);
    }

    private static int loadPortFromConfig() {
        String configFile = "claude-gui.config.json";
        File file = new File(configFile);

        // If not found in current directory, try parent directory
        if (!file.exists()) {
            file = new File("../" + configFile);
        }

        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(file);
                if (root.has("backend") && root.get("backend").has("port")) {
                    return root.get("backend").get("port").asInt();
                }
            } catch (Exception e) {
                System.err.println("Failed to load port from config, using default: " + e.getMessage());
            }
        }

        // Default port
        return 8080;
    }
}
