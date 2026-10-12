@echo off
setlocal enabledelayedexpansion

echo ==========================================================
echo   Fake News Detection on Social Media - Quick Start
echo ==========================================================

:: 1. Copy .env.example to .env if not exists
if not exist .env (
    echo [*] Creating .env from .env.example with safe local defaults...
    copy .env.example .env >nul
) else (
    echo [*] Using existing .env configuration.
)

:: 2. Check if Docker is installed and running
where docker >nul 2>nul
if %ERRORLEVEL% equ 0 (
    docker info >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        echo [*] Docker is running. Starting containerized stack...
        docker compose up --build
        goto :end
    )
)

:: 3. Non-Docker execution fallback
echo [*] Docker not detected or not running. Starting in local standalone mode...
echo [*] Checking Java 21...
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java 21 is required but was not found on PATH.
    pause
    exit /b 1
)

echo [*] Starting Spring Boot Backend in background...
start "FakeNews Backend" cmd /c "cd /d %~dp0backend && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local"

echo [*] Installing frontend dependencies...
cd /d "%~dp0frontend"
call npm install

echo [*] Starting React Frontend in background...
start "FakeNews Frontend" cmd /c "cd /d %~dp0frontend && npm run dev"
cd /d "%~dp0"

:end
echo ==========================================================
echo Application started!
echo Frontend: http://localhost:5173 (or http://localhost:3000 with Docker)
echo Backend:  http://localhost:8080
echo Swagger:  http://localhost:8080/swagger-ui.html
echo ==========================================================
