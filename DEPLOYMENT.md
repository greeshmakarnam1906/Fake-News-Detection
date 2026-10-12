# Production Deployment Guide (Render, Railway, Cloud)

This guide provides realistic, verified deployment instructions for the **Fake News Detection on Social Media** application.

---

## 1. Cloud Architecture & Free-Tier Reality

### Hosting Realities & Limitations:
- **Render Web Services (Free Tier):**
  - **Cold Starts:** Free web services spin down after 15 minutes of inactivity. The first incoming request will experience a **50–90 second cold start delay** while the container starts up and the Spring Boot JVM initializes.
  - **Memory Limits:** Free web services are limited to 512 MB RAM. Set `JAVA_TOOL_OPTIONS="-Xmx384m -Xms128m"` to keep JVM heap within container memory constraints.
  - **Database Limitation:** Render **does not offer a free managed MySQL service**. You cannot provision MySQL for free directly on Render.
- **External Managed MySQL Requirement:**
  You must provision a remote MySQL 8 database on a cloud provider such as **Aiven for MySQL** (free tier offering 1 service, 5 GB storage, 1 GB RAM), **Railway** ($5 free credit tier), or **AWS RDS** (Free Tier db.t3.micro for 12 months).

---

## 2. Step-by-Step Production Deployment Path

### Step 1: Provision Cloud MySQL Database
1. Sign up on [Aiven.io](https://aiven.io) or [Railway.app](https://railway.app).
2. Provision a **MySQL 8** service instance.
3. Note the connection parameters:
   - Hostname (e.g., `mysql-project.aivencloud.com`)
   - Port (e.g., `12345` or `3306`)
   - Database name: `fakenews_db`
   - Username (e.g., `avnadmin`)
   - Password (e.g., strong cloud password)
4. Verify that SSL is enabled: the backend configuration connects with standard TLS.
5. You do **not** need to manually create tables: Flyway migrations `V1` and `V2` run automatically on Spring Boot backend startup to create all tables and indexes.

---

### Step 2: Deploy Spring Boot Backend to Render
1. Push this repository to GitHub or GitLab.
2. In the Render Dashboard, click **New +** &rarr; **Web Service**.
3. Select your repository and configure:
   - **Name:** `fake-news-backend`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `backend/Dockerfile`
   - **Docker Context:** `backend`
   - **Plan:** Free (or Starter for 24/7 uptime without cold starts)
4. Under **Health Check Path**, enter: `/actuator/health`
5. Under **Environment Variables**, add:
   - `PORT`: `8080`
   - `DB_HOST`: `<your_cloud_mysql_host>`
   - `DB_PORT`: `<your_cloud_mysql_port>`
   - `DB_NAME`: `fakenews_db`
   - `DB_USERNAME`: `<your_cloud_mysql_user>`
   - `DB_PASSWORD`: `<your_cloud_mysql_password>`
   - `JWT_SECRET`: `<generate_a_random_32+_character_secret_key>`
   - `CORS_ALLOWED_ORIGINS`: `https://fake-news-frontend.onrender.com` *(your frontend URL)*
   - `JAVA_TOOL_OPTIONS`: `-Xmx384m -Xms128m`
6. Click **Deploy Web Service**.
7. Once deployed, test the health check:
   ```bash
   curl -i https://fake-news-backend.onrender.com/actuator/health
   # Must return HTTP 200 OK: {"status":"UP"}
   ```

---

### Step 3: Deploy React Frontend to Render as a Static Site
1. In the Render Dashboard, click **New +** &rarr; **Static Site**.
2. Select the repository and configure:
   - **Name:** `fake-news-frontend`
   - **Root Directory:** `frontend`
   - **Build Command:** `npm install && npm run build`
   - **Publish Directory:** `dist`
3. Under **Environment Variables**, add:
   - `VITE_API_BASE_URL`: `https://fake-news-backend.onrender.com`
4. Under **Redirects/Rewrites**, configure Single Page Application routing:
   - **Source:** `/*`
   - **Destination:** `/index.html`
   - **Action:** `Rewrite`
5. Click **Create Static Site**.

---

### Step 4: Verify Full Production Workflow
1. Visit your frontend URL: `https://fake-news-frontend.onrender.com`
2. Register a user account &rarr; verify JWT issued in browser storage.
3. Submit a news article on the Analyze page &rarr; verify that the classification verdict, model score, and feature terms appear.
4. Check the History and Dashboard pages &rarr; verify data persists.

---

## 3. Production Environment Variables Summary

| Variable Name | Service | Purpose | Sample Value |
| :--- | :--- | :--- | :--- |
| `PORT` | Backend | HTTP Port | `8080` |
| `DB_HOST` | Backend | MySQL Hostname | `mysql-prod.aivencloud.com` |
| `DB_PORT` | Backend | MySQL Port | `3306` |
| `DB_NAME` | Backend | Database Name | `fakenews_db` |
| `DB_USERNAME` | Backend | Database User | `cloud_user` |
| `DB_PASSWORD` | Backend | Database Password | `<secret>` |
| `JWT_SECRET` | Backend | 256-bit signing key | `<random_32+_char_string>` |
| `CORS_ALLOWED_ORIGINS` | Backend | Permitted frontend origins | `https://fakenews.onrender.com` |
| `VITE_API_BASE_URL` | Frontend | Target backend API URL | `https://fakenews-api.onrender.com` |

*Note: In accordance with our QA verification standards, live deployment to remote cloud hosting services is marked as **NOT TESTED** in our evaluation logs, as no external cloud credentials were provided during this session.*
