// Yoga Class Booking Portal – pipeline as code
// Stages: Checkout -> Build -> Package -> Deploy to Tomcat -> Smoke test

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
    }

    environment {
        JAVA_HOME     = '/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home'
        PATH          = "/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
        CATALINA_BASE = '/opt/homebrew/opt/tomcat@10/libexec'
        MAVEN_OPTS    = '-Xmx768m'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -5'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -ntp clean compile'
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
    }

    post {
        success {
            echo "Deployed: ${params.TOMCAT_URL}/${params.CONTEXT_PATH}/ (environment=${params.APP_ENVIRONMENT})"
        }
        failure {
            echo 'Pipeline failed - nothing was deployed past the failing stage.'
        }
    }
}
