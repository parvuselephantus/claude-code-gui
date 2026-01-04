# Claude Code GUI

A clean, standalone web GUI for interacting with Claude Code CLI. Simple chat interface with conversation support.

## Quick Start

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
  "autoOpenBrowser": true
}
```

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

## Development

See [CLAUDE.md](CLAUDE.md) for architecture details and development guidelines.

---

## License

Apache 2.0
