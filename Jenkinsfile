pipeline {
  agent {
    docker { image 'maven:3.9-eclipse-temurin-17' args '-u root' }
  }
  options { timestamps() }
  environment { MAVEN_OPTS = '-Dmaven.test.failure.ignore=true' }

  stages {
    stage('Build & Test') { steps { sh 'mvn -B -ntp clean verify' } }
    stage('Publish Test Reports') {
      steps { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
    }
    stage('Archive Artifacts') {
      steps { archiveArtifacts artifacts: 'target/**/*.jar, target/**/*.war', fingerprint: true }
    }
  }
  post { always { cleanWs() } }
}
