# Public API Test Automation Suite

Automated API testing project built with **REST Assured**, **Java**, and **TestNG**. This project validates the [FakeRestAPI](https://fakerestapi.azurewebsites.net) across multiple test categories — smoke, functional, integration, performance, and regression — to ensure comprehensive API quality coverage.

## Project Structure

```
src/test/java/
├── base/
│   └── BaseSetup.java              # Test configuration and report initialization
├── services/
│   └── ActivityService.java        # Reusable API request methods (CRUD operations)
├── data/
│   └── RequestDataFactory.java     # Test data builders for request payloads
├── utils/
│   └── ResponseValidator.java      # Shared assertion and validation helpers
└── tests/
    ├── smoke/
    │   └── SmokeTestSuite.java     # Quick health-check tests
    ├── functional/
    │   └── FunctionalTestSuite.java # Detailed endpoint validation
    ├── integration/
    │   └── IntegrationTestSuite.java # Multi-endpoint workflow tests
    ├── performance/
    │   └── PerformanceTestSuite.java # Load, stress, and throughput tests
    └── regression/
        └── RegressionTestSuite.java  # Regression and simulated change tests
```

## Test Categories

### Smoke Tests (6 tests)
Quick verification that the most critical API operations are functional. These are designed to run frequently and give fast feedback on API health.
- API reachability check
- Create, Read (single + all), Update, and Delete operations

### Functional Tests (9 tests)
Thorough validation of each endpoint's behavior, including positive and negative scenarios.
- CRUD operations with valid and invalid data
- Response structure and header validation
- Parameterized testing using `@DataProvider` for multiple activity IDs
- Boundary/edge case testing (minimal payload, non-existent ID)

### Integration Tests (5 tests)
Tests that validate how multiple endpoints work together in real-world workflows.
- Create -> Retrieve workflow
- Create -> Update -> Retrieve workflow
- Create -> Delete -> Verify removal workflow
- Activity count consistency across endpoints
- Cross-endpoint data consistency validation

### Performance Tests (6 tests)
Assessment of API responsiveness and behavior under load.
- **Response Time Benchmarks**: Individual endpoint timing against thresholds
- **Concurrent Load Test**: 10 concurrent users, 5 requests each — measures average response time and success rate
- **Stress Test**: Gradually increases concurrent threads (5 -> 10 -> 20 -> 30 -> 50) to identify the breaking point
- **Throughput Measurement**: Sequential request rate (requests/second)

### Regression Tests (10 tests)
Ensures existing functionality is not broken by changes. Includes simulated code change scenarios.
- Re-validation of all CRUD operations
- Response structure stability checks
- **Simulated Code Change Tests**:
  - Verifies the `title` field hasn't been renamed
  - Verifies the status code contract for POST (200) hasn't changed
  - Verifies the DELETE behavior (200) hasn't changed

## Regression Testing Process

The regression suite demonstrates what happens when a developer introduces a breaking change:

1. **Baseline Run**: Execute the full regression suite — all 10 tests pass, confirming the API contract is intact.
2. **Simulate a Change**: The `simulatedChange*` tests explicitly check for specific contract details (field names, status codes). If the API were to change (e.g., rename `title` to `name`, or return 201 instead of 200 for creation), these tests would immediately fail.
3. **Detection**: A failing regression test pinpoints exactly what changed and what broke, making it easy to identify and fix the issue before deployment.
4. **Documentation**: Results are captured in the ExtentReport HTML file with detailed pass/fail information for each test.

## Prerequisites

- Java 22 or higher
- Maven 3.x
- Internet connection (tests call a live API)

## Running the Tests

Run the entire test suite:
```bash
mvn clean test
```

Run from the TestNG XML file:
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testNG.xml
```

## Test Reports

After execution, the HTML report is generated at:
```
reports/TestExecutionReport.html
```

The report is powered by **ExtentReports** and includes:
- Test pass/fail status for each category
- Detailed step-by-step logs per test
- Performance metrics and load test results
- System information (tester, project, environment, Java version)

Application logs are written to `logs/api-test.log`.

## CI/CD

A GitHub Actions pipeline (`.github/workflows/maven.yml`) automatically runs the test suite on every push and pull request to `main`. The test report is uploaded as a build artifact.

## Tools and Technologies

| Tool | Purpose |
|------|---------|
| Java 22 | Programming language |
| REST Assured 5.5.0 | API testing library |
| TestNG 7.10.2 | Test framework and runner |
| ExtentReports 5.1.2 | HTML test reporting |
| Log4j 2.20.0 | Logging framework |
| Maven | Build and dependency management |
| GitHub Actions | CI/CD pipeline |
