pipeline {
    agent any
    stages {
        stage('Push To Sonar') {
            steps {
                sh '''echo Pushing to sonar'''
            }
        }

        stage('Unit Testing') {
            steps {
                sh '''echo testing'''
            }
        }

        stage('Build') {
            steps {
                sh '''echo building'''
            }
        }

        stage('Deploy To SIT') {
            steps {
                sh '''echo "Deploying to SIT"'''
            }
        }

        stage('Integration Testing') {
            steps {
                sh '''echo "Running integration tests"'''
            }
        }

        stage('Deploy To UAT') {
            steps {
                sh '''echo "Deploying to UAT"'''
            }
        }

        stage('UA Testing') {
            steps {
                sh '''echo "Confirming user acceptance testing"'''
            }
        }

        stage('Deploy To Production') {
            steps {
                sh '''echo "Deploying to production..."'''
            }
        }
    }
}
