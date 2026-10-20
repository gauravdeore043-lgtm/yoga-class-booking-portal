// Yoga Class Booking Portal – pipeline as code
// Stages: Checkout -> Build -> Test (unit + Selenium) -> Package -> Deploy to Tomcat -> Smoke test
//         -> Docker Build -> Docker Push (registry) -> Deploy Container -> Container Health
// A failing test fails the Test stage, so Package and Deploy never run.

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        // Poll GitHub every minute; a new commit on main starts the pipeline.
        pollSCM('* * * * *')
    }

    parameters {
        choice(name: 'APP_ENVIRONMENT', choices: ['dev', 'qa', 'staging'],
               description: 'Environment label injected into the Tomcat deployment as a context parameter (shown in the app footer and /api/version)')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8090',
               description: 'Base URL of the Tomcat server used for the post-deployment smoke test')
        string(name: 'CONTEXT_PATH', defaultValue: 'yoga',
               description: 'Tomcat context path the WAR is deployed under')
        string(name: 'REGISTRY', defaultValue: 'localhost:5001',
               description: 'Docker registry the versioned image is pushed to (local registry or docker.io/<user>)')
        string(name: 'CONTAINER_PORT', defaultValue: '8085',
               description: 'Host port for the continuously deployed container')
    }

    environment {
        JAVA_HOME     = '/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home'
        PATH          = "/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
        CATALINA_BASE = '/opt/homebrew/opt/tomcat@10/libexec'
        MAVEN_OPTS    = '-Xmx768m'
        IMAGE_NAME    = 'yoga-portal'
        CD_CONTAINER  = 'yoga-portal-cd'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -5'
                script {
                    env.APP_VERSION = sh(returnStdout: true,
                            script: "sed -n 's:.*<version>\\([0-9][0-9.]*\\)</version>.*:\\1:p' pom.xml | sed -n 2p").trim()
                    env.IMAGE_TAG = "${env.APP_VERSION}-${env.BUILD_NUMBER}"
                    currentBuild.description = "v${env.APP_VERSION} -> image ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                }
                echo "Application version ${env.APP_VERSION}, image tag ${env.IMAGE_TAG}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -ntp clean compile'
            }
        }

        stage('Test') {
            steps {
                // Unit tests + Selenium WebDriver suite (headless Chrome)
                sh 'mvn -B -ntp test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
                    archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
                }
                failure {
                    echo 'Tests failed: deployment is blocked. Failure screenshots are archived with this build.'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -ntp -DskipTests package'
                archiveArtifacts artifacts: 'target/yoga-portal.war', fingerprint: true
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sh "APP_ENVIRONMENT=${params.APP_ENVIRONMENT} CONTEXT_PATH=${params.CONTEXT_PATH} ./scripts/deploy-tomcat.sh"
            }
        }

        stage('Smoke test') {
            steps {
                sh "./scripts/wait-for-health.sh ${params.TOMCAT_URL}/${params.CONTEXT_PATH} ${params.APP_ENVIRONMENT}"
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build --build-arg APP_VERSION=${APP_VERSION} \
                        -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                        -t ${REGISTRY}/${IMAGE_NAME}:latest .
                    docker image inspect ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                        --format 'Image {{.Id}} created {{.Created}} size {{.Size}} labels {{json .Config.Labels}}' | tee image-info.txt
                '''
            }
        }

        stage('Docker Push') {
            steps {
                sh '''
                    docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker push ${REGISTRY}/${IMAGE_NAME}:latest
                    echo "Registry tags:" | tee -a image-info.txt
                    curl -fsS http://${REGISTRY}/v2/${IMAGE_NAME}/tags/list | tee -a image-info.txt || true
                '''
                archiveArtifacts artifacts: 'image-info.txt', fingerprint: false
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                    docker pull ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker rm -f ${CD_CONTAINER} >/dev/null 2>&1 || true
                    docker run -d --name ${CD_CONTAINER} --restart unless-stopped \
                        -p ${CONTAINER_PORT}:8080 \
                        -e APP_ENVIRONMENT=${APP_ENVIRONMENT}-docker \
                        --label build=${BUILD_NUMBER} \
                        ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker ps --filter name=${CD_CONTAINER} --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
                '''
            }
        }

        stage('Container Health') {
            steps {
                sh "./scripts/wait-for-health.sh http://localhost:${params.CONTAINER_PORT} ${params.APP_ENVIRONMENT}-docker 150"
            }
        }
    }

    post {
        success {
            echo "Deployed: ${params.TOMCAT_URL}/${params.CONTEXT_PATH}/ (environment=${params.APP_ENVIRONMENT})"
            echo "Container: http://localhost:${params.CONTAINER_PORT}/ running ${params.REGISTRY}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline failed - nothing was deployed past the failing stage.'
        }
    }
}
