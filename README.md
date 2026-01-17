# Claude Code GUI

Web GUI for Claude Code CLI with two usage modes:
1. **Standalone**: Independent web app
2. **Library**: Integrate into existing Java/Angular projects

## Quick Start (Standalone)

### Linux/Mac
```bash
git clone https://github.com/parvuselephantus/claude-code-gui.git
cd claude-code-gui
./install.sh
./start.sh
```

### Windows
```cmd
git clone https://github.com/parvuselephantus/claude-code-gui.git
cd claude-code-gui
install.bat
start.bat
```

### Docker
```bash
docker-compose up
```

Open http://localhost:4200 in your browser.

---

## Installation Methods

### Method 1: Standalone Clone
```bash
git clone https://github.com/parvuselephantus/claude-code-gui.git
cd claude-code-gui
./install.sh    # or install.bat on Windows
```

### Method 2: Git Submodule
```bash
cd your-project
git submodule add https://github.com/parvuselephantus/claude-code-gui.git .claude-gui
cd .claude-gui
./install.sh
```

### Method 3: Clone into Project
```bash
cd your-project
git clone https://github.com/parvuselephantus/claude-code-gui.git .claude-gui
echo ".claude-gui/" >> .gitignore
cd .claude-gui
./install.sh
```

---

## Configuration

### Application Configuration

Copy the template to create your config:
```bash
cp claude-gui-template.config.json claude-gui.config.json
```

Edit `claude-gui.config.json`:
```json
{
  "backend": {
    "port": 8080,
    "host": "localhost"
  },
  "frontend": {
    "port": 4200,
    "host": "localhost"
  },
  "projectRoot": ".",
  "autoOpenBrowser": true,
  "domainName": "localhost"
}
```

**Configuration Options:**
- `backend.port` - Backend server port (default: 8080)
- `backend.host` - Backend hostname (default: localhost)
- `frontend.port` - Frontend dev server port (default: 4200)
- `frontend.host` - Frontend hostname (default: localhost)
- `projectRoot` - Root directory for project files (default: ".")
- `autoOpenBrowser` - Automatically open browser on startup (default: true)
- `domainName` - Domain name for the application (default: "localhost")

### Custom Domain Configuration

To use a custom domain name (e.g., for local network access):

1. **Update `claude-gui.config.json`:**
   ```json
   {
     "backend": {
       "port": 8080,
       "host": "myapp"
     },
     "frontend": {
       "port": 4200,
       "host": "myapp"
     },
     "domainName": "myapp"
   }
   ```

2. **Add domain to hosts file:**
   - **Linux/Mac:** Add `127.0.0.1 myapp` to `/etc/hosts`
   - **Windows:** Add `127.0.0.1 myapp` to `C:\Windows\System32\drivers\etc\hosts`
   - For network access, use your computer's IP instead of `127.0.0.1`

3. **Update Angular configuration:**
   - Edit `frontend/angular.json`
   - Add your domain to the `allowedHosts` array under `serve` → `options`:
     ```json
     "serve": {
       "builder": "@angular/build:dev-server",
       "options": {
         "allowedHosts": ["localhost", "myapp"]
       }
     }
     ```

4. **Restart both services**

### Local Environment (Optional)

If Maven, Java, or Node.js are not in your PATH, create a local environment file:

```bash
cp claude-gui-local.template.sh claude-gui.local.sh
```

Edit `claude-gui.local.sh` to set your paths:
```bash
# Maven path
export MAVEN_HOME="$HOME/apache-maven-3.9.6"
export PATH="$MAVEN_HOME/bin:$PATH"

# Java path (if needed)
export JAVA_HOME="$HOME/jdk-17.0.2"
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## Requirements

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)
  - Optional if backend jar is pre-built (e.g., from git clone with included jar)
  - Install script checks for Maven in PATH, Maven Wrapper (mvnw), and common locations
- **Node.js 18+** - [Download](https://nodejs.org/)
- **npm** - Comes with Node.js

---

## Usage

### Start
```bash
./start.sh    # Linux/Mac
start.bat     # Windows
```

### Stop
Press `Ctrl+C` in the terminal

### View Logs
```bash
tail -f backend.log
tail -f frontend.log
```

---

## Features

- **Simple Mode** - One-shot analysis, no conversation history
- **MCP Mode** - Multi-turn conversations with context
- **Real-time Updates** - WebSocket-based progress streaming
- **Configurable Ports** - Avoid conflicts easily
- **Multiple Installation Options** - Submodule, clone, or Docker

---

## Project Structure

```
claude-code-gui/
├── backend/                    # Spring Boot backend (port 8080)
├── frontend/                   # Angular 20 frontend (port 4200)
├── docker/                     # Docker configuration
├── install.sh / install.bat    # Dependency installation
├── start.sh / start.bat        # Startup scripts
├── claude-gui-template.config.json
├── docker-compose.yml
└── README.md
```

---

## Updating

```bash
git pull
./install.sh    # Reinstall if dependencies changed
```

For submodules:
```bash
git submodule update --remote
```

---

## Troubleshooting

### Port Already in Use
Edit `claude-gui.config.json` to change ports:
```json
{
  "backend": { "port": 9090 },
  "frontend": { "port": 3000 }
}
```

### Dependencies Not Found
Run the install script manually:
```bash
./install.sh    # Linux/Mac
install.bat     # Windows
```

### Browser Doesn't Open
Set `autoOpenBrowser: false` in config and open manually:
```
http://localhost:4200
```

---

---

## Library Usage

### Maven Dependency (Backend)

Build and install locally:
```bash
cd backend
mvn clean install
```

Add to your `pom.xml`:
```xml
<dependency>
    <groupId>com.claudegui</groupId>
    <artifactId>claude-code-gui</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/../../claude_code_gui/backend/target/claude-code-gui-1.0.0.jar</systemPath>
</dependency>
```

Use services:
```java
@Autowired
private ClaudeMcpService claudeMcpService;

@Autowired
private ClaudeSimpleService claudeSimpleService;
```

### NPM Package (Frontend)

Build library:
```bash
cd frontend
ng build claude-code-gui-lib --configuration production
cd dist/claude-code-gui-lib
npm pack
```

Install in your project:
```bash
npm install path/to/claude-code-gui-lib-0.0.1.tgz
```

Import components:
```typescript
import { ClaudeChatComponent, SettingsComponent, ClaudeService } from 'claude-code-gui-lib';
```

See example integration in [Stock project](https://github.com/parvuselephantus/stock).

---

## Development

See [CLAUDE.md](CLAUDE.md) for architecture details and development guidelines.

---

## License

Apache 2.0
