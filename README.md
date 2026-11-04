# FinSecure Pay - Transaction Validator

A Spring Boot transaction validation API built for FinSecure Pay to provide a fast, repeatable, and reliable way to validate financial transactions.

## Table of Contents
A. [Prerequisites](#prerequisites)  
B. [How to Run](#how-to-run)  
C. [How to Test](#how-to-test)  
D. [API Endpoints](#api-endpoints)  
E. [Business Rules](#business-rules-implemented)  
F. [Jenkins Setup](#jenkins-setup-for-running-the-pipeline)  
G. [Troubleshooting and Logs](#troubleshooting-and-logs)  
H. [Accessing Monitoring Dashboards](#accessing-monitoring-dashboards)

---

## A. Prerequisites

### Local Machine Requirements
To run this project locally, you will need:
- **Java 17 or higher**
- **Maven** (or use the included `mvnw` wrapper)
- **Docker Desktop** (for building the container and running the local Kubernetes cluster)
- **Kubernetes** enabled in Docker Desktop (Settings → Kubernetes → Enable Kubernetes)
- **`kubectl`** installed locally (to interact with the cluster)
- **`helm`** installed locally (to install the monitoring stack if required)

### Verify your local environment
```bash
docker --version
kubectl version --client
helm version
```

---

## B. How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/ahmadzhufar/finsecure-pay.git
   cd finsecure-pay
   ```

2. Start the application:
   ```bash
   # On Windows
   .\mvnw spring-boot:run

   # On Mac/Linux
   ./mvnw spring-boot:run
   ```

   The application will start on `http://localhost:8080`.

---

## C. How to Test

```bash
.\mvnw test
```

---

## D. API Endpoints

### 1. GET /health

```bash
curl.exe http://localhost:8080/health
```

**Response:**
```json
{"status":"UP","service":"finsecure-pay"}
```

### 2. POST /validate

```bash
curl.exe -X POST http://localhost:8080/validate \
  -H "Content-Type: application/json" \
  -d '{\"accountId\":\"A\",\"destinationAccountId\":\"B\",\"amount\":100,\"currency\":\"USD\"}'
```

**Response (valid):**
```json
{"valid":true,"reasons":[]}
```

---

## E. Business Rules Implemented

1. `accountId` and `destinationAccountId` are required.
2. Self-transfers are not allowed (e.g., Account A to Account A).
3. Amount must be greater than $0.01.
4. Amount must not exceed the daily limit of $10,000.00.
5. Currency must be one of the supported ISO 4217 codes: USD, EUR, GBP, SGD.

---

## F. Jenkins Setup (for running the pipeline)

This pipeline requires a Jenkins container with the following tools pre-installed:
- `kubectl` (Kubernetes CLI)
- `helm` (Kubernetes package manager)
- `trivy` (security scanner)
- `docker` (Docker CLI)

### 1. Build the custom Jenkins image
```bash
docker build -t finsecure-jenkins -f Dockerfile.jenkins .
docker run -d -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins finsecure-jenkins
```

### 2. Unlock Jenkins
Once the container is running, open `http://localhost:8080` in your browser. You will be prompted for the initial admin password.

Retrieve it with:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Paste the password into the browser, then install the **suggested plugins** and create an admin user.

### 3. Install additional plugins
Go to **Manage Jenkins → Plugins → Available plugins** and install:
- **Docker Pipeline**
- **Kubernetes CLI**

Restart Jenkins when prompted.

### 4. Add credentials
Go to **Manage Jenkins → Credentials → System → Global credentials (unrestricted)** and add:

**a) Docker Hub credentials**
- **Kind:** Username with password
- **ID:** `dockerhub-creds`
- **Username:** Your Docker Hub username
- **Password:** Your Docker Hub password (or access token)

**b) Kubernetes kubeconfig**
- **Kind:** Secret file
- **ID:** `kubeconfig`
- **File:** Upload `~/.kube/config` from your local machine

### 5. Verify the Kubernetes manifests exist
Ensure the following files exist in the `k8s/` folder before running the pipeline:
- `configmap.yaml`
- `deployment.yaml`
- `service.yaml`
- `servicemonitor.yaml` ← **Required for Prometheus to discover the application**

### 6. Create the pipeline job
- **New Item → Pipeline → FinSecure-Pipeline**
- **Definition:** Pipeline script from SCM
- **SCM:** Git
- **Repository URL:** `https://github.com/ahmadzhufar/finsecure-pay.git`
- **Branch Specifier:** `*/main`
- **Script Path:** `Jenkinsfile`
- Click **Save**

### 7. Run the pipeline
Click **Build Now**. The pipeline will:
- Build and test the Spring Boot application
- Scan dependencies and the Docker image with Trivy
- Build and push the Docker image to Docker Hub
- Install Prometheus and Grafana via Helm
- Deploy the application and ServiceMonitor to Kubernetes

---

## G. Troubleshooting and Logs

To view logs for the deployed application in Kubernetes:

```bash
# Get pod names
kubectl get pods -l app=finsecure-pay

# View logs for all running pods of FinSecure Pay
kubectl logs -l app=finsecure-pay --tail=100

# View logs for a specific pod
kubectl logs <pod-name>

# Describe gives you events, resource usage, and error messages
kubectl describe pod <pod-name>

# View Grafana logs
kubectl logs -n monitoring deployment/monitoring-grafana

# View Prometheus logs
kubectl logs -n monitoring prometheus-monitoring-kube-prometheus-prometheus-0
```

---

## H. Accessing Monitoring Dashboards

The Jenkins pipeline automatically installs Prometheus and Grafana into the `monitoring` namespace.

To access the dashboards, open **two separate terminal windows** and run the following commands. Keep both terminals open while you use the dashboards.

### 1. Access Grafana

```bash
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
```

Open your browser to: **http://localhost:3000**
- Username: `admin`
- Password: `prom-operator`

### 2. Access Prometheus

```bash
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
```

Open your browser to: **http://localhost:9090**

> **Note:** If a port-forward connection drops (e.g., because a pod was restarted), simply close the terminal and run the command again.