pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = 'ahmadzhufar'
        IMAGE_NAME = "${DOCKER_HUB_USER}/finsecure-pay"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Building and running unit tests...'
                sh 'sh ./mvnw clean package'
            }
        }

        stage('Dependency Scan (Trivy)') {
            steps {
                echo 'Scanning dependencies for known vulnerabilities...'
                sh 'trivy fs --exit-code 1 --severity CRITICAL,HIGH --ignorefile .trivyignore --no-progress .'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                }
            }
        }

        stage('Container Scan (Trivy)') {
            steps {
                echo 'Scanning Docker image for vulnerabilities...'
                sh "trivy image --exit-code 1 --severity CRITICAL,HIGH --ignorefile ${WORKSPACE}/.trivyignore --no-progress ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Docker Push') {
            steps {
                echo 'Pushing image to Docker Hub...'
                script {
                    docker.withRegistry('', 'dockerhub-creds') {
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Install Monitoring Stack') {
            steps {
                echo 'Installing Prometheus and Grafana via Helm...'
                sh '''
                    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
                    helm repo update
                    helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
                      --namespace monitoring \
                      --create-namespace \
                      --set grafana.adminPassword=prom-operator \
                      --set prometheus-node-exporter.enabled=false \
                      --wait \
                      --timeout 5m
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to local Docker Desktop K8s...'
                withKubeConfig(credentialsId: 'kubeconfig') {
                    sh "sed -i 's|image: .*|image: ${IMAGE_NAME}:${IMAGE_TAG}|g' k8s/deployment.yaml"
                    sh 'kubectl apply -f k8s/'
                    sh 'kubectl rollout status deployment/finsecure-deployment'
                }
            }
        }
    }
}