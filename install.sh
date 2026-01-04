#!/bin/bash

set -e

echo "════════════════════════════════════════════════════════"
echo "  Claude Code GUI - Installation"
echo "════════════════════════════════════════════════════════"
echo ""

# Check for required dependencies
echo "Checking dependencies..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or higher."
    echo "   Download from: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher required. Found version: $JAVA_VERSION"
    exit 1
fi
echo "✓ Java $JAVA_VERSION detected"

# Check Maven
MAVEN_CMD=""
if command -v mvn &> /dev/null; then
    MAVEN_CMD="mvn"
elif [ -f "backend/mvnw" ]; then
    MAVEN_CMD="./backend/mvnw"
    chmod +x backend/mvnw 2>/dev/null || true
elif [ -f "../mvnw" ]; then
    MAVEN_CMD="../mvnw"
    chmod +x ../mvnw 2>/dev/null || true
elif [ -d "/usr/local/maven" ]; then
    MAVEN_CMD="/usr/local/maven/bin/mvn"
else
    # Search for apache-maven-* directories in home directory
    MAVEN_DIR=$(find "$HOME" -maxdepth 1 -type d -name "apache-maven-*" 2>/dev/null | head -n 1)
    if [ -n "$MAVEN_DIR" ] && [ -f "$MAVEN_DIR/bin/mvn" ]; then
        MAVEN_CMD="$MAVEN_DIR/bin/mvn"
    elif [ -d "$HOME/.m2/wrapper" ]; then
        # Check for Maven Wrapper in ~/.m2
        WRAPPER_MAVEN=$(find "$HOME/.m2/wrapper" -name "mvn" -type f 2>/dev/null | head -n 1)
        if [ -n "$WRAPPER_MAVEN" ]; then
            MAVEN_CMD="$WRAPPER_MAVEN"
        fi
    fi
fi

if [ -z "$MAVEN_CMD" ]; then
    # Check if backend jar already exists
    if [ -f "backend/target/claude-code-gui-backend-1.0.0.jar" ]; then
        echo "⚠ Maven not found, but backend jar already exists - will skip rebuild"
    else
        echo "❌ Maven not found in PATH or common locations."
        echo "   Searched for:"
        echo "   - mvn command in PATH"
        echo "   - Maven Wrapper (mvnw) in backend/ or parent directory"
        echo "   - /usr/local/maven"
        echo "   - ~/apache-maven"
        echo "   - ~/.m2/wrapper"
        echo ""
        echo "   Please install Maven 3.6+ or use Maven Wrapper."
        echo "   Download from: https://maven.apache.org/download.cgi"
        exit 1
    fi
else
    echo "✓ Maven detected: $MAVEN_CMD"
fi

# Check Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js 18 or higher."
    echo "   Download from: https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Node.js 18 or higher required. Found version: $NODE_VERSION"
    exit 1
fi
echo "✓ Node.js $NODE_VERSION detected"

# Check npm
if ! command -v npm &> /dev/null; then
    echo "❌ npm not found. Please install npm."
    exit 1
fi
echo "✓ npm detected"

echo ""
echo "Installing backend dependencies..."
cd backend

# Check if we can build or use existing jar
if [ -n "$MAVEN_CMD" ]; then
    $MAVEN_CMD clean install -DskipTests
    echo "✓ Backend dependencies installed"
elif [ -f "target/claude-code-gui-backend-1.0.0.jar" ]; then
    echo "⚠ Maven not available but backend jar already exists."
    echo "✓ Skipping backend build - using existing jar"
else
    echo "❌ Cannot build backend - Maven not found and no existing jar"
    exit 1
fi

echo ""
echo "Installing frontend dependencies..."
cd ../frontend
npm install
echo "✓ Frontend dependencies installed"

echo ""
echo "════════════════════════════════════════════════════════"
echo "  Installation Complete!"
echo "════════════════════════════════════════════════════════"
echo ""
echo "To start the application, run:"
echo "  ./start.sh"
echo ""
