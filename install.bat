@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   Claude Code GUI - Installation
echo ============================================================
echo.

REM Check for required dependencies
echo Checking dependencies...

REM Check Java
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] Java not found. Please install Java 17 or higher.
    echo    Download from: https://adoptium.net/
    exit /b 1
)
echo [OK] Java detected

REM Check Maven
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] Maven not found. Please install Maven 3.6 or higher.
    echo    Download from: https://maven.apache.org/download.cgi
    exit /b 1
)
echo [OK] Maven detected

REM Check Node.js
where node >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] Node.js not found. Please install Node.js 18 or higher.
    echo    Download from: https://nodejs.org/
    exit /b 1
)
echo [OK] Node.js detected

REM Check npm
where npm >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] npm not found. Please install npm.
    exit /b 1
)
echo [OK] npm detected

echo.
echo Installing backend dependencies...
cd backend
call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [X] Backend installation failed
    exit /b 1
)
echo [OK] Backend dependencies installed

echo.
echo Installing frontend dependencies...
cd ..\frontend
call npm install
if %ERRORLEVEL% neq 0 (
    echo [X] Frontend installation failed
    exit /b 1
)
echo [OK] Frontend dependencies installed

cd ..

echo.
echo ============================================================
echo   Installation Complete!
echo ============================================================
echo.
echo To start the application, run:
echo   start.bat
echo.

endlocal
