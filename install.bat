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
set MAVEN_CMD=
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set MAVEN_CMD=mvn
) else if exist "backend\mvnw.cmd" (
    set MAVEN_CMD=backend\mvnw.cmd
) else if exist "..\mvnw.cmd" (
    set MAVEN_CMD=..\mvnw.cmd
) else if exist "C:\Program Files\Apache\maven\bin\mvn.cmd" (
    set MAVEN_CMD="C:\Program Files\Apache\maven\bin\mvn.cmd"
) else if exist "%USERPROFILE%\apache-maven\bin\mvn.cmd" (
    set MAVEN_CMD="%USERPROFILE%\apache-maven\bin\mvn.cmd"
)

if "%MAVEN_CMD%"=="" (
    REM Check if backend jar already exists
    if exist "backend\target\claude-code-gui-backend-1.0.0.jar" (
        echo [!] Maven not found, but backend jar already exists - will skip rebuild
    ) else (
        echo [X] Maven not found in PATH or common locations.
        echo    Searched for:
        echo    - mvn command in PATH
        echo    - Maven Wrapper (mvnw.cmd^) in backend\ or parent directory
        echo    - C:\Program Files\Apache\maven
        echo    - %USERPROFILE%\apache-maven
        echo.
        echo    Please install Maven 3.6+ or use Maven Wrapper.
        echo    Download from: https://maven.apache.org/download.cgi
        exit /b 1
    )
) else (
    echo [OK] Maven detected: %MAVEN_CMD%
)

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

REM Check if we can build or use existing jar
if not "%MAVEN_CMD%"=="" (
    call %MAVEN_CMD% clean install -DskipTests
    if %ERRORLEVEL% neq 0 (
        echo [X] Backend installation failed
        exit /b 1
    )
    echo [OK] Backend dependencies installed
) else if exist "target\claude-code-gui-backend-1.0.0.jar" (
    echo [!] Maven not available but backend jar already exists.
    echo [OK] Skipping backend build - using existing jar
) else (
    echo [X] Cannot build backend - Maven not found and no existing jar
    exit /b 1
)

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
