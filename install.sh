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
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6 or higher."
    echo "   Download from: https://maven.apache.org/download.cgi"
    exit 1
fi
echo "✓ Maven detected"

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
mvn clean install -DskipTests
echo "✓ Backend dependencies installed"

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
