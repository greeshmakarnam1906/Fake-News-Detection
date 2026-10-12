# START HERE — Quick Setup & Run Guide

Welcome to the **Fake News Detection on Social Media** academic capstone system! This application is built with **Java 21, Spring Boot 3.3, React 19 + Vite, and a 100% pure Java Machine Learning pipeline** (no Python required).

---

## 1. Prerequisites

| Component | Minimum Version | Check Command | Purpose |
| :--- | :--- | :--- | :--- |
| **Java Development Kit (JDK)** | Java 21 LTS | `java -version` | Backend runtime & ML engine |
| **Node.js & npm** | Node 18+ / npm 9+ | `node -v` && `npm -v` | Frontend development & build |
| **Docker & Docker Compose** *(Optional)* | Docker 24+ | `docker --version` | Containerized 1-command startup |
| **MySQL Database** *(Optional)* | MySQL 8.0+ | `mysql --version` | Persistent database storage |

*Note: You do **not** need Apache Maven installed on your machine. The included Maven Wrapper (`./mvnw` or `.\mvnw.cmd`) will handle all backend builds automatically.*

---

## 2. Fastest One-Click Startup

We provide convenience launcher scripts for all platforms:

### On Windows:
Double-click `run.bat` or run in terminal:
```cmd
.\run.bat
```

### On macOS / Linux:
```bash
chmod +x run.sh backend/mvnw
./run.sh
```

These scripts will:
1. Automatically copy `.env.example` to `.env` with working defaults.
2. If Docker is available, launch MySQL 8, Backend, and Frontend in containers.
3. If Docker is not available, launch the backend and frontend in local standalone mode.

To stop the services:
- Windows: `.\stop.bat`
- Linux/macOS: `./stop.sh`

---

## 3. Manual Startup Instructions

### Option A: Using Docker Compose
```bash
# 1. Copy environment template
cp .env.example .env

# 2. Launch all services
docker compose up --build
```
- Frontend: **[http://localhost:3000](http://localhost:3000)**
- Backend API: **[http://localhost:8080](http://localhost:8080)**
- Swagger UI: **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**
- Actuator Health: **[http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)**

---

### Option B: Running Without Docker (Zero-Config Standalone Mode)

If you don't have MySQL or Docker installed, you can run in standalone mode with an embedded file database:

#### Step 1: Start Backend
In terminal 1:
```bash
cd backend

# On Windows:
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# On Linux/macOS:
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
*The backend will automatically create persistent storage at `backend/data/fakenews_db`, run Flyway migrations, and start on port 8080.*

#### Step 2: Start Frontend
In terminal 2:
```bash
cd frontend
npm install
npm run dev
```
Open **[http://localhost:5173](http://localhost:5173)** in your browser!

---

### Option C: Running With a Local MySQL 8 Server

If you have a local MySQL 8 server running on port 3306:
1. Create the database: `CREATE DATABASE fakenews_db;`
2. Start the backend:
   ```bash
   cd backend
   # Windows:
   .\mvnw.cmd spring-boot:run
   # Linux/macOS:
   ./mvnw spring-boot:run
   ```
3. Start the frontend:
   ```bash
   cd frontend
   npm install && npm run dev
   ```

---

## 4. Re-training the Machine Learning Model (Optional)

The pre-trained model artifact `model.json` is already bundled inside `backend/src/main/resources/model.json`. You do **not** need to retrain the model to run the app.

If you wish to re-run the training pipeline from the public dataset:
1. Download the public George McIntire dataset into `ml/data/`:
   ```bash
   curl -L https://raw.githubusercontent.com/joolsa/fake_real_news_dataset/master/fake_or_real_news.csv.zip -o ml/data/dataset.zip
   tar -xf ml/data/dataset.zip -C ml/data/
   ```
2. Execute the pure Java model trainer:
   ```bash
   cd backend
   # Windows:
   .\mvnw.cmd -Ptrain-model exec:java
   # Linux/macOS:
   ./mvnw -Ptrain-model exec:java
   ```
This will clean the text, remove label leakage tags, perform an 80/20 stratified split, evaluate metrics, and update `model.json` and `ml/reports/evaluation.json`.

---

## 5. Troubleshooting Guide

| Issue / Error | Root Cause | Exact Fix |
| :--- | :--- | :--- |
| **Docker not running or not installed** | Docker daemon is stopped | Use Option B (Standalone mode) or start Docker Desktop. |
| **Port 8080 already in use** | Another process is using port 8080 | Run backend on port 8081: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"` |
| **Port 5173 already in use** | Another Vite dev server is running | Vite will automatically offer port 5174, or specify: `npm run dev -- --port 5174` |
| **Port 3306 conflict in Docker** | Local MySQL is already using port 3306 | In `.env`, change `DB_PORT=3307`. Docker will map to 3307 without conflict. |
| **`Communications link failure` on startup** | Backend cannot reach MySQL 8 | Start MySQL service, verify credentials in `.env`, or run with `-Dspring-boot.run.profiles=local`. |
| **`Permission denied: ./mvnw` (Linux/macOS)** | File execute bit missing | Run `chmod +x backend/mvnw run.sh stop.sh`. |
| **Wrong Java version error** | System has Java 8, 11, or 17 | Ensure `java -version` returns Java 21 LTS. Set `JAVA_HOME` to JDK 21. |
| **Node.js version too old** | Node.js < 18 installed | Upgrade Node.js to LTS (v20 or v22) from nodejs.org. |
| **`model.json` not found (503 Service Unavailable)** | Resource missing from classpath | Ensure `backend/src/main/resources/model.json` exists. If missing, copy from `ml/models/model.json` or re-run the trainer. |
