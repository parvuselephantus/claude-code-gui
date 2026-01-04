@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ============================================================
echo   Claude Code GUI
echo ============================================================
echo.

REM Check if dependencies are installed
if not exist "backend\target" (
    echo Dependencies not found. Running installation...
    echo.
    call install.bat
    if %ERRORLEVEL% neq 0 exit /b 1
    echo.
)
if not exist "frontend\node_modules" (
    echo Dependencies not found. Running installation...
    echo.
    call install.bat
    if %ERRORLEVEL% neq 0 exit /b 1
    echo.
)

REM Default configuration
set BACKEND_PORT=8080
set FRONTEND_PORT=4200
set BACKEND_HOST=localhost
set FRONTEND_HOST=localhost
set AUTO_OPEN=true

REM Load configuration if exists
if exist "claude-gui.config.json" (
    echo Loading configuration from claude-gui.config.json...
    REM Simple parsing - you may want to use a JSON parser for production
    REM For now, using defaults
) else (
    echo No configuration file found, using defaults...
    echo To customize ports, copy claude-gui-template.config.json to claude-gui.config.json
    echo.
)

REM Update frontend config
echo Updating frontend configuration...
(
echo {
echo   "apiUrl": "http://%BACKEND_HOST%:%BACKEND_PORT%"
echo }
) > frontend\public\config.json

REM Check if ports are available
netstat -ano | findstr ":%BACKEND_PORT%" | findstr "LISTENING" >nul
if %ERRORLEVEL% equ 0 (
    echo [X] Error: Port %BACKEND_PORT% is already in use
    echo    Please stop the process using this port or change the backend.port in claude-gui.config.json
    exit /b 1
)

netstat -ano | findstr ":%FRONTEND_PORT%" | findstr "LISTENING" >nul
if %ERRORLEVEL% equ 0 (
    echo [X] Error: Port %FRONTEND_PORT% is already in use
    echo    Please stop the process using this port or change the frontend.port in claude-gui.config.json
    exit /b 1
)

echo Starting backend on port %BACKEND_PORT%...
cd backend
start "Claude GUI Backend" /min cmd /c "set SERVER_PORT=%BACKEND_PORT% && java -jar target/claude-code-gui-backend-1.0.0.jar > ..\backend.log 2>&1"
cd ..

echo Starting frontend on port %FRONTEND_PORT%...
cd frontend
start "Claude GUI Frontend" /min cmd /c "npm start -- --port %FRONTEND_PORT% > ..\frontend.log 2>&1"
cd ..

echo.
echo Waiting for services to start...
timeout /t 10 /nobreak >nul

echo.
echo ============================================================
echo   Claude Code GUI is running!
echo ============================================================
echo.
echo   Frontend: http://%FRONTEND_HOST%:%FRONTEND_PORT%
echo   Backend:  http://%BACKEND_HOST%:%BACKEND_PORT%
echo.
echo   Logs:
echo     backend.log
echo     frontend.log
echo.
echo   Press Ctrl+C to stop (close this window to keep running)
echo.

REM Auto-open browser
if "%AUTO_OPEN%"=="true" (
    start http://%FRONTEND_HOST%:%FRONTEND_PORT%
)

REM Keep window open
pause

endlocal
