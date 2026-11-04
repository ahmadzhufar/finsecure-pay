#FinSecure Pay - Transaction Validator

A Spring Boot transaction validation API built for FinSecure Pay to provide a fast, repeatable, and reliable way to validate financial transactions.

## Prerequisites
To run this project locally, you will need:
- Java 17 or higher
- Maven (or use the included `mvnw` wrapper)

## How to Run
1. Clone the repository:
   bash
   git clone https://github.com/ahmadzhufar/finsecure-pay.git
   cd finsecure-pay
   
2. Start the application:
   
   # On Windows
   .\mvnw spring-boot:run

   # On Mac/Linux
   ./mvnw spring-boot:run
   
   The application will start on `http://localhost:8080`.

## How to Test

.\mvnw test


## API Endpoints

### 1. GET /health

curl.exe http://localhost:8080/health
```
**Response:**

{"status":"UP","service":"finsecure-pay"}


### 2. POST /validate

curl.exe -X POST http://localhost:8080/validate \
  -H "Content-Type: application/json" \
  -d '{\"accountId\":\"A\",\"destinationAccountId\":\"B\",\"amount\":100,\"currency\":\"USD\"}'

Response (valid):

{"valid":true,"reasons":[]}


## Business Rules Implemented
1. `accountId` and `destinationAccountId` are required.
2. Self-transfers are not allowed (e.g., Account A to Account A).
3. Amount must be greater than $0.01.
4. Amount must not exceed the daily limit of $10,000.00.
5. Currency must be one of the supported ISO 4217 codes: USD, EUR, GBP, SGD.

## Jenkins Setup (for running the pipeline)

This pipeline requires a Jenkins container with the following tools pre-installed:
- `kubectl` (Kubernetes CLI)
- `helm` (Kubernetes package manager)
- `trivy` (security scanner)
- `docker` (Docker CLI)

To build the custom Jenkins image:

docker build -t finsecure-jenkins -f Dockerfile.jenkins .
docker run -d -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins finsecure-jenkins


## Troubleshooting and Logs

To view logs for the deployed application in Kubernetes:


# View logs for all running pods of FinSecure Pay
kubectl logs -l app=finsecure-pay --tail=100

# View logs for a specific pod
kubectl logs <pod-name>

# View Grafana logs
kubectl logs -n monitoring deployment/monitoring-grafana

# View Prometheus logs
kubectl logs -n monitoring prometheus-monitoring-kube-prometheus-prometheus-0


## Accessing Monitoring Dashboards

The Jenkins pipeline automatically installs Prometheus and Grafana into the `monitoring` namespace.

To access the dashboards, open **two separate terminal windows** and run the following commands. Keep both terminals open while you use the dashboards.

### 1. Access Grafana

kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

Open your browser to: **http://localhost:3000**
- Username: `admin`
- Password: `prom-operator`

### 2. Access Prometheus

kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090

Open your browser to: **http://localhost:9090**

**Note:** If a port-forward connection drops (e.g., because a pod was restarted), simply close the terminal and run the command again.