# Web Services Automation — GitHub API Test Framework

> **Course:** Test Automation  
> **Module:** Web Services Automation  
> **API under test:** [GitHub REST API](https://docs.github.com/en/rest)

---

## What This Project Does

This project demonstrates how to build an automated test framework for a **REST API** from scratch using Java. It covers the full cycle: sending HTTP requests, validating responses (status codes, headers, body), deserializing JSON, and running tests in a CI/CD pipeline with Jenkins.

All tests run against the real GitHub public API — no mocks, no stubs.

---

## Setup

### 1. Prerequisites
- Java 8+
- Maven 3.6+
- A GitHub account with a [Personal Access Token](https://github.com/settings/tokens) (scopes: `repo`, `delete_repo`)

### 2. Configure credentials
Open `src/main/resources/config.properties` and fill in your values:

```properties
base_url=https://api.github.com
token=<your_github_token>
username=<your_github_username>
password=<your_github_password>
```

### 3. Run the tests
```bash
mvn test -Dsurefire.suiteXmlFiles=testng.xml
```

---

## Project Structure

```
GetStartedWithTestingAPI-master/
│
├── pom.xml                          # Maven build — dependencies and plugins
├── testng.xml                       # Regression suite configuration
├── Jenkinsfile                      # CI/CD pipeline definition
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── entities/
│   │   │   │   └── BaseClass.java           # Parent class for all API tests
│   │   │   │
│   │   │   ├── dto/                         # Data Transfer Objects (POJOs)
│   │   │   │   ├── User.java                # Maps GitHub user response
│   │   │   │   ├── NotFound.java            # Maps 404 error response
│   │   │   │   └── RateLimit.java           # Maps rate limit response (nested JSON)
│   │   │   │
│   │   │   └── utils/
│   │   │       └── ResponseUtils.java       # Reusable response helper methods
│   │   │
│   │   └── resources/
│   │       ├── config.properties            # base_url, token, credentials
│   │       └── log4j2.xml                   # Logging configuration (main)
│   │
│   └── test/
│       ├── java/
│       │   └── service/                     # API test classes
│       │       ├── Get200.java              # GET — happy path (200 OK)
│       │       ├── Get401.java              # GET — unauthorized (401), @DataProvider
│       │       ├── Get404.java              # GET — not found (404)
│       │       ├── Options204.java          # OPTIONS — allowed methods (204)
│       │       ├── DeleteAndPost.java       # POST (201) + DELETE (204)
│       │       ├── ResponseHeaders.java     # Header value assertions
│       │       └── BodyTestWithSimpleMap.java  # JSON body deserialization tests
│       │
│       └── resources/
│           ├── log4j2.xml                   # Logging config (active during test runs)
│           └── ...
│
└── logs/
    └── test-run.log                         # Generated on each test run (git-ignored)
```

---

## Key Concepts

### 1. BaseClass — The Foundation of Every Test

`entities/BaseClass.java` is the parent class that all API test classes extend. It centralises shared logic so you never repeat yourself.

**Lifecycle hooks** (run automatically by TestNG):

```java
@BeforeTest   suiteSetup()       // logs suite start
@AfterTest    suiteTeardown()    // logs suite end
@BeforeMethod setup()            // creates a fresh HttpClient before each test
@AfterMethod  closeResources()   // closes client and response after each test
```

**HTTP request factory methods:**

```java
createGetRequest(String url)
createPostRequest(String url, String jsonBody)
createDeleteRequest(String url)
createOptionsRequest(String url)
createPutRequest(String url, String jsonBody)
addTokenAuth(HttpRequestBase request)   // adds Authorization: token ...
```

**Utilities:**
```java
getConfigProperty(String key)                        // reads from config.properties
assertEqualsIgnoreCase(String actual, String expected) // case-insensitive assertion
```

> **Why extend BaseClass?**  
> If the way you create the HTTP client changes, you update it in one place — not in every test file.

---

### 2. DTO Package — Mapping JSON to Java Objects

A **DTO (Data Transfer Object)** is a plain Java class that mirrors the structure of a JSON response. Jackson reads the JSON body and populates the fields automatically — this is called **deserialization**.

**Example — GitHub user response:**
```json
{ "login": "olgadarii", "id": 29649900 }
```
```java
// dto/User.java
public class User {
    private String login;
    private int id;
    // getters...
}
```

**Nested JSON** uses `@JsonProperty` to map a nested structure to flat fields:
```java
// dto/RateLimit.java
@JsonProperty("resources")
private void unmarshallNested(Map<String, Object> resources) {
    Map<String, Integer> core = (Map<String, Integer>) resources.get("core");
    coreLimit = core.get("limit");
}
```

---

### 3. Utils Package — Reusable Response Helpers

`utils/ResponseUtils.java` contains static helper methods for common operations on HTTP responses:

| Method | What it does |
|--------|-------------|
| `getHeader(response, name)` | Returns a header value (loop-based) |
| `getHeaderJava8Way(response, name)` | Same, using Java 8 Streams |
| `headerIsPresent(response, name)` | Returns `true`/`false` |
| `unmarshall(response, Class)` | Deserializes JSON → specific DTO |
| `unmarshallGeneric(response, Class<T>)` | Deserializes JSON → any DTO (generic) |

---

### 4. HTTP Methods Tested

| Class | Method | Endpoint | Expected Status |
|-------|--------|----------|-----------------|
| `Get200` | GET | `/`, `/rate_limit`, `/search/repositories` | 200 |
| `Get401` | GET | `/user`, `/user/followers`, `/notifications` | 401 |
| `Get404` | GET | `/nonexistingurl` | 404 |
| `Options204` | OPTIONS | `/` | 204 |
| `DeleteAndPost` | POST | `/user/repos` | 201 |
| `DeleteAndPost` | DELETE | `/repos/{username}/hello-world` | 204 |

---

### 5. TestNG Annotations

| Annotation | Scope | Used in |
|------------|-------|---------|
| `@BeforeTest` | Once before all tests in a `<test>` block | `BaseClass` |
| `@AfterTest` | Once after all tests in a `<test>` block | `BaseClass` |
| `@BeforeMethod` | Before **each** test method | `BaseClass` |
| `@AfterMethod` | After **each** test method | `BaseClass` |
| `@Test` | Marks a method as a test | All test classes |
| `@Test(priority=N)` | Controls execution order within a class | `DeleteAndPost` |
| `@DataProvider` | Supplies parameter sets to a `@Test` | `Get401` |

**Example — parameterized test with `@DataProvider`:**
```java
@DataProvider
private Object[][] endpoints() {
    return new Object[][]{
        {"/user"},
        {"/user/followers"},
        {"/notifications"}
    };
}

@Test(dataProvider = "endpoints")
public void userReturns401(String endpoint) throws IOException {
    // runs 3 times, once per endpoint
}
```

---

### 6. Regression Suite — testng.xml

`testng.xml` defines which tests run and how:

```xml
<suite name="Test Suite" parallel="classes" thread-count="5">
    <test name="Service Tests">
        <classes>
            <class name="service.BodyTestWithSimpleMap"/>
            <class name="service.Get200"/>
            <class name="service.Get401"/>
            <class name="service.Get404"/>
            <class name="service.DeleteAndPost"/>
        </classes>
    </test>
</suite>
```

- `parallel="classes"` — different test classes run simultaneously on separate threads
- `thread-count="5"` — up to 5 classes run in parallel

---

### 7. Configuration File

`src/main/resources/config.properties` keeps environment-specific values out of the test code:

```properties
base_url=https://api.github.com
token=<your_github_token>
username=<your_github_username>
password=<your_github_password>
```

Loaded once in `BaseClass` via `java.util.Properties` and exposed through `getConfigProperty(key)`.

---

### 8. Logging — Log4j 2

Configured in `src/test/resources/log4j2.xml`. Every test run writes to two destinations simultaneously:

| Destination | Location | Format |
|-------------|----------|--------|
| Console | Terminal | Colored by log level |
| File | `logs/test-run.log` | Plain text, overwritten each run |

**Color scheme:**

| Level | Color |
|-------|-------|
| `FATAL` | Red blinking |
| `ERROR` | Red bold |
| `WARN` | Yellow bold |
| `INFO` | Green |
| `DEBUG` | Cyan |
| `TRACE` | White |

**Usage in test code:**
```java
private static final Logger logger = LogManager.getLogger(Get200.class);
logger.info("Executing GET request to " + BASE_ENDPOINT);
```

---

### 9. CI/CD — Jenkins Pipeline

`Jenkinsfile` defines a 3-stage pipeline:

```
Checkout  →  Build  →  Test
```

| Stage | Command | What happens |
|-------|---------|-------------|
| Checkout | — | Pulls latest code from git |
| Build | `mvn compile` | Compiles all source files |
| Test | `mvn test` | Runs regression suite, publishes results |

After a build, TestNG test results appear as a trend graph on the Jenkins job page.

---

## Running Tests

**Terminal (from project root):**
```bash
mvn test -Dsurefire.suiteXmlFiles=testng.xml
```

**Jenkins:**
1. Open `http://localhost:8080`
2. Click **GetStartedWithTestingAPI-master**
3. Click **Build Now**

**Reports:**
- Surefire HTML: `target/site/surefire-report.html`
- JUnit XML: `target/surefire-reports/`
- Log file: `logs/test-run.log`

---

## Checklist — Self-Verification

| Requirement | Status | Where to find it |
|-------------|--------|-----------------|
| Maven project | ✅ | `pom.xml` |
| `src/test/java`, `src/main/java`, `resources` structure | ✅ | Project structure above |
| Separate `dto` package | ✅ | `src/main/java/dto/` |
| Separate `utils` package | ✅ | `src/main/java/utils/` |
| Apache HttpClient dependency | ✅ | `pom.xml` — `httpclient 4.5.9` |
| TestNG dependency | ✅ | `pom.xml` — `testng 7.4.0` |
| Jackson dependency | ✅ | `pom.xml` — `jackson-databind 2.15.2` |
| Request creation methods in BaseClass | ✅ | `entities/BaseClass.java` |
| `@BeforeTest` / `@AfterTest` in BaseClass | ✅ | `entities/BaseClass.java` |
| `@BeforeMethod` / `@AfterMethod` in BaseClass | ✅ | `entities/BaseClass.java` |
| Headers tested | ✅ | `service/ResponseHeaders.java` |
| GET tests | ✅ | `Get200`, `Get401`, `Get404` |
| POST test | ✅ | `DeleteAndPost.createRepoReturns201` |
| OPTIONS test | ✅ | `Options204` |
| DELETE test | ✅ | `DeleteAndPost.deleteIsSuccessful` |
| Status codes verified (200, 201, 204, 401, 404) | ✅ | All service test classes |
| JSON deserialization | ✅ | `utils/ResponseUtils.java` + `dto/` |
| `@Test` annotation | ✅ | All service test classes |
| Regression suite (`testng.xml`) | ✅ | `testng.xml` |
| `@DataProvider` parameterization | ✅ | `service/Get401.java` |
| Log4j logging with file output | ✅ | `src/test/resources/log4j2.xml` |
| Git version control | ✅ | `.git/` on `master` branch |
| Config file for environment variables | ✅ | `src/main/resources/config.properties` |
| Tests run without manual intervention | ✅ | Maven Surefire Plugin |
| TestNG reporting | ✅ | `maven-surefire-report-plugin` |
| Jenkins CI/CD | ✅ | `Jenkinsfile` |
