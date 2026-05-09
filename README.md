# CloudEagle DevOps Assignment - Deployment & Infrastructure Design

This repository contains the CI/CD pipeline design and infrastructure architecture for the `sync-service` Spring Boot application.

## Part 1: CI/CD Design

### 1. Branching Strategy
We follow a **GitFlow-based strategy** to ensure stability:
*   **Feature Branches (`feature/*`):** For active development.
*   **Develop Branch:** Mapped to the **QA environment**. Every merge here triggers an auto-deploy to QA.
*   **Main Branch:** Mapped to **Production**. 
*   **Prevention of Accidental Prod Deployments:** 
    *   Main branch is protected. 
    *   Direct commits are disabled. 
    *   Deployment to Production requires a **Manual Approval** gate in Jenkins.

### 2. Jenkins Pipeline Stages
1.  **Checkout:** Pulls code from GitHub.
2.  **Build & Test:** Runs Maven build and Unit tests.
3.  **Static Code Analysis:** Uses SonarQube for code quality checks.
4.  **Dockerize:** Builds a Docker image and pushes it to Google Artifact Registry.
5.  **Deploy:** Deploys to the respective environment (QA/Staging/Prod).
6.  **Rollback:** If the deployment fails, the pipeline automatically triggers a script to redeploy the previous stable Docker image tag.

### 3. Configuration & Secrets
*   **Env-specific Configs:** Managed via Spring Boot Profiles (`application-qa.yaml`, `application-prod.yaml`).
*   **Secrets:** Handled via **GCP Secret Manager**. Credentials like MongoDB URI and API keys are injected at runtime as environment variables.

### 4. Deployment Strategy
*   **Strategy:** **Rolling Update**.
*   **Justification:** Given startup constraints, Rolling Updates allow us to update instances one-by-one without requiring double the resources (unlike Blue/Green), ensuring zero downtime while keeping costs low.

---

## Part 2: Infrastructure Design (GCP)

### 1. Compute Choice: Cloud Run
*   **Why:** Since we have startup constraints, **Cloud Run** is the most cost-effective. It is serverless, scales to zero when not in use, and handles auto-scaling out-of-the-box.

### 2. Database: MongoDB Atlas
*   **Why:** Managed MongoDB Atlas (Shared Tier) is cheaper and easier to maintain than self-hosting MongoDB on VMs, which requires manual backups and scaling.

### 3. Networking & Security
*   **VPC:** Cloud Run is connected to a VPC via a Serverless VPC Access connector.
*   **IAM:** Each service runs under a dedicated Service Account with "Least Privilege" access.
*   **Ingress:** Restricted to an External HTTP(S) Load Balancer with Cloud Armor for DDoS protection.

### 4. Logging & Monitoring
*   **Stack:** Google Cloud Operations Suite (formerly Stackdriver).
*   **Logs:** All application logs are sent to Cloud Logging.
*   **Alerts:** Uptime checks and latency alerts configured in Cloud Monitoring.
