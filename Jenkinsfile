pipeline {
    agent any

    options {
        timestamps()     // add timestamps to logs
    }

    environment {
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
    }

    stages {
        stage('Checkout') {
            steps {
                // Use your Jenkins SSH credential ID here
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],  // change to */master if needed
                    userRemoteConfigs: [[
                        url: 'git@github.com:hp0520/bank-sec1-demo.git',
                        credentialsId: 'github-ssh'   // <-- update if your ID is different
                    ]]
                ])
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn -B -ntp clean verify'
            }
        }

        stage('Publish Test Reports') {
            steps {
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/**/*.jar, target/**/*.war', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()   // clean workspace after each build
        }
        failure {
            echo '❌ Build failed. Check logs for details.'
        }
        success {
            echo '✅ Build succeeded!'
        }
    }
}
