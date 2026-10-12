# REST API Documentation

The **Fake News Detection** application exposes a clean, versioned RESTful API following modern API standards.

- **Base URL (Local):** `http://localhost:8080`
- **Interactive Swagger Documentation:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 JSON Specification:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 1. Authentication & Security

Endpoints marked with **[Authenticated]** require a JWT Bearer token passed in the `Authorization` header:

```http
Authorization: Bearer <your_jwt_token_here>
```

Tokens are valid for 24 hours (`86,400,000 ms`) by default and are signed with HMAC-SHA256.

---

## 2. Standard Error Response Shape

All endpoints return a uniform JSON error payload on failure (4xx / 5xx). Sensitive details such as database stack traces, internal paths, or password values are strictly suppressed.

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed for one or more fields",
  "timestamp": "2026-10-11T12:00:00.000Z",
  "fieldErrors": {
    "text": "Submitted text must be between 10 and 10,000 characters",
    "sourceUrl": "Source URL must be a valid HTTP or HTTPS URL (or left blank)"
  }
}
```

---

## 3. Endpoints

### 3.1 Authentication

#### `POST /api/auth/register`
Registers a new user account and returns a signed JWT.

- **Request Body (`application/json`):**
```json
{
  "name": "Jane Doe",
  "email": "jane@university.edu",
  "password": "securePassword123"
}
```

- **Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "Jane Doe",
  "email": "jane@university.edu",
  "role": "ROLE_USER",
  "expiresInMs": 86400000
}
```

---

#### `POST /api/auth/login`
Authenticates existing credentials and issues a signed JWT.

- **Request Body (`application/json`):**
```json
{
  "email": "jane@university.edu",
  "password": "securePassword123"
}
```

- **Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "Jane Doe",
  "email": "jane@university.edu",
  "role": "ROLE_USER",
  "expiresInMs": 86400000
}
```

---

#### `GET /api/auth/me` **[Authenticated]**
Retrieves user profile details associated with the current JWT token.

- **Response (200 OK):**
```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane@university.edu",
  "role": "ROLE_USER",
  "createdAt": "2026-10-11T09:30:00Z"
}
```

---

### 3.2 News Analysis

#### `POST /api/analyses`
Submits text or social media claims for machine-learning feature extraction and classification.
*Note: Publicly accessible; if an authenticated Bearer token is provided, the analysis is persisted to the user's history.*

- **Rate Limit:** 30 requests per minute per IP / user.
- **Request Body (`application/json`):**
```json
{
  "headline": "NASA Mars Rover Unveils Ancient Riverbed Core Samples",
  "text": "Scientists at the Jet Propulsion Laboratory confirmed that the Perseverance rover successfully drilled and cached core rock samples from an ancient river delta on Mars. Geologists report sedimentary textures indicating sustained liquid water flowed for extended epochs.",
  "sourceUrl": "https://nasa.gov/missions/mars-2020-perseverance",
  "category": "Science & Health"
}
```

- **Response (201 Created):**
```json
{
  "id": 42,
  "headline": "NASA Mars Rover Unveils Ancient Riverbed Core Samples",
  "text": "Scientists at the Jet Propulsion Laboratory confirmed...",
  "sourceUrl": "https://nasa.gov/missions/mars-2020-perseverance",
  "label": "Likely Genuine",
  "score": 0.2241,
  "modelVersion": "1.0.0",
  "explanation": "The content exhibits structural and stylistic characteristics consistent with verified news sources in the training corpus (Model Score: 0.22).",
  "topFakeFeatures": [],
  "topGenuineFeatures": [
    { "token": "scientists", "weight": -0.7412, "contribution": -0.1852 },
    { "token": "confirmed", "weight": -0.6310, "contribution": -0.1415 },
    { "token": "delta", "weight": -0.5218, "contribution": -0.1120 }
  ],
  "limitations": [
    "Model scores reflect statistical resemblance to training data style, not real-time journalistic fact-checking.",
    "Satirical, ironic, or parody content can share stylistic markers with fabricated news.",
    "Breaking news events and newly emerged terminology may experience distribution shift."
  ],
  "verificationActions": [
    "Verify claims against established primary sources and official wire reports.",
    "Check publication date and ensure quotes or photos are not taken out of historical context.",
    "Consult verified fact-checking databases (e.g., PolitiFact, Snopes, FactCheck.org)."
  ],
  "characterCount": 278,
  "wordCount": 38,
  "createdAt": "2026-10-11T12:05:00Z"
}
```

---

#### `GET /api/analyses` **[Authenticated]**
Returns paginated analysis history belonging to the authenticated user.

- **Query Parameters:**
  - `page` *(optional, default 0)*: Zero-based page index.
  - `size` *(optional, default 10)*: Number of items per page (max 50).

- **Response (200 OK):**
```json
{
  "content": [
    {
      "id": 42,
      "headline": "NASA Mars Rover Unveils Ancient Riverbed Core Samples",
      "truncatedText": "Scientists at the Jet Propulsion Laboratory confirmed that the Perseverance rover successfully drilled and cached core rock samples...",
      "sourceUrl": "https://nasa.gov/missions/mars-2020-perseverance",
      "label": "Likely Genuine",
      "score": 0.2241,
      "modelVersion": "1.0.0",
      "createdAt": "2026-10-11T12:05:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

#### `GET /api/analyses/{id}` **[Authenticated]**
Retrieves the complete diagnostic report of a specific analysis record. Strict ownership enforcement: User A cannot access User B's record (returns 404 Not Found).

- **Response (200 OK):** Full `AnalyzeResponse` object.

---

#### `DELETE /api/analyses/{id}` **[Authenticated]**
Deletes an analysis record owned by the authenticated user.

- **Response (204 No Content)**

---

### 3.3 Dashboard

#### `GET /api/dashboard/summary` **[Authenticated]**
Returns aggregate statistics of analyses performed by the user.

- **Response (200 OK):**
```json
{
  "totalAnalyses": 12,
  "likelyFakeCount": 4,
  "likelyGenuineCount": 6,
  "uncertainCount": 2,
  "recentAnalyses": [ ... ],
  "disclaimer": "All figures reflect statistical text-classification model outputs and should not be interpreted as definitive fact-checking or verified truth."
}
```

---

### 3.4 System Health

#### `GET /actuator/health`
Spring Boot Actuator liveness and readiness probe.

- **Response (200 OK):**
```json
{
  "status": "UP"
}
```
