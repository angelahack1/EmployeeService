# EmployeeService — SOAP 1.2 Web Service

A Jakarta EE 10 SOAP 1.2 web service that manages employees through full CRUD operations. Built with the SEI + SIB (Service Endpoint Interface + Service Implementation Bean) pattern and deployed on Eclipse GlassFish 7.

---

## Tech Stack

| Layer          | Technology                                    |
|----------------|-----------------------------------------------|
| Language       | Java 17 (LTS)                                 |
| Platform       | Jakarta EE 10                                 |
| Protocol       | SOAP 1.2 — Document/Literal/Wrapped (WS-I)   |
| Server         | Eclipse GlassFish 7.x                         |
| Build          | Apache Maven 3.9+                             |
| Testing        | JUnit 5 + Playwright 1.49                     |
| Storage        | In-memory (`LinkedHashMap`) — no database     |

---

## Project Structure

```
EmployeeService/
├── pom.xml                                    Maven build config + profiles
├── SETUP-GUIDE.md                             Detailed IDE setup walkthrough
├── src/
│   ├── main/
│   │   ├── java/com/enterprise/soap/
│   │   │   ├── model/
│   │   │   │   └── Employee.java              JAXB-annotated DTO
│   │   │   ├── exception/
│   │   │   │   └── EmployeeServiceException   @WebFault for SOAP faults
│   │   │   └── service/
│   │   │       ├── EmployeeService.java       SEI (contract/interface)
│   │   │       └── EmployeeServiceImpl.java   SIB (implementation + @Stateless EJB)
│   │   └── webapp/
│   │       ├── index.html                     Landing page
│   │       └── WEB-INF/
│   │           ├── web.xml                    Deployment descriptor
│   │           └── glassfish-web.xml          Context root → /EmployeeService
│   └── test/
│       └── java/com/enterprise/soap/
│           ├── client/
│           │   └── SoapClientTest.java        Standalone JAX-WS client demo
│           ├── helpers/
│           │   ├── PlaywrightBaseTest.java     Base class (browser + API lifecycle)
│           │   ├── SoapEnvelopeBuilder.java    SOAP 1.2 request builder
│           │   └── SoapResponseParser.java     XML response parser
│           └── tests/
│               ├── SoapApiTest.java            18 API tests (all CRUD + protocol)
│               └── LandingPageTest.java        11 UI tests (landing page + WSDL)
└── test-requests/
    ├── getAllEmployees.xml                     cURL payloads
    ├── getEmployee.xml
    └── createEmployee.xml
```

---

## SOAP Operations

All operations are exposed at a single endpoint via HTTP POST with `Content-Type: application/soap+xml;charset=UTF-8`.

| Operation                        | Description                        | Fault on Error |
|----------------------------------|------------------------------------|----------------|
| `getAllEmployees()`              | Returns all employees              | No             |
| `getEmployee(employeeId)`       | Returns one employee by ID         | Yes — not found|
| `createEmployee(employee)`      | Creates employee, assigns auto-ID  | Yes — validation|
| `updateEmployee(employee)`      | Updates existing employee          | Yes — not found|
| `deleteEmployee(employeeId)`    | Deletes employee, returns boolean  | Yes — not found|

**Target namespace:** `http://service.soap.enterprise.com/`

The service ships with 3 seed employees (Carlos Garcia, Maria Lopez, Roberto Martinez). Data is volatile — restarting GlassFish resets to seeds.

---

## Prerequisites

- **JDK 17** — [Adoptium](https://adoptium.net/)
- **Eclipse GlassFish 7.x** — [glassfish.org/download](https://glassfish.org/download)
- **Apache Maven 3.9+** — bundled with most IDEs or [maven.apache.org](https://maven.apache.org/)

---

## Build

```bash
# Standard build (skips tests by default if GlassFish isn't running)
mvn clean package

# Build without tests explicitly
mvn clean package -Pno-tests
```

This produces `target/EmployeeService.war`.

---

## Deploy

### Option 1 — GlassFish Autodeploy

Copy the WAR to the autodeploy directory:

```bash
cp target/EmployeeService.war <GLASSFISH_HOME>/glassfish/domains/domain1/autodeploy/
```

Or use the Maven profile (configure the path in `pom.xml` first):

```bash
mvn clean package -Pautodeploy
```

### Option 2 — asadmin CLI

```bash
<GLASSFISH_HOME>/bin/asadmin deploy target/EmployeeService.war
```

### Option 3 — Eclipse IDE

1. **Servers** tab → right-click GlassFish 7 → **Add and Remove...**
2. Move **EmployeeService** to Configured → **Finish**
3. Right-click server → **Publish**

---

## Verify Deployment

After deploying, confirm the service is live:

| Resource         | URL                                                          |
|------------------|--------------------------------------------------------------|
| Landing Page     | http://localhost:8080/EmployeeService/                       |
| WSDL             | http://localhost:8080/EmployeeService/EmployeeService?wsdl   |
| SOAP Endpoint    | http://localhost:8080/EmployeeService/EmployeeService        |
| GlassFish Admin  | http://localhost:4848                                        |

The WSDL should contain `<soap12:binding>` elements and list all five operations.

---

## Test the Service

### cURL Examples

**Get all employees:**

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d @test-requests/getAllEmployees.xml
```

**Get employee by ID:**

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d @test-requests/getEmployee.xml
```

**Create an employee:**

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d @test-requests/createEmployee.xml
```

Pre-built XML payloads are in the `test-requests/` directory.

### Standalone Java Client

With GlassFish running, execute the test client:

```bash
mvn exec:java -Dexec.mainClass="com.enterprise.soap.client.SoapClientTest"
```

Or run `SoapClientTest.java` directly from your IDE as a Java Application. It demonstrates all five CRUD operations using JAX-WS dynamic proxy.

### SoapUI

1. Create a new SOAP project with the WSDL URL
2. SoapUI auto-generates requests for all operations
3. Ensure requests use SOAP 1.2 (`application/soap+xml`, not `text/xml`)

---

## Automated Test Suite

The project includes **29 automated tests** using JUnit 5 and Playwright, split into API and UI tests.

> **Requirement:** GlassFish must be running with the service deployed before executing tests.

### Run All Tests

```bash
mvn test
```

### Run by Category

```bash
# API tests only (SOAP operations, protocol compliance)
mvn test -Ptesting-api

# UI tests only (landing page, WSDL endpoint)
mvn test -Ptesting-ui

# Run with visible browser (headed mode)
mvn test -Ptesting-headed
```

### Test Coverage

| Suite              | Tests | What It Covers                                         |
|--------------------|-------|--------------------------------------------------------|
| `SoapApiTest`      | 18    | CRUD operations, SOAP fault handling, protocol compliance (content-type, malformed XML, empty body) |
| `LandingPageTest`  | 11    | Landing page content/links, WSDL availability/structure |

### Test Architecture

- **`PlaywrightBaseTest`** — manages Playwright browser and API request context lifecycle
- **`SoapEnvelopeBuilder`** — fluent builder for SOAP 1.2 request envelopes
- **`SoapResponseParser`** — regex-based XML parser for extracting fields and detecting faults

---

## Maven Profiles

| Profile          | Purpose                                      |
|------------------|----------------------------------------------|
| `development`    | Local GlassFish autodeploy                   |
| `testing`        | Testing environment                          |
| `production`     | Production environment                       |
| `autodeploy`     | Copy WAR to GlassFish autodeploy directory   |
| `autoclean`      | Clean the target directory                   |
| `no-tests`       | Build without running tests                  |
| `testing-api`    | Run SOAP API tests only                      |
| `testing-ui`     | Run landing page tests only                  |
| `testing-headed` | Run tests with visible browser window        |

---

## Architecture Notes

### SEI + SIB Pattern

- **SEI** (`EmployeeService.java`) — The interface annotated with `@WebService` and `@SOAPBinding` that defines the service contract. Clients program against this.
- **SIB** (`EmployeeServiceImpl.java`) — The `@Stateless` EJB that implements the SEI. Linked via `endpointInterface`. The `@BindingType(SOAP12HTTP_BINDING)` annotation on this class is what enables SOAP 1.2 (JAX-WS defaults to 1.1 without it).

### SOAP 1.2 vs 1.1

| Aspect       | SOAP 1.1                 | SOAP 1.2 (this project)          |
|--------------|--------------------------|----------------------------------|
| Content-Type | `text/xml`               | `application/soap+xml`           |
| Namespace    | `.../soap-envelope`      | `.../2003/05/soap-envelope`      |
| Action       | `SOAPAction` header      | `action` param in Content-Type   |
| Faults       | Single `faultcode`       | Structured `Code/Subcode/Reason` |

### Data Storage

The service uses an in-memory `LinkedHashMap` with an `AtomicLong` ID sequence. There is no database — data resets on every server restart. This makes it ideal for development, demos, and learning JAX-WS without database setup overhead.

---

## Troubleshooting

| Problem                              | Solution                                                                                  |
|--------------------------------------|-------------------------------------------------------------------------------------------|
| 404 on WSDL URL                      | Check `serviceName` in `@WebService` matches URL. Verify context root in `glassfish-web.xml`. |
| Getting SOAP 1.1 responses           | Ensure `@BindingType(SOAP12HTTP_BINDING)` is on the **implementation class**, not the interface. |
| `ClassNotFoundException: jakarta.xml.ws` | Set GlassFish 7 as the target runtime in project properties.                            |
| Port 8080 conflict                   | Change port in GlassFish Admin Console → Network Listeners.                              |
| Tests fail with connection refused   | Start GlassFish and deploy the service before running tests.                             |
| Build error: source 17 not supported | Set Java compiler to 17 in project properties or `JAVA_HOME`.                            |

---

## Further Reading

- [SETUP-GUIDE.md](SETUP-GUIDE.md) — Step-by-step Eclipse IDE setup with screenshots-level detail
- [Jakarta XML Web Services Spec](https://jakarta.ee/specifications/xml-web-services/)
- [GlassFish Documentation](https://glassfish.org/documentation)
