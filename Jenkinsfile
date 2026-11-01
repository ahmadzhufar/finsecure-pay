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
                // Using the Maven wrapper included in the repo
                sh './mvnw clean package'
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

        stage('Docker Push') {
            steps {
                echo 'Pushing image to Docker Hub...'
                script {
                    docker.withRegistry('', 'dockerhub-creds') {
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                        // Also tag as latest
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to local Docker Desktop K8s...'
                withKubeConfig(credentialsId: 'kubeconfig') {
                    // Update the deployment.yaml with the new image tag
                    sh "sed -i 's|image: .*|image: ${IMAGE_NAME}:${IMAGE_TAG}|g' k8s/deployment.yaml"
                    
                    // Apply the deployment
                    sh 'kubectl apply -f k8s/'
                    
                    // Wait for rollout to finish
                    sh 'kubectl rollout status deployment/finsecure-deployment'
                }
            }
        }
    }
}