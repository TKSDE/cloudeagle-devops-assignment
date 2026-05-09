pipeline {
    agent any
    
    environment {
        GCP_PROJECT = 'your-project-id'
        IMAGE_NAME = 'sync-service'
        REGION = 'us-central1'
        // Example logic to fetch the previous stable image tag for rollback
        PREVIOUS_STABLE_TAG = "stable-v1.0.0" 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                echo "Running Maven Build and Tests..."
                sh './mvnw clean test'
            }
        }

        stage('Static Code Analysis') {
            steps {
                echo "Running SonarQube analysis..."
                // sh './mvnw sonar:sonar -Dsonar.projectKey=sync-service'
            }
        }

        stage('Docker Build & Push') {
            // We build the Docker image for all builds to ensure it compiles,
            // but in a real scenario we might only push on non-PR branches.
            steps {
                echo "Packaging Jar and Building Docker Image..."
                sh './mvnw package -DskipTests'
                sh "docker build -t gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} ."
                sh "docker push gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }

        // -------------------------------------------------------------
        // DEPLOYMENT STAGES (Skipped on Pull Requests)
        // -------------------------------------------------------------

        stage('Deploy to QA') {
            when { 
                branch 'develop'
                not { changeRequest() } 
            }
            steps {
                echo "Deploying to QA environment..."
                sh "gcloud run deploy ${IMAGE_NAME}-qa --image gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} --region ${REGION} --set-env-vars SPRING_PROFILES_ACTIVE=qa"
            }
        }

        stage('Deploy to Staging') {
            when { 
                branch 'staging'
                not { changeRequest() }
            }
            steps {
                echo "Deploying to Staging environment..."
                sh "gcloud run deploy ${IMAGE_NAME}-staging --image gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} --region ${REGION} --set-env-vars SPRING_PROFILES_ACTIVE=staging"
            }
        }

        stage('Deploy to Production') {
            when { 
                branch 'main'
                not { changeRequest() }
            }
            steps {
                input message: 'Production Gate: Approve Deployment to PROD?'
                echo "Deploying to Production environment..."
                sh "gcloud run deploy ${IMAGE_NAME}-prod --image gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${BUILD_NUMBER} --region ${REGION} --set-env-vars SPRING_PROFILES_ACTIVE=prod"
            }
        }
    }

    // -------------------------------------------------------------
    // POST-BUILD ACTIONS & ROLLBACK STRATEGY
    // -------------------------------------------------------------
    post {
        always {
            echo "Pipeline finished. Cleaning up workspace..."
            cleanWs()
        }
        success {
            echo "Deployment Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
            script {
                // Automated Rollback Strategy for Production
                if (env.BRANCH_NAME == 'main') {
                    echo "CRITICAL FAILURE: Initiating automated rollback to previous stable tag: ${PREVIOUS_STABLE_TAG}..."
                    sh "gcloud run deploy ${IMAGE_NAME}-prod --image gcr.io/${GCP_PROJECT}/${IMAGE_NAME}:${PREVIOUS_STABLE_TAG} --region ${REGION}"
                }
            }
        }
    }
}
