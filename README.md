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
```

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
