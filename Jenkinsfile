pipeline {
  agent any

  options { timestamps() }

  // If your agent doesn't have Maven in PATH, uncomment the tools block
  // and ensure a Maven installation with this exact name exists under
  // Manage Jenkins -> Tools.
  // tools { maven 'Maven-3.9' }

  environment {
    MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
  }

  stages {
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
    always { cleanWs() }
  }
}
