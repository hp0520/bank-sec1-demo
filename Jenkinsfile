pipeline {
  agent any

  environment {
    REPORT_DIR = "reports"
    ZAP_IMAGE  = "owasp/zap2docker-stable"

    IMAGE_NAME = "bank-sec-demo"
    IMAGE_TAG  = "${BUILD_NUMBER}"
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

    stage('Build & Unit Tests (Dockerized Maven)') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            -v "$HOME/.m2":/root/.m2 \
            -w /workspace maven:3.9-eclipse-temurin-17 \
            mvn -B -e clean verify
        '''
      }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
          publishHTML(target: [
            reportName: 'JaCoCo Coverage',
            reportDir: 'target/site/jacoco',
            reportFiles: 'index.html',
            keepAll: true
          ])
        }
      }
    }

    stage('SAST - SpotBugs + FindSecBugs') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            -v "$HOME/.m2":/root/.m2 \
            -w /workspace maven:3.9-eclipse-temurin-17 \
            mvn -B spotbugs:spotbugs
        '''
      }
      post {
        always {
          // Warnings NG needs this step; install the plugin
          recordIssues tools: [spotBugs(pattern: '**/target/spotbugsXml.xml')]
          archiveArtifacts artifacts: 'target/spotbugsXml.xml', fingerprint: true
        }
      }
    }

    stage('SCA - OWASP Dependency-Check (12.1.x)') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            -v "$HOME/.m2":/root/.m2 \
            -w /workspace maven:3.9-eclipse-temurin-17 \
            mvn -B org.owasp:dependency-check-maven:12.1.3:check
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: 'target/dependency-check-report.*', fingerprint: true
          publishHTML(target: [
            reportName: 'Dependency-Check',
            reportDir: 'target',
            reportFiles: 'dependency-check-report.html',
            keepAll: true
          ])
        }
      }
    }

    stage('Package App (Dockerized Maven)') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            -v "$HOME/.m2":/root/.m2 \
            -w /workspace maven:3.9-eclipse-temurin-17 \
            mvn -q -DskipTests package
        '''
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Build Docker Image') {
      steps {
        sh '''
          docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
          docker images | grep ${IMAGE_NAME} || (echo "Image not built" && exit 1)
        '''
      }
    }

    stage('Run App (Container)') {
      steps {
        sh '''
          docker network rm zap-net 2>/dev/null || true
          docker network create zap-net

          docker rm -f bank-app 2>/dev/null || true
          docker run -d --name bank-app --network zap-net -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}

          # wait for app (use curl image inside the same network)
          for i in {1..30}; do
            docker run --rm --network zap-net curlimages/curl:8.8.0 -sSf http://bank-app:8080/users >/dev/null && exit 0
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
          docker run --rm --network zap-net -v "$(pwd)/$REPORT_DIR:/zap/wrk" \
            $ZAP_IMAGE zap-baseline.py -t http://bank-app:8080 -r zap_report.html -x zap_report.xml -I
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: "${REPORT_DIR}/zap_report.*", fingerprint: true
          publishHTML(target: [
            reportName: 'OWASP ZAP (Baseline)',
            reportDir: "${REPORT_DIR}",
            reportFiles: 'zap_report.html',
            keepAll: true
          ])
        }
      }
    }
  }

  post {
    always {
      sh '''
        docker rm -f bank-app 2>/dev/null || true
        docker network rm zap-net 2>/dev/null || true
      '''
    }
  }
}
