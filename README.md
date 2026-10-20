# Yoga Class Booking Portal

A Jenkins-based DevOps mini project. Members of a yoga studio can **register**, **view class slots
with live seats**, **request a booking**, **confirm** it, **cancel** it and **track its status**.
The app is built, tested with Selenium, deployed to Tomcat and Docker, and provisioned with
Ansible, all automatically.

**Author:** Gaurav Deore (23102C0074)

## Tech stack

Java 21 · Spring Boot 3.5 (Thymeleaf, JPA) · H2 · Maven · Tomcat 10.1 · Jenkins LTS ·
Selenium WebDriver 4 + JUnit 5 · Docker · Ansible

## Repository layout

```
.
├── src/main/java/com/yoga/portal   # application code (model, repository, service, web, api)
├── src/main/resources              # Thymeleaf templates, CSS, application.properties
├── src/test/java                   # unit tests and the Selenium suite
├── docs/                           # Task deliverables: scope, agile plan, SRS, policies, test plan…
├── .github/                        # issue templates and PR template
├── scripts/                        # deployment and Docker lifecycle helper scripts
├── Jenkinsfile                     # pipeline as code (added in Task 8)
├── Dockerfile                      # container image (added in Task 11)
└── ansible/                        # inventory + playbooks (added in Tasks 13–14)
```

## Run locally

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
mvn spring-boot:run          # http://localhost:8080
mvn test                     # unit + Selenium tests
mvn -DskipTests package      # target/yoga-portal.war
```

## Branching

`main` (released, protected) ← `develop` (integration) ← `feature/*`, `fix/*`, `ci/*`, `docs/*`.
See [docs/04-git-branching-policy.md](docs/04-git-branching-policy.md).

## Documentation

| Task | Document |
|---|---|
| 1 | [Problem definition and scope](docs/01-problem-definition-and-scope.md) |
| 2 | [Agile plan and DevOps workflow](docs/02-agile-plan-and-devops-workflow.md) |
| 3 | [SRS, architecture and setup](docs/03-srs-architecture-and-setup.md) |
| 4 | [Git branching policy](docs/04-git-branching-policy.md) |
| 7–8 | [Jenkins CI job, pipeline and Tomcat deployment](docs/07-jenkins-ci-and-pipeline.md) |
