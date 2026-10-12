@echo off
echo [*] Stopping Docker Compose containers...
where docker >nul 2>nul
if %ERRORLEVEL% equ 0 (
    docker compose down
)
echo [*] Stopping local Java / Node processes if running...
taskkill /F /FI "WINDOWTITLE eq FakeNews Backend*" >nul 2>nul
taskkill /F /FI "WINDOWTITLE eq FakeNews Frontend*" >nul 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>nul
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>nul
echo Done.
