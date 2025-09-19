pipeline {
  agent any

  options { timestamps() }

  environment {
    MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
  }

  stages {
    stage('Checkout') {
      steps {
        sshagent(credentials: ['github-ssh']) {   // <-- uses Jenkins credential you just created
          sh '''
            set -e
            if [ ! -d ".git" ]; then
              git clone git@github.com:hp0520/bank-sec1-demo.git .
            else
              git remote set-url origin git@github.com:hp0520/bank-sec1-demo.git
            fi
            git fetch --all --prune
            git checkout -B main origin/main || git checkout main || true
          '''
        }
      }
    }

    stage('Verify Tooling') {
      steps {
        sh '''
          git --version
          mvn -version || (echo "Maven not found on agent. Install Maven or run on a Maven agent." && exit 1)
        '''
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
        archiveArtifacts artifacts: 'target/**/*.jar, target/**/*.war', fingerprint: true, onlyIfSuccessful: false
      }
    }
  }

  post {
    always { cleanWs() }
  }
}
