#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "  Fake News Detection on Social Media - Quick Start"
echo "=========================================================="

# 1. Copy .env.example to .env if missing
if [ ! -f .env ]; then
    echo "[*] Creating .env from .env.example with safe local defaults..."
    cp .env.example .env
else
    echo "[*] Using existing .env configuration."
fi

# 2. Check if Docker is available
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    echo "[*] Docker is running. Starting containerized stack..."
    docker compose up --build
    exit 0
fi

# 3. Non-Docker Fallback
echo "[*] Docker not available. Starting in local standalone mode..."

# Ensure mvnw has execute permissions
chmod +x backend/mvnw || true

echo "[*] Starting Spring Boot Backend..."
(cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local) &
BACKEND_PID=$!

echo "[*] Starting React Frontend..."
cd frontend
npm install
npm run dev

# Trap to kill backend on exit
trap "kill $BACKEND_PID" EXIT
