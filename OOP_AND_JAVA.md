# OOP & Java Concepts in This Framework

```
       ,
       \`-._           __
        \\  `-..____,.'  `.
         :`.         /    \`.
         :  )       :      : \
          ;'        '   ;  |  :
          )..      .. .:.`.;  :
         /::...  .:::...   ` ;
         ; _ '    __        /:\
         `:o>   /\o_>      ;:. `.
        `-`.__ ;   __..--- /:.   \
        === \_/   ;=====_.':.     ;
         ,/'`--'...`--....        ;
              ;                    ;
            .'                      ;
          .'                        ;
        .'     ..     ,      .       ;
       :       ::..  /      ;::.     |
      /      `.;::.  |       ;:..    ;
     :         |:.   :       ;:.    ;
     :         ::     ;:..   |.    ;
      :       :;      :::....|     |
      /\     ,/ \      ;:::::;     ;
    .:. \:..|    :     ; '.--|     ;
   ::.  :''  `-.,,;     ;'   ;     ;
.-'. _.'\      / `;      \,__:      \
`---'    `----'   ;      /    \,.,,,/
                   `----`              fsc
```

> **Course:** Test Automation — Web Services Automation module  
> This document maps the four OOP pillars and core Java concepts to the actual code in this project. Use it as a reference when studying the framework.

---

## The Four OOP Pillars

### 1. Encapsulation
> Hide internal details. Expose only what the outside world needs.

Encapsulation appears in two places in this project.

**`BaseClass`** hides the HTTP client, response object, and config loading. Test classes never touch those internals directly — they call named methods instead.

```java
// BaseClass.java
private static final Properties config = loadConfig();  // hidden from everyone
protected CloseableHttpClient client;                   // shared with subclasses only
protected CloseableHttpResponse response;

// Test classes call a method — they don't know how the request is built
HttpGet get = createGetRequest(BASE_ENDPOINT + "/users/olgadarii");
```

**DTOs (`User`, `NotFound`, `RateLimit`)** hide their fields behind getters. No one can read `user.login` directly — they must go through `user.getLogin()`.

```java
// dto/User.java
public class User {
    private String login;   // hidden — no direct access from outside
    private int id;

    public String getLogin() { return login; }  // controlled access
    public int getId()       { return id; }
}
```

**Why it matters:** if the internal representation changes (e.g. `login` becomes `username`), you update the class in one place. All callers using the getter are unaffected.

---

### 2. Inheritance
> Share behaviour across related classes through a parent–child relationship.

All seven test classes extend `BaseClass`. They get the lifecycle hooks, the HTTP factory methods, and the config reader for free — without copy-pasting a single line.

```java
public class Get200 extends BaseClass {

    @Test
    public void rootEndpointReturns200() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/");  // inherited from BaseClass
        response = client.execute(get);                       // inherited field
        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }
}
```

**Inheritance chain:**
```
ApiClient (interface)
    ↑ implements
BaseClass
    ↑ extends
Get200 / Get401 / Get404 / DeleteAndPost / Options204 / ResponseHeaders / BodyTestWithSimpleMap
```

**Why it matters:** change the HTTP client once in `BaseClass` — all test classes pick it up automatically.

---

### 3. Abstraction
> Define *what* something does without committing to *how*.

`ApiClient` is a pure interface — it lists the six HTTP operations the framework must support, with no implementation details.

```java
// ApiClient.java — the contract
public interface ApiClient {
    HttpGet     createGetRequest(String url);
    HttpPost    createPostRequest(String url, String jsonBody);
    HttpDelete  createDeleteRequest(String url);
    HttpOptions createOptionsRequest(String url);
    HttpPut     createPutRequest(String url, String jsonBody);
    void        addTokenAuth(HttpRequestBase request);
}
```

`BaseClass` provides the concrete implementation. Test classes depend on the contract, not the internals.

**DTOs are also an abstraction layer.** They hide the raw JSON string behind a typed Java object:

```java
// Without abstraction — raw, fragile string parsing
String json = EntityUtils.toString(response.getEntity());
String login = json.split("\"login\":\"")[1].split("\"")[0];

// With abstraction — clean, typed
User user = ResponseUtils.unmarshall(response, User.class);
String login = user.getLogin();
```

---

### 4. Polymorphism
> One interface, many forms.

Because `BaseClass implements ApiClient`, any `BaseClass` instance can be treated as an `ApiClient`. If you wrote a second implementation (e.g., using RestAssured instead of Apache HttpClient), it could satisfy the same contract and no test code would change.

**Method-level polymorphism** also appears in `ResponseUtils` via generics:

```java
// Works for any DTO type — same method, different T
public static <T> T unmarshallGeneric(CloseableHttpResponse response, Class<T> clazz)

// Call it with User, NotFound, or RateLimit — all valid
User user        = ResponseUtils.unmarshallGeneric(response, User.class);
NotFound error   = ResponseUtils.unmarshallGeneric(response, NotFound.class);
RateLimit limits = ResponseUtils.unmarshallGeneric(response, RateLimit.class);
```

One method, many types.

---

## Java Concepts

### Access Modifiers
Controls who can see and use a field or method.

| Modifier | Used on | Who can access |
|----------|---------|----------------|
| `private` | DTO fields, `config`, `loadConfig()` | That class only |
| `protected` | `client`, `response`, `BASE_ENDPOINT` | `BaseClass` + all subclasses |
| `public` | Interface methods, `getConfigProperty()` | Everyone |

```java
private static final Properties config = loadConfig();  // BaseClass only
protected CloseableHttpClient client;                   // visible to all test classes
public HttpGet createGetRequest(String url) { ... }     // required public by ApiClient
```

---

### Static Members
Belong to the class itself, not to any instance. Loaded once, shared across all objects.

```java
// Loaded once when the class is first used — not per test run
private static final Properties config = loadConfig();
protected static final String BASE_ENDPOINT = ...;
protected static final String TOKEN = ...;

// Can be called without creating an object
protected static String getConfigProperty(String key) { ... }
protected static void assertEqualsIgnoreCase(String actual, String expected) { ... }
```

**Rule of thumb:** if a method does not use instance fields like `client` or `response`, make it `static`.

---

### Generics
Write one method that works safely for any type, checked at compile time.

```java
// <T> is a type parameter — resolved at the call site
public static <T> T unmarshallGeneric(CloseableHttpResponse response, Class<T> clazz) throws IOException {
    String jsonBody = EntityUtils.toString(response.getEntity());
    return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(jsonBody, clazz);
}
```

Without generics you would need a separate method for every DTO class.

---

### Exception Handling
Java forces you to declare or handle checked exceptions. `IOException` is thrown by any I/O operation (HTTP calls, file reads).

```java
// Propagate — let the test framework handle it
@Test
public void rootEndpointReturns200() throws IOException { ... }

// Handle — try-with-resources closes the stream automatically
private static Properties loadConfig() {
    Properties props = new Properties();
    try (InputStream in = BaseClass.class.getClassLoader().getResourceAsStream("config.properties")) {
        if (in != null) props.load(in);
    } catch (IOException e) {
        throw new RuntimeException("Failed to load config.properties", e);
    }
    return props;
}
```

`try-with-resources` guarantees the stream is closed even if an exception is thrown — no `finally` block needed.

---

### Java 8 Streams
A functional style for processing collections — filter, find, transform — in a single readable chain.

```java
// Classic loop
for (Header header : httpHeaders) {
    if (headerName.equalsIgnoreCase(header.getName())) {
        returnHeader = header.getValue();
    }
}

// Java 8 Stream — same result, more concise
Header matchedHeader = httpHeaders.stream()
        .filter(header -> headerName.equalsIgnoreCase(header.getName()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Didn't find the header: " + headerName));
```

Both versions live side by side in `ResponseUtils` (`getHeader` vs `getHeaderJava8Way`) so you can compare them directly.

---

### Annotations
Metadata attached to code, read at runtime by a framework.

**TestNG annotations** control the test lifecycle:

```java
@BeforeTest    // runs once before all tests in a <test> block
@AfterTest     // runs once after all tests in a <test> block
@BeforeMethod  // runs before each individual test method
@AfterMethod   // runs after each individual test method
@Test          // marks this method as a test case
@Test(priority = 0)               // controls execution order
@Test(dataProvider = "endpoints") // wires up parameterized data
@DataProvider                     // supplies parameter sets to a @Test
```

**Jackson annotations** control JSON deserialization:

```java
@JsonProperty("resources")
private void unmarshallNested(Map<String, Object> resources) {
    // maps the JSON key "resources" to this method
}
```

---

### Packages
Java organises classes into namespaces. This project uses four:

| Package | Purpose |
|---------|---------|
| `entities` | Framework infrastructure (`BaseClass`, `ApiClient`) |
| `dto` | Plain data classes that mirror JSON responses |
| `utils` | Stateless helper methods for working with responses |
| `service` | The actual test classes |

Keeping them separate enforces single responsibility — a DTO has no test logic; a utility class has no HTTP lifecycle code.

---

### POJOs and DTOs
A **POJO (Plain Old Java Object)** is a class with no framework dependencies — just private fields and public getters.  
A **DTO (Data Transfer Object)** is a POJO used specifically to carry data between layers (here: from a JSON response to a test assertion).

```java
// dto/User.java — mirrors the GitHub /users/{username} response
public class User {
    private String login;
    private int id;

    public String getLogin() { return login; }
    public int getId()       { return id; }
}
```

Jackson deserializes the raw JSON into this object automatically, matching JSON keys to field names. `RateLimit` shows the nested case — a `@JsonProperty` method extracts values from a nested JSON object into flat fields.

---

## Summary Table

| Concept | Where in the project |
|---------|---------------------|
| Encapsulation | `BaseClass` hides config + lifecycle; DTO fields are `private` with getters |
| Inheritance | All test classes extend `BaseClass` |
| Abstraction | `ApiClient` interface; DTO classes hide raw JSON |
| Polymorphism | `BaseClass implements ApiClient`; `unmarshallGeneric<T>` |
| Access modifiers | `private` DTO fields + config, `protected` shared fields, `public` interface methods |
| Static members | `BASE_ENDPOINT`, `TOKEN`, `getConfigProperty()`, `assertEqualsIgnoreCase()` |
| Generics | `unmarshallGeneric(response, Class<T>)` |
| Exception handling | `throws IOException`; `try-with-resources` in `loadConfig()` |
| Java 8 Streams | `getHeaderJava8Way()`, `headerIsPresent()` in `ResponseUtils` |
| Annotations | TestNG lifecycle + `@DataProvider`; Jackson `@JsonProperty` |
| Packages | `entities`, `dto`, `utils`, `service` |
| POJOs / DTOs | `User`, `NotFound`, `RateLimit` |
