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
                // Scans the pom.xml for vulnerable libraries. 
                // --exit-code 1 makes the stage fail if vulnerabilities are found.
                // --severity CRITICAL,HIGH sets the threshold.
                sh 'trivy fs --exit-code 1 --severity CRITICAL,HIGH --ignore-unfixed --no-progress .'
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
                // Scans the built image. We scan the exact tag we just built.
                sh "trivy image --exit-code 1 --severity CRITICAL,HIGH --ignore-unfixed --no-progress ${IMAGE_NAME}:${IMAGE_TAG}"
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