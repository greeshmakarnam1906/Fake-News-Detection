# Comprehensive Test Report & Verification Log

This document records the exact test execution commands, suites, and verified results for the **Fake News Detection on Social Media** project.

---

## 1. Summary of Execution Status

| Component | Test Suite / Check | Result | Total / Passed | Time |
| :--- | :--- | :---: | :---: | :--- |
| **Backend Unit Tests** | JUnit 5 & Mockito (`TextPreprocessor`, `ModelPredictor`, `RateLimiter`, `Jwt`, `AuthService`, `AnalysisService`) | **PASS** | 20 / 20 | 2.5s |
| **Backend Integration Tests** | Spring Boot Test + MockMvc (`AuthControllerIntegrationTest`, `AnalysisControllerIntegrationTest`) | **PASS** | 7 / 7 | 10.5s |
| **Machine Learning Evaluation** | Stratified 80/20 test split (`ModelTrainer`) | **PASS** | 1,260 samples evaluated | 20.6s |
| **Backend Production Build** | Maven repackaged jar (`fake-news-backend-1.0.0.jar`) | **PASS** | Build Success | 2.8s |
| **Frontend Component Tests** | Vitest + React Testing Library (`NewsInputForm`, `PredictionResult`, `AnalysisHistoryTable`) | **PASS** | 11 / 11 | 1.6s |
| **Frontend Production Build** | Vite production rollup build (`dist/`) | **PASS** | 391 kB bundle | 1.1s |
| **Live Remote Cloud Hosting** | Deployment to remote host without user credentials | **NOT TESTED** | N/A | N/A |

---

## 2. Backend Test Suites (`mvn test`)

Command executed:
```bash
cd backend
mvn test
```

### Detailed Suite Breakdown:
1. **`TextPreprocessorTest` (4/4 PASS):**
   - `testStripPublisherLeaks`: Verifies that wire tags like `(Reuters)` and `(AP)` are stripped from text.
   - `testNormalizeUnicodeAndPunctuation`: Verifies NFD diacritic stripping and special character filtering.
   - `testTokenizeAndRemoveStopWords`: Verifies stop word elimination while retaining informative words.
   - `testEmptyAndBlankText`: Verifies null and blank text safety.

2. **`ModelPredictorTest` (4/4 PASS):**
   - `testLoadClasspathModel`: Verifies that `model.json` loads from classpath with 6,000 vocabulary terms.
   - `testPredictionPipeline`: Verifies end-to-end inference and feature contribution calculation.
   - `testUncertainZoneClassification`: Verifies that scores in `[0.40, 0.60]` trigger the `Uncertain / Needs Verification` verdict.
   - `testMissingModelThrowsException`: Verifies that missing model throws `ModelNotAvailableException` rather than returning fake predictions.

3. **`JwtTokenProviderTest` (2/2 PASS):**
   - `testGenerateAndParseToken`: Verifies token signing, claims extraction, and expiration.
   - `testRejectInvalidToken`: Verifies rejection of tampered and malformed JWT strings.

4. **`RateLimiterServiceTest` (1/1 PASS):**
   - `testRateLimiterThrottle`: Verifies that requests beyond 30 per minute per key are throttled.

5. **`AuthServiceTest` (4/4 PASS):**
   - `testRegisterSuccess`: Verifies user registration and BCrypt password hashing.
   - `testRegisterDuplicateEmail`: Verifies rejection of duplicate email registration.
   - `testLoginSuccess`: Verifies authentication manager handshake and token issue.
   - `testLoginFailure`: Verifies invalid password rejection.

6. **`AnalysisServiceTest` (5/5 PASS):**
   - `testAnalyzeAuthenticatedUser`: Verifies persistence of analysis record for authenticated users.
   - `testUserCannotReadAnotherUsersAnalysis`: Verifies user isolation and resource security.
   - `testUserCannotDeleteAnotherUsersAnalysis`: Verifies ownership enforcement on deletions.
   - `testRateLimitThrottle`: Verifies throttling behavior.
   - `testGetPaginatedUserAnalyses`: Verifies pagination of user analysis history.

7. **`AuthControllerIntegrationTest` (2/2 PASS):**
   - `testAuthWorkflow`: Full register &rarr; login &rarr; `/api/auth/me` with Bearer token.
   - `testRegisterValidationFailure`: Verifies 400 Bad Request with field errors.

8. **`AnalysisControllerIntegrationTest` (5/5 PASS):**
   - `testActuatorHealth`: Verifies `GET /actuator/health` returns status UP.
   - `testAnalyzeEmptyTextValidation`: Verifies rejection of empty text.
   - `testAnalyzeMalformedUrl`: Verifies SSRF protection on invalid URLs.
   - `testFullAnalysisAndHistoryLifecycle`: Full submit &rarr; view in history &rarr; delete &rarr; 404 verify.
   - `testUnauthorizedHistoryAccess`: Verifies 401 Unauthorized for unauthenticated history requests.

**Total Backend Tests:** 27  
**Failures:** 0  
**Errors:** 0  
**Status:** **100% PASS**

---

## 3. Frontend Test Suites (`npm test`)

Command executed:
```bash
cd frontend
npm test
```

### Detailed Suite Breakdown:
1. **`NewsInputForm.test.jsx` (5/5 PASS):**
   - Empty input validation message display.
   - Sample article loading buttons.
   - Successful form submission calling `onSubmit`.
   - Disabled state and spinner during analysis execution.
   - API error alert rendering.

2. **`PredictionResult.test.jsx` (3/3 PASS):**
   - Classification verdict, score, and explanation rendering.
   - Active feature terms and contribution weight breakdown.
   - Reset button action calling `onAnalyzeAnother`.

3. **`AnalysisHistoryTable.test.jsx` (3/3 PASS):**
   - Table rows, formatted dates, and verdict badge styles.
   - Inspect details button action.
   - Empty state rendering when history is empty.

**Total Frontend Tests:** 11  
**Failures:** 0  
**Errors:** 0  
**Status:** **100% PASS**

---

## 4. Production Build Verification

### Backend Build:
```bash
cd backend
mvn package -DskipTests
```
- Output: `backend/target/fake-news-backend-1.0.0.jar`
- Status: **BUILD SUCCESS**

### Frontend Rollup Build:
```bash
cd frontend
npm run build
```
- Output: `frontend/dist/` (HTML, CSS bundle 28.62 kB, JS bundle 391.20 kB)
- Status: **BUILD SUCCESS**
