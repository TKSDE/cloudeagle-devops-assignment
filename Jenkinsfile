pipeline {
    agent any
    
    environment {
        GCP_PROJECT = 'your-project-id'
        IMAGE_NAME = 'sync-service'
        REGION = 'us-central1'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh './mvnw clean package -DskipTests=false'
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh "docker build -t gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} ."
                sh "docker push gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }

        stage('Deploy to QA') {
            when { branch 'develop' }
            steps {
                sh "gcloud run deploy ${IMAGE_NAME}-qa --image gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} --region ${REGION}"
            }
        }

        stage('Deploy to Prod') {
            when { branch 'main' }
            steps {
                input message: 'Approve Production Deployment?'
                sh "gcloud run deploy ${IMAGE_NAME}-prod --image gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} --region ${REGION}"
            }
        }
    }

    post {
        failure {
            echo "Deployment failed. Initiating Rollback..."
            // Logic to re-deploy previous stable tag
        }
    }
}
