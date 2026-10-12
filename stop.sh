#!/usr/bin/env bash
echo "[*] Stopping Docker Compose containers..."
if command -v docker >/dev/null 2>&1; then
    docker compose down || true
fi
echo "[*] Done."
