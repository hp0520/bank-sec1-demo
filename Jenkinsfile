pipeline {
  agent any
  environment {
    MAVEN_OPTS = "-Dmaven.test.failure.ignore=false"
    REPORT_DIR = "reports"
    ZAP_IMAGE = "owasp/zap2docker-stable"
    APP_PORT  = "8080"
    APP_URL   = "http://localhost:8080"
  }
  options { timestamps(); ansiColor('xterm') }
  stages {
    stage('Checkout') {
      steps {
        deleteDir()
        checkout scm
        sh 'mkdir -p $REPORT_DIR'
      }
    }
    stage('Build & Unit Tests') {
      steps { sh 'mvn -B -e clean verify' }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
          publishHTML(target: [reportName: 'JaCoCo Coverage', reportDir: 'target/site/jacoco', reportFiles: 'index.html', keepAll: true])
        }
      }
    }
    stage('SAST - SpotBugs + FindSecBugs') {
      steps { sh 'mvn -B spotbugs:spotbugs' }
      post {
        always {
          recordIssues tools: [spotBugs(pattern: '**/target/spotbugsXml.xml')]
          archiveArtifacts artifacts: 'target/spotbugsXml.xml', fingerprint: true
        }
      }
    }
    stage('SCA - OWASP Dependency-Check') {
      steps { sh 'mvn -B org.owasp:dependency-check-maven:check' }
      post {
        always {
          archiveArtifacts artifacts: 'target/dependency-check-report.*', fingerprint: true
          publishHTML(target: [reportName: 'Dependency-Check', reportDir: 'target', reportFiles: 'dependency-check-report.html', keepAll: true])
        }
      }
    }
    stage('Package App') {
      steps {
        sh 'mvn -q -DskipTests package'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }
    stage('Run App (Background)') {
      steps {
        sh '''
          nohup java -jar target/bank-sec-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
          echo $! > app.pid
          for i in {1..30}; do
            if nc -z localhost $APP_PORT; then echo "App up"; exit 0; fi
            sleep 2
          done
          echo "App did not start in time" && exit 1
        '''
      }
    }
    stage('DAST - ZAP Baseline Scan') {
      steps {
        sh '''
          docker pull $ZAP_IMAGE
          docker run --rm --net="host" -v "$(pwd)/$REPORT_DIR:/zap/wrk" \
            $ZAP_IMAGE zap-baseline.py -t $APP_URL -r zap_report.html -x zap_report.xml -I
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: "${REPORT_DIR}/zap_report.*", fingerprint: true
          publishHTML(target: [reportName: 'OWASP ZAP (Baseline)', reportDir: "${REPORT_DIR}", reportFiles: 'zap_report.html', keepAll: true])
        }
      }
    }
  }
  post {
    always {
      sh '''
        if [ -f app.pid ]; then
          kill $(cat app.pid) || true
          rm -f app.pid
        fi
      '''
    }
  }
}
