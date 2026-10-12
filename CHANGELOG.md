# Changelog

All notable changes to the **Fake News Detection on Social Media** project are documented in this file.

## [1.0.0] - 2026-10-11

### Initial Master Build & Engineering Release
- **Machine Learning Pipeline (Pure Java):**
  - Implemented `ModelTrainer` with TF-IDF and L2-regularized Logistic Regression trained from scratch in standard Java.
  - Added `TextPreprocessor` with regex wire-service label leakage removal (`(Reuters)`, `(AP)`) and NFD diacritic mark stripping.
  - Stratified 80/20 train/test evaluation on George McIntire dataset producing 93.33% accuracy and 0.9343 F1-score.
  - Exported deterministic `model.json` artifact containing vocabulary, smoothed IDF weights, coefficients, and bias.
  - Implemented runtime inference engine `ModelPredictor` with tri-zone decision boundaries (`Likely Genuine` <= 0.40, `Uncertain` 0.40–0.60, `Likely Fake` >= 0.60) and explainability top-5 feature terms.
- **Backend Architecture (Spring Boot 3.3 / Java 21):**
  - Configured Spring Security with stateless HMAC-SHA256 JWT authentication and BCrypt password hashing.
  - Added versioned Flyway database migrations (`V1` for `users`, `V2` for `analyses` with foreign key and index).
  - Implemented sliding-window in-memory rate limiter on `/api/analyses` (30 requests/minute limit).
  - Implemented unified JSON error handling via `GlobalExceptionHandler` preventing sensitive stack trace leakage.
  - Added Spring Boot Actuator `/actuator/health` and OpenAPI 3.0 Swagger UI documentation.
  - Added `local` standalone profile supporting persistent embedded database for zero-config workstation execution.
- **Frontend Application (React 19 / Vite / Tailwind CSS):**
  - Built editorial news theme with responsive layout and accessible focus states.
  - Implemented components: `Navbar`, `Footer`, `NewsInputForm` (with character counter and sample buttons), `PredictionResult`, `ConfidenceIndicator`, `VerificationChecklist`, `AnalysisHistoryTable` (with pagination, detail modal, and delete), `DashboardSummary`, `ProtectedRoute`.
  - Implemented `AuthContext` with JWT session persistence in `localStorage`.
- **Packaging & DevOps:**
  - Multi-stage Dockerfiles for backend and frontend.
  - `docker-compose.yml` for unified MySQL 8, backend, and frontend orchestration.
  - Added platform convenience launcher scripts `run.bat`, `run.sh`, `stop.bat`, `stop.sh`.
  - Added `render.yaml` Blueprint configuration and comprehensive documentation.
