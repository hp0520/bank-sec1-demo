# bank-sec-demo

Minimal Spring Boot Java app + Jenkins pipeline for SAST (SpotBugs/FindSecBugs), SCA (OWASP Dependency-Check) and DAST (OWASP ZAP baseline).

## Quickstart (local)
```bash
mvn -B -DskipTests clean package
mvn -B verify
mvn -q spring-boot:run
# Try endpoints:
curl "http://localhost:8080/users"
curl "http://localhost:8080/echo?msg=test"
curl "http://localhost:8080/user?id=1"
```

## Jenkins
Pipeline stages: Checkout → Build/Unit Tests → SAST → SCA → Package → Run → DAST → Archive reports
