# Task 9 – Selenium Test Plan

## Scope

End-to-end UI tests of the critical user journeys, run with Selenium WebDriver 4 + JUnit 5 against
the full application (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) in headless Chrome.
Selenium Manager downloads a matching ChromeDriver, and a Chrome-for-Testing build if Chrome is
missing, so no manual driver setup is needed.

## Critical user journeys

| ID | Journey | Test class / method | Key assertions |
|---|---|---|---|
| J1 | Registration | `RegistrationSeleniumTest` (3 tests) | Redirect to `/slots`; welcome message with name; name in nav; duplicate email shows error; invalid email/phone show field errors |
| J2 | Slot availability | `SlotAvailabilitySeleniumTest` | ≥ 6 classes listed with title, instructor, time and numeric seats left; sign-in hint for visitors |
| J3 | Request + confirm booking | `BookingFlowSeleniumTest` (2 tests) | Reference matches `YB-[A-Z0-9]{6}`; status REQUESTED; seats drop by 1; Confirm → CONFIRMED; listed in *My Bookings*; double booking blocked |
| J4 | Cancellation | `CancellationSeleniumTest` | Cancel → CANCELLED; Cancel button gone; **seat count restored** |
| J5 | Status tracking | `StatusTrackingSeleniumTest` (2 tests) | Lower-case reference accepted; shows status and member; unknown reference shows "No booking found" |

## Test data

* Every test creates its **own member** with a unique email (`<name>.<timestamp>.<n>@example.com`), so tests are independent and can run in any order.
* Class slots come from the seeded timetable (`DataSeeder`): 7 classes, capacities 1–20.
* Each Spring test context gets its own in-memory H2 database.

## Failure screenshot mechanism

`ScreenshotOnFailureExtension` (a JUnit 5 `TestWatcher`) runs when a test fails and saves:

* `target/screenshots/<TestClass>_<method>_<timestamp>.png`, a screenshot of the browser
* `target/screenshots/<TestClass>_<method>_<timestamp>.html`, the page source with its URL and the failure message

Jenkins archives `target/screenshots/**` on every run. To see the mechanism on purpose, run:

```bash
mvn test -Dtest=FailureScreenshotDemoSeleniumTest -Ddemo.failure=true
```

## How to run

```bash
mvn test                                  # unit + Selenium suite, headless
mvn test -Dselenium.headless=false        # watch the browser
mvn test -Dtest='*SeleniumTest'           # Selenium only
```

Reports are written to `target/surefire-reports/` (JUnit XML + text), and Jenkins publishes them.

## Entry and exit criteria

* **Entry:** the build compiles; Chrome is available or downloadable.
* **Exit:** all journeys pass. Any failure fails the Jenkins *Test* stage, which blocks deployment.

---

# Task 10 – Continuous Testing in Jenkins

* The **Test** stage of the [`Jenkinsfile`](../Jenkinsfile) runs `mvn test` (unit + Selenium) on every pipeline run.
* `junit 'target/surefire-reports/*.xml'` publishes the **Jenkins test report**, with per-test results and a trend chart.
* `archiveArtifacts 'target/screenshots/**'` keeps failure screenshots with the build.
* **Deployment gate.** In declarative pipelines a failed stage skips all later stages, so *Package*, *Deploy to Tomcat* and the Docker stages
  do not run when any test fails.

## Defect drill

1. A deliberate defect is committed: cancelling a booking no longer releases the seat (`BookingService.cancel`).
2. Jenkins runs. `CancellationSeleniumTest` and `BookingServiceTest.cancelReleasesSeat` fail, the build is **FAILED** and nothing is deployed
   (the Tomcat app keeps serving the previous good build).
3. The fix is committed as `fix(booking): release seat when a booking is cancelled`.
4. Jenkins reruns automatically (SCM poll). All tests pass and the build is deployed.
