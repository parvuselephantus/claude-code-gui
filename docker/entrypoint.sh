#!/bin/sh

set -e

echo "Starting Claude Code GUI in Docker..."

# Start backend in background
SERVER_PORT=${BACKEND_PORT:-8080} java -jar backend.jar &
BACKEND_PID=$!

# Wait for backend
echo "Waiting for backend..."
sleep 5

# Update frontend config
cat > frontend/public/config.json << EOF
{
  "apiUrl": "http://localhost:${BACKEND_PORT:-8080}"
}
EOF

# Start frontend
cd frontend
npm start -- --host 0.0.0.0 --port ${FRONTEND_PORT:-4200} &
FRONTEND_PID=$!

# Cleanup function
cleanup() {
    echo "Shutting down..."
    kill $BACKEND_PID 2>/dev/null || true
    kill $FRONTEND_PID 2>/dev/null || true
    exit 0
}

trap cleanup SIGTERM SIGINT

# Wait for processes
wait
