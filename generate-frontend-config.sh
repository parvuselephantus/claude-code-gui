#!/bin/bash

# Generate frontend config.json from claude-gui.config.json

CONFIG_FILE="claude-gui.config.json"
FRONTEND_CONFIG="frontend/public/config.json"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: $CONFIG_FILE not found"
    exit 1
fi

# Extract backend host and port
BACKEND_HOST=$(jq -r '.backend.host // "localhost"' "$CONFIG_FILE")
BACKEND_PORT=$(jq -r '.backend.port // 8080' "$CONFIG_FILE")
DOMAIN_NAME=$(jq -r '.domainName // "localhost"' "$CONFIG_FILE")

# Use domain name if set, otherwise use backend host
if [ "$DOMAIN_NAME" != "localhost" ]; then
    API_URL="http://${DOMAIN_NAME}:${BACKEND_PORT}"
else
    API_URL="http://${BACKEND_HOST}:${BACKEND_PORT}"
fi

# Generate frontend config
cat > "$FRONTEND_CONFIG" <<EOF
{
  "apiUrl": "$API_URL"
}
EOF

echo "Generated $FRONTEND_CONFIG with apiUrl: $API_URL"
