# Tasks 7–8 – Jenkins CI Job, Pipeline as Code and Tomcat Deployment

## Jenkins installation (macOS, Homebrew)

| Item | Value |
|---|---|
| Package | `jenkins-lts` (Homebrew), run as a launchd service with `brew services start jenkins-lts` |
| URL | http://localhost:8080 (bound to 127.0.0.1 only) |
| JENKINS_HOME | `~/.jenkins` |
| Configuration | **Configuration as Code** (`jenkins.yaml`) + **Job DSL**. Jobs are created from code, not by clicking. |
| Plugins | configuration-as-code, job-dsl, workflow-aggregator, git, junit, pipeline-stage-view, timestamper, ws-cleanup |
| Tools on the agent | Java 21 (`openjdk@21`), Maven 3.9, Git, Docker CLI (Colima) |

## Task 7 – CI build job `yoga-ci-build` (freestyle)

* **SCM:** GitHub repository, branch `*/main`
* **Trigger:** *Poll SCM* `* * * * *`. Jenkins checks GitHub every minute and builds when there is a new commit.
  The *Polling Log* page and the build cause "Started by an SCM change" are the trigger evidence.
* **Build step:** `mvn -B -ntp clean package -DskipTests`
* **Post-build:** archive `target/yoga-portal.war` (with fingerprint)

## Task 8 – Pipeline job `yoga-pipeline` (Jenkinsfile)

The pipeline is defined in [`Jenkinsfile`](../Jenkinsfile) at the root of the repo.

| Stage | What it does |
|---|---|
| Checkout | `checkout scm` from GitHub `main` |
| Build | `mvn clean compile` |
| Package | `mvn -DskipTests package`, then archives the WAR |
| Deploy to Tomcat | `scripts/deploy-tomcat.sh`: copies the WAR to `~/yoga-deploy/` and writes `conf/Catalina/localhost/yoga.xml` |
| Smoke test | `scripts/wait-for-health.sh`: waits for `/actuator/health = UP` and checks the environment in `/api/version` |

### Parameterised environment setting

| Parameter | Default | Effect |
|---|---|---|
| `APP_ENVIRONMENT` | `dev` (choices dev / qa / staging) | Written into the Tomcat context descriptor as the context parameter `app.environment`. It is shown in the app footer and in `/api/version`. |
| `TOMCAT_URL` | `http://localhost:8090` | Server used for the smoke test |
| `CONTEXT_PATH` | `yoga` | URL path of the deployment |

Running *Build with Parameters* with `APP_ENVIRONMENT=qa` redeploys the same WAR. The footer then shows `env: qa`,
and nothing in the code changes.

**Deployed URL:** http://localhost:8090/yoga/
