package com.funit.claudegui.controller;

import com.funit.claudegui.config.GuiConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {
    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
    private static final String CONFIG_FILE = "claude-gui.config.json";

    @Autowired
    private GuiConfiguration guiConfiguration;

    @GetMapping("/domain")
    public ResponseEntity<Map<String, Object>> getDomainConfig() {
        return ResponseEntity.ok(Map.of(
            "domainName", guiConfiguration.getDomainName(),
            "frontendPort", guiConfiguration.getFrontendPort()
        ));
    }

    @PostMapping("/domain")
    public ResponseEntity<Map<String, String>> updateDomainName(@RequestBody Map<String, String> request) {
        String newDomain = request.get("domainName");

        if (newDomain == null || newDomain.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Domain name cannot be empty"));
        }

        try {
            // Update in-memory configuration
            guiConfiguration.setDomainName(newDomain);

            // Save to config file
            saveConfigFile(newDomain);

            log.info("Domain name updated to: {}", newDomain);
            return ResponseEntity.ok(Map.of("message", "Domain name updated successfully"));
        } catch (IOException e) {
            log.error("Failed to save configuration", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save configuration"));
        }
    }

    private void saveConfigFile(String domainName) throws IOException {
        // Try to load from current directory first
        File configFile = new File(CONFIG_FILE);

        // If not found, try parent directory (in case we're in backend/)
        if (!configFile.exists()) {
            configFile = new File("../" + CONFIG_FILE);
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root;

        // Read existing config or create new
        if (configFile.exists()) {
            root = (ObjectNode) mapper.readTree(configFile);
        } else {
            root = mapper.createObjectNode();
            // Add default values
            ObjectNode backend = root.putObject("backend");
            backend.put("port", guiConfiguration.getBackendPort());
            backend.put("host", guiConfiguration.getBackendHost());

            ObjectNode frontend = root.putObject("frontend");
            frontend.put("port", guiConfiguration.getFrontendPort());
            frontend.put("host", guiConfiguration.getFrontendHost());

            root.put("projectRoot", guiConfiguration.getProjectRoot());
            root.put("autoOpenBrowser", guiConfiguration.isAutoOpenBrowser());
        }

        // Update domain name
        root.put("domainName", domainName);

        // Write back to file
        mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, root);
    }
}
