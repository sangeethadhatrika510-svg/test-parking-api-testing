# API Testing Template

Default Java API automation template using:

- Rest Assured for HTTP API calls
- Cucumber for BDD feature files
- TestNG as the test runner
- Maven for build and execution
- Environment-based configuration
- Cucumber HTML/JSON reports and Surefire XML reports

## Project Structure

```text
.
├── pom.xml
├── testng.xml
├── src
│   └── test
│       ├── java
│       │   └── com/example/com.example.api.api
│       │       ├── clients
│       │       ├── config
│       │       ├── context
│       │       ├── hooks
│       │       ├── runners
│       │       └── steps
│       └── resources
│           ├── config/env
│           └── features
└── target
```

## Run Tests

Run all tests:

```bash
mvn clean test
```

Run smoke tests:

```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
```

Run against a specific environment:

```bash
mvn clean test -Dtest.env=local
```

Run smoke tests against QA:

```bash
mvn clean test -Dtest.env=qa -Dcucumber.filter.tags="@smoke"
```

## Configuration

Environment files are under:

```text
src/test/resources/config/env
```

Available examples:

- `local.properties`
- `qa.properties`
- `dev.properties`

Default environment is `local`.

Example:

```properties
base.url=http://localhost:8080
request.timeout.ms=10000
```

You can override values from Maven:

```bash
mvn clean test -Dtest.env=qa -Dbase.url=https://qa.example.com
```

## Reports

After execution:

```text
target/cucumber-reports/cucumber.html
target/cucumber-reports/cucumber.json
target/surefire-reports
target/allure-results
target/allure-report
```

## GitHub Actions

The workflow at `.github/workflows/api-tests.yml` provides:

- Java 21 and Maven framework validation on pushes and pull requests to `main`.
- Manual API test runs with environment and Cucumber tag inputs.
- A daily scheduled test run at 06:00 UTC using the QA profile by default.
- Cucumber HTML/JSON, Allure HTML, and Surefire XML workflow artifacts.
- Allure publication through GitHub Pages for manual and scheduled test runs.

The environment profiles contain default direct service URLs. They can be overridden with repository secrets named `API_BASE_URL`, `AUTH_BASE_URL`, `LOCATION_BASE_URL`, `BOOKING_BASE_URL`, and `PAYMENT_BASE_URL`. All reports remain available as workflow artifacts. To publish Allure at a permanent URL, set **Settings > Pages > Build and deployment > Source** to **GitHub Actions** before running the workflow.

## How To Add A New API Test

1. Add a scenario in `src/test/resources/features`.
2. Add or update a client class in `src/test/java/com/example/com.example.api.api/clients`.
3. Add step definitions in `src/test/java/com/example/com.example.api.api/steps`.
4. Use tags such as `@smoke`, `@regression`, `@auth`, or service-specific tags.

## Sample Tags

```gherkin
@health @smoke
Scenario: Check API health
```

Recommended tag usage:

- `@smoke` for high-value quick checks
- `@regression` for full coverage
- `@health` for status endpoints
- `@auth` for authentication flows
- `@db` for database validation tests
