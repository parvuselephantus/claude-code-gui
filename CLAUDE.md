# Claude Code GUI - Technical Documentation

## Architecture Decisions

### Overview
```
Frontend (Angular 20)  ←→  Backend (Spring Boot)  ←→  Claude CLI
    Port 4200                  Port 8080              (subprocess)
```

### Configuration System

**Decision**: JSON configuration file with project-level overrides

**File**: `claude-gui.config.json` (user creates from template)

**Structure**:
```json
{
  "backend": { "port": 8080, "host": "localhost" },
  "frontend": { "port": 4200, "host": "localhost" },
  "projectRoot": ".",
  "autoOpenBrowser": true
}
```

**Loading Order**:
1. Backend reads config at startup (GuiConfiguration.java)
2. Startup script reads config and updates frontend/public/config.json
3. Frontend loads config.json at app initialization (ConfigService)

**Port Conflicts**: Fail fast with clear error message

---

## Installation Methods

### 1. Standalone Clone
- User clones repository
- Runs install script
- Runs start script

### 2. Git Submodule
- Added to existing project as `.claude-gui`
- Tracked by parent repository
- User must cd into submodule to start

### 3. Clone into Project
- Cloned into project directory
- Added to .gitignore of parent
- Independent from parent git

### 4. Docker
- Single command: `docker-compose up`
- No local dependencies needed
- Isolated environment

---

## Startup Process

### Linux/Mac (start.sh)
1. Check if dependencies installed (target/, node_modules/)
2. Auto-run install.sh if missing
3. Load claude-gui.config.json (parse with python if available)
4. Update frontend/public/config.json
5. Check ports available (fail if in use)
6. Start backend in background (nohup)
7. Start frontend in background (nohup)
8. Health checks (curl polling)
9. Display URLs
10. Auto-open browser (if enabled)
11. Trap Ctrl+C for cleanup

### Windows (start.bat)
- Similar flow but using Windows commands
- netstat for port checks
- start /min for background processes
- No health checks (complexity)

---

## Frontend Configuration

**Runtime Config**: `public/config.json` loaded at app initialization

**Why Runtime**:
- No rebuild needed when changing backend URL
- Startup script can modify before frontend starts
- Supports dynamic port configuration

**Implementation**:
- ConfigService loads config.json via HTTP
- APP_INITIALIZER ensures config loaded before app starts
- ClaudeService uses ConfigService.getApiUrl()

---

## Backend Configuration

**Spring Boot**: `application.properties` reads SERVER_PORT env var

**Config Loading**: GuiConfiguration.java reads JSON file

**Search Order**:
1. Current directory (`./claude-gui.config.json`)
2. Parent directory (`../claude-gui.config.json`)
3. Defaults if not found

**Why**: Allows backend to run from backend/ directory or project root

---

## Docker Setup

**Multi-stage Build**:
1. Build frontend (node:20-alpine)
2. Build backend (maven + JDK 17)
3. Runtime image (JRE 17 + Node for dev server)

**Why Node in Runtime**: Frontend runs `npm start`, not static files

**Volumes**:
- Config file mounted read-only
- Project directory mounted at /workspace (for Claude context)

**Entrypoint**:
1. Start backend in background
2. Wait for backend ready
3. Update frontend config
4. Start frontend in foreground

---

## Scripts Design

### Auto-install
- Check for target/ and node_modules/
- Run install.sh if missing
- Reduces user steps

### Foreground Mode
- Keeps terminal open with logs
- Ctrl+C stops both services cleanly
- Easy to debug

### Health Checks
- Poll backend /api/claude/status
- Poll frontend root page
- 30s timeout for backend, 60s for frontend
- Clear failure messages

---

## Port Conflict Handling

**Decision**: Fail fast with clear message

**Alternatives Rejected**:
- Auto-increment ports (unpredictable URLs)
- Random ports (hard to remember)

**Implementation**:
- lsof/netstat checks before starting
- Error shows which port and how to fix
- User edits config file

---

## Update Mechanism

**Git Pull**: `git pull` + `./install.sh` if deps changed

**Submodule**: `git submodule update --remote`

**No Auto-Update**: User controls when to update

---

## File Structure

```
claude-code-gui/
├── backend/
│   ├── src/main/java/com/claudegui/
│   │   ├── config/
│   │   │   ├── GuiConfiguration.java    # Config reader
│   │   │   ├── CorsConfig.java
│   │   │   └── ...
│   │   └── ...
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   │   ├── services/
│   │   │   ├── config.service.ts        # Runtime config loader
│   │   │   ├── claude.service.ts
│   │   │   └── websocket.service.ts
│   │   ├── app.config.ts                # APP_INITIALIZER
│   │   └── ...
│   ├── public/
│   │   └── config.json                  # Runtime config (generated)
│   └── package.json
├── docker/
│   └── entrypoint.sh
├── claude-gui-template.config.json      # Template for users
├── install.sh / install.bat
├── start.sh / start.bat
├── Dockerfile
├── docker-compose.yml
└── .gitignore
```

---

## Dependencies

### Backend
- Java 17+
- Maven 3.6+
- Spring Boot 2.7.3
- Jackson 2.18.2 (JSON parsing)

### Frontend
- Node.js 18+
- npm
- Angular 20
- Angular Material 20
- RxJS 7.x
- @stomp/stompjs, sockjs-client

---

## Key Design Principles

1. **Multiple Installation Options** - Flexibility for different use cases
2. **Configuration File** - Single source of truth for ports/settings
3. **Fail Fast** - Clear errors, don't silently use fallbacks
4. **Auto-install** - Reduce manual steps
5. **Foreground Logs** - Easy debugging
6. **No Pre-built Artifacts** - User builds locally
7. **Runtime Frontend Config** - No rebuild needed

---

## Future Considerations

### Potential Enhancements
- PowerShell script for modern Windows
- Health check API endpoint for Docker
- Config validation on startup
- Multiple config file support (dev, prod)
- Shell script for global installation

### Known Limitations
- Windows batch has no health checks
- JSON parsing in bash requires python
- Docker image includes dev server (large)
- No automatic port finding

---

## Changelog

### v1.0.0
- Initial implementation
- Multi-platform installation scripts
- Configuration file support
- Docker setup
- Runtime frontend configuration
- Auto-install on startup
