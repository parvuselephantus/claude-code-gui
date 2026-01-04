package com.claudegui.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class GuiConfiguration {
    private static final Logger log = LoggerFactory.getLogger(GuiConfiguration.class);
    private static final String CONFIG_FILE = "claude-gui.config.json";

    private int backendPort = 8080;
    private String backendHost = "localhost";
    private int frontendPort = 4200;
    private String frontendHost = "localhost";
    private String projectRoot = ".";
    private boolean autoOpenBrowser = true;

    @PostConstruct
    public void loadConfiguration() {
        // Try to load from current directory first
        File configFile = new File(CONFIG_FILE);

        // If not found, try parent directory (in case we're in backend/)
        if (!configFile.exists()) {
            configFile = new File("../" + CONFIG_FILE);
        }

        if (configFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(configFile);

                if (root.has("backend")) {
                    JsonNode backend = root.get("backend");
                    if (backend.has("port")) {
                        backendPort = backend.get("port").asInt();
                    }
                    if (backend.has("host")) {
                        backendHost = backend.get("host").asText();
                    }
                }

                if (root.has("frontend")) {
                    JsonNode frontend = root.get("frontend");
                    if (frontend.has("port")) {
                        frontendPort = frontend.get("port").asInt();
                    }
                    if (frontend.has("host")) {
                        frontendHost = frontend.get("host").asText();
                    }
                }

                if (root.has("projectRoot")) {
                    projectRoot = root.get("projectRoot").asText();
                }

                if (root.has("autoOpenBrowser")) {
                    autoOpenBrowser = root.get("autoOpenBrowser").asBoolean();
                }

                log.info("Loaded configuration from: {}", configFile.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to load configuration file, using defaults: {}", e.getMessage());
            }
        } else {
            log.info("No configuration file found, using defaults");
        }

        log.info("Backend: {}:{}", backendHost, backendPort);
        log.info("Frontend: {}:{}", frontendHost, frontendPort);
        log.info("Project Root: {}", getAbsoluteProjectRoot());
    }

    public int getBackendPort() {
        return backendPort;
    }

    public String getBackendHost() {
        return backendHost;
    }

    public int getFrontendPort() {
        return frontendPort;
    }

    public String getFrontendHost() {
        return frontendHost;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public Path getAbsoluteProjectRoot() {
        return Paths.get(projectRoot).toAbsolutePath().normalize();
    }

    public boolean isAutoOpenBrowser() {
        return autoOpenBrowser;
    }

    public String getFrontendUrl() {
        return String.format("http://%s:%d", frontendHost, frontendPort);
    }
}
