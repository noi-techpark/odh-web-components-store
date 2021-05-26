pipeline {
    agent any

    environment {
		COMPOSE_PROJECT_NAME = "webcompstore"
        DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/webcompstore'
        DOCKER_TAG = "test-$BUILD_NUMBER"
        API_SERVER_PORT = "1200"
		CDN_SERVER_PORT = "1201"
		ANSIBLE_LIMIT = "test"

		LIGHTHOUSE_API_KEY = credentials('webcompstore-lighthouse-api-key-test')
        DEBUG_LEVEL = "INFO"
		WORKSPACE_PATH = "/workspace"

		DB_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/webcompstore"
		DB_USR = credentials('webcompstore-test-postgres-username')
		DB_PWD = credentials('webcompstore-test-postgres-password')
		DB_SCHEMA = "public"

		// Endpoint from where we create a standalone preview inside a simple HTML skeleton
		API_PREVIEW_URL = "https://api.webcomponents.opendatahub.testingmachine.eu/preview"

		// Base URL from where we will distribute build artifacts (javascript bundles and assets)
		CDN_DIST_URL = "https://cdn.webcomponents.opendatahub.testingmachine.eu/dist"

		// Security for CRUD operations
		KEYCLOAK_URL = "https://auth.opendatahub.testingmachine.eu/auth/"
		KEYCLOAK_SSL_REQUIRED = "none"
		KEYCLOAK_REALM = "noi"
		KEYCLOAK_CLIENT_ID = "it.bz.opendatahub.webcomponents.api"
		KEYCLOAK_CLIENT_SECRET = credentials('webcompstore-keycloak-client-secret-test')

		// SMTP configuration
		SMTP_PWD = credentials('webcompstore-test-smtp-password')
		SMTP_USR = "no-reply@testingmachine.eu"
		SMTP_TO = "help@opendatahub.bz.it"
		SMTP_FROM = "no-reply@testingmachine.eu"
		SMTP_SUBJECT = "Web Component Store: Contact form (TEST PLEASE IGNORE)"
		SMTP_HOST = "smtp.eu.mailgun.org"
		SMTP_PORT = "587"
		SMTP_DEBUG = "true"
    }

    stages {
        stage('Configure') {
            steps {
                sh """
					cd backend/infrastructure/docker
                    rm -f .env
                    echo 'COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME}' >> .env
                    echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                    echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env
                    echo 'API_SERVER_PORT=${API_SERVER_PORT}' >> .env
					echo 'CDN_SERVER_PORT=${CDN_SERVER_PORT}' >> .env

					echo 'DEBUG_LEVEL=${DEBUG_LEVEL}' >> .env
					echo 'WORKSPACE_PATH=${WORKSPACE_PATH}' >> .env
					echo 'DB_URL=${DB_URL}' >> .env
					echo 'DB_SCHEMA=${DB_SCHEMA}' >> .env
					echo 'API_PREVIEW_URL=${API_PREVIEW_URL}' >> .env
					echo 'CDN_DIST_URL=${CDN_DIST_URL}' >> .env
					echo 'KEYCLOAK_URL=${KEYCLOAK_URL}' >> .env
					echo 'KEYCLOAK_SSL_REQUIRED=${KEYCLOAK_SSL_REQUIRED}' >> .env
					echo 'KEYCLOAK_REALM=${KEYCLOAK_REALM}' >> .env
					echo 'KEYCLOAK_CLIENT_ID=${KEYCLOAK_CLIENT_ID}' >> .env
					echo 'SMTP_USR=${SMTP_USR}' >> .env
					echo 'SMTP_TO=${SMTP_TO}' >> .env
					echo 'SMTP_FROM=${SMTP_FROM}' >> .env
					echo 'SMTP_SUBJECT=${SMTP_SUBJECT}' >> .env
					echo 'SMTP_HOST=${SMTP_HOST}' >> .env
					echo 'SMTP_PORT=${SMTP_PORT}' >> .env
					echo 'SMTP_DEBUG=${SMTP_DEBUG}' >> .env
				"""
				// Security fix: See https://jenkins.io/redirect/groovy-string-interpolation
				sh '''
					cd backend/infrastructure/docker
					echo "LIGHTHOUSE_API_KEY=$LIGHTHOUSE_API_KEY" >> .env
					echo "DB_USR=$DB_USR" >> .env
					echo "DB_PWD=$DB_PWD" >> .env
					echo "KEYCLOAK_CLIENT_SECRET=$KEYCLOAK_CLIENT_SECRET" >> .env
					echo "SMTP_PWD=$SMTP_PWD" >> .env
				'''
			}
		}
		stage('Test') {
            steps {
                sh '''
					cd backend
                    docker network create authentication || true

					docker-compose --no-ansi -f infrastructure/docker/docker-compose.test.yml build --pull --build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)
                    docker-compose --no-ansi -f infrastructure/docker/docker-compose.test.yml run --rm --no-deps -u $(id -u jenkins):$(id -g jenkins) api "mvn -B install test"
                '''
            }
        }
        stage('Build') {
            steps {
                sh '''
					cd backend
                    aws ecr get-login --region eu-west-1 --no-include-email | bash

                    docker-compose --no-ansi -f infrastructure/docker/docker-compose.build.yml build --pull

					docker-compose --no-ansi -f infrastructure/docker/docker-compose.buildapi.yml build --pull
					docker-compose --no-ansi -f infrastructure/docker/docker-compose.buildcdn.yml build --pull

                    docker-compose --no-ansi -f infrastructure/docker/docker-compose.buildapi.yml push
                    docker-compose --no-ansi -f infrastructure/docker/docker-compose.buildcdn.yml push
                '''
            }
        }
        stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
                        cd backend/infrastructure/ansible
                        ansible-galaxy install -f -r requirements.yml
                        ansible-playbook --limit=${ANSIBLE_LIMIT} deploy.yml --extra-vars "release_name=${BUILD_NUMBER}"
                    """
                }
            }
        }
	}
}
