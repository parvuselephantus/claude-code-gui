#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "════════════════════════════════════════════════════════"
echo "  Claude Code GUI"
echo "════════════════════════════════════════════════════════"
echo ""

# Check if dependencies are installed
if [ ! -d "backend/target" ] || [ ! -d "frontend/node_modules" ]; then
    echo "Dependencies not found. Running installation..."
    echo ""
    ./install.sh
    echo ""
fi

# Load configuration
BACKEND_PORT=8080
FRONTEND_PORT=4200
BACKEND_HOST="localhost"
FRONTEND_HOST="localhost"
AUTO_OPEN=true

if [ -f "claude-gui.config.json" ]; then
    echo "Loading configuration from claude-gui.config.json..."

    # Parse JSON using python if available, otherwise use defaults
    if command -v python3 &> /dev/null; then
        BACKEND_PORT=$(python3 -c "import json,sys; print(json.load(open('claude-gui.config.json')).get('backend', {}).get('port', 8080))" 2>/dev/null || echo 8080)
        FRONTEND_PORT=$(python3 -c "import json,sys; print(json.load(open('claude-gui.config.json')).get('frontend', {}).get('port', 4200))" 2>/dev/null || echo 4200)
        BACKEND_HOST=$(python3 -c "import json,sys; print(json.load(open('claude-gui.config.json')).get('backend', {}).get('host', 'localhost'))" 2>/dev/null || echo "localhost")
        FRONTEND_HOST=$(python3 -c "import json,sys; print(json.load(open('claude-gui.config.json')).get('frontend', {}).get('host', 'localhost'))" 2>/dev/null || echo "localhost")
        AUTO_OPEN=$(python3 -c "import json,sys; print(str(json.load(open('claude-gui.config.json')).get('autoOpenBrowser', True)).lower())" 2>/dev/null || echo "true")
    fi
else
    echo "No configuration file found, using defaults..."
    echo "To customize ports, copy claude-gui-template.config.json to claude-gui.config.json"
    echo ""
fi

# Update frontend config.json
echo "Updating frontend configuration..."
cat > frontend/public/config.json << EOF
{
  "apiUrl": "http://${BACKEND_HOST}:${BACKEND_PORT}"
}
EOF

# Check if ports are available
if lsof -Pi :$BACKEND_PORT -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "❌ Error: Port $BACKEND_PORT is already in use"
    echo "   Please stop the process using this port or change the backend.port in claude-gui.config.json"
    exit 1
fi

if lsof -Pi :$FRONTEND_PORT -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "❌ Error: Port $FRONTEND_PORT is already in use"
    echo "   Please stop the process using this port or change the frontend.port in claude-gui.config.json"
    exit 1
fi

echo "Starting backend on port $BACKEND_PORT..."
cd backend
SERVER_PORT=$BACKEND_PORT nohup java -jar target/claude-code-gui-backend-1.0.0.jar > ../backend.log 2>&1 &
BACKEND_PID=$!
cd ..

echo "Starting frontend on port $FRONTEND_PORT..."
cd frontend
nohup npm start -- --port $FRONTEND_PORT > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

# Cleanup function
cleanup() {
    echo ""
    echo "Shutting down..."
    kill $BACKEND_PID 2>/dev/null || true
    kill $FRONTEND_PID 2>/dev/null || true
    echo "Stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo ""
echo "Waiting for services to start..."

# Wait for backend
for i in {1..30}; do
    if curl -s http://${BACKEND_HOST}:${BACKEND_PORT}/api/claude/status > /dev/null 2>&1; then
        echo "✓ Backend ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Backend failed to start. Check backend.log for details."
        cleanup
    fi
    sleep 1
done

# Wait for frontend
for i in {1..60}; do
    if curl -s http://${FRONTEND_HOST}:${FRONTEND_PORT} > /dev/null 2>&1; then
        echo "✓ Frontend ready"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "❌ Frontend failed to start. Check frontend.log for details."
        cleanup
    fi
    sleep 1
done

echo ""
echo "════════════════════════════════════════════════════════"
echo "  ✓ Claude Code GUI is running!"
echo "════════════════════════════════════════════════════════"
echo ""
echo "  Frontend: http://${FRONTEND_HOST}:${FRONTEND_PORT}"
echo "  Backend:  http://${BACKEND_HOST}:${BACKEND_PORT}"
echo ""
echo "  Logs:"
echo "    Backend:  tail -f backend.log"
echo "    Frontend: tail -f frontend.log"
echo ""
echo "  Press Ctrl+C to stop"
echo ""

# Auto-open browser
if [ "$AUTO_OPEN" = "true" ]; then
    if command -v xdg-open &> /dev/null; then
        xdg-open "http://${FRONTEND_HOST}:${FRONTEND_PORT}" &>/dev/null || true
    elif command -v open &> /dev/null; then
        open "http://${FRONTEND_HOST}:${FRONTEND_PORT}" &>/dev/null || true
    fi
fi

# Wait for processes
wait
