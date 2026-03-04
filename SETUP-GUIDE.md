# SOAP 1.2 Web Service — Complete Setup Guide

## Jakarta EE 10 · GlassFish 7 · JDK 17 · Eclipse IDE

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                       PROJECT STRUCTURE                          │
│                                                                  │
│  Pattern: SEI + SIB  (Service Endpoint Interface + Impl Bean)   │
│  Binding: SOAP 1.2 / Document-Literal-Wrapped                   │
│  Server:  Eclipse GlassFish 7.x (Jakarta EE 10)                 │
│  JDK:    17 (LTS)                                                │
│                                                                  │
│  EmployeeService/                                                │
│  ├── pom.xml                          ← Maven build config       │
│  ├── src/main/java/com/enterprise/soap/                          │
│  │   ├── model/                                                  │
│  │   │   └── Employee.java            ← JAXB POJO               │
│  │   ├── exception/                                              │
│  │   │   └── EmployeeServiceException.java  ← SOAP Fault        │
│  │   └── service/                                                │
│  │       ├── EmployeeService.java     ← SEI (interface/contract) │
│  │       └── EmployeeServiceImpl.java ← SIB (implementation)    │
│  ├── src/main/webapp/                                            │
│  │   ├── index.html                   ← Landing page             │
│  │   └── WEB-INF/                                                │
│  │       ├── web.xml                  ← Deployment descriptor    │
│  │       └── glassfish-web.xml        ← Context root config      │
│  ├── src/test/java/com/enterprise/soap/                          │
│  │   ├── client/                                                 │
│  │   │   └── SoapClientTest.java      ← Standalone test client   │
│  │   ├── helpers/                                                │
│  │   │   ├── PlaywrightBaseTest.java  ← Base test class          │
│  │   │   ├── SoapEnvelopeBuilder.java ← SOAP request builder    │
│  │   │   └── SoapResponseParser.java  ← XML response parser     │
│  │   └── tests/                                                  │
│  │       ├── SoapApiTest.java         ← 18 SOAP API tests        │
│  │       └── LandingPageTest.java     ← 11 UI/browser tests      │
│  └── test-requests/                                              │
│      ├── getAllEmployees.xml           ← cURL test payloads       │
│      ├── getEmployee.xml                                         │
│      └── createEmployee.xml                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

| Requirement               | Version       | Download URL                                          |
|--------------------------|---------------|------------------------------------------------------|
| **JDK**                  | 17 (LTS)      | https://adoptium.net/                                |
| **Eclipse IDE**          | Enterprise EE  | https://www.eclipse.org/downloads/                   |
| **Eclipse GlassFish**    | 7.x           | https://glassfish.org/download                       |
| **Apache Maven**         | 3.9+          | Bundled with Eclipse (or https://maven.apache.org/)  |

---

## STEP 1 — Install & Configure GlassFish 7 in Eclipse

### 1.1  Download GlassFish 7

1. Go to **https://glassfish.org/download**
2. Download `glassfish-7.x.x.zip`
3. Extract to a **path with NO spaces**, e.g.:
   - Windows: `C:\servers\glassfish7`
   - macOS/Linux: `/opt/glassfish7`

### 1.2  Add GlassFish Server to Eclipse

1. Open Eclipse → **Window → Show View → Servers**
2. In the **Servers** tab → right-click → **New → Server**
3. If you don't see GlassFish in the list:
   - Click **"Download additional server adapters"**
   - Install **"GlassFish Tools"** (or search Eclipse Marketplace for "GlassFish")
   - Restart Eclipse
4. Select **GlassFish → GlassFish 7** → Next
5. Browse to your GlassFish installation directory
6. Set **JDK 17** as the JRE
7. Click **Finish**

### 1.3  Verify GlassFish Starts

1. In the **Servers** tab → right-click your GlassFish server → **Start**
2. Open browser → navigate to **http://localhost:8080**
3. You should see the GlassFish welcome page
4. Admin console: **http://localhost:4848**

---

## STEP 2 — Create the Maven Project in Eclipse

### 2.1  Import as Maven Project

**Option A — From these files (recommended):**

1. Copy the entire `EmployeeService/` folder to your Eclipse workspace
2. In Eclipse: **File → Import → Maven → Existing Maven Projects**
3. Browse to the `EmployeeService/` folder → Select `pom.xml` → **Finish**
4. Eclipse will resolve dependencies automatically

**Option B — Create from scratch:**

1. **File → New → Maven Project**
2. Check **"Create a simple project (skip archetype selection)"**
3. Fill in:
   - **Group Id:** `com.enterprise`
   - **Artifact Id:** `EmployeeService`
   - **Version:** `0.1.0`
   - **Packaging:** `war`
4. Click **Finish**
5. Replace the generated `pom.xml` with the provided one
6. Copy all Java source files into `src/main/java/`
7. Copy `webapp/` contents into `src/main/webapp/`

### 2.2  Verify Project Structure in Eclipse

Your Package Explorer should look like:

```
EmployeeService/
├── src/main/java/
│   └── com.enterprise.soap.model
│       └── Employee.java
│   └── com.enterprise.soap.exception
│       └── EmployeeServiceException.java
│   └── com.enterprise.soap.service
│       ├── EmployeeService.java
│       └── EmployeeServiceImpl.java
├── src/main/webapp/
│   ├── index.html
│   └── WEB-INF/
│       ├── web.xml
│       └── glassfish-web.xml
├── src/test/java/
│   └── com.enterprise.soap.client
│       └── SoapClientTest.java
│   └── com.enterprise.soap.helpers
│       ├── PlaywrightBaseTest.java
│       ├── SoapEnvelopeBuilder.java
│       └── SoapResponseParser.java
│   └── com.enterprise.soap.tests
│       ├── SoapApiTest.java
│       └── LandingPageTest.java
├── test-requests/
│   ├── getAllEmployees.xml
│   ├── getEmployee.xml
│   └── createEmployee.xml
└── pom.xml
```

### 2.3  Set Project Facets (if needed)

1. Right-click project → **Properties → Project Facets**
2. Ensure these are checked:
   - **Dynamic Web Module:** 6.0
   - **Java:** 17
   - **Jakarta EE:** 10.0 (if available)
3. Click **Apply and Close**

### 2.4  Set Target Runtime

1. Right-click project → **Properties → Targeted Runtimes**
2. Check **GlassFish 7**
3. Click **Apply and Close**

---

## STEP 3 — Build the Project

### 3.1  Maven Build

1. Right-click project → **Run As → Maven build...**
2. In the **Goals** field, type: `clean package`
3. Click **Run**
4. The console should show:
   ```
   [INFO] BUILD SUCCESS
   [INFO] Building war: .../target/EmployeeService.war
   ```

### 3.2  Build without Tests

If GlassFish is not running, the automated tests will fail. Use the `no-tests` profile:

```bash
mvn clean package -Pno-tests
```

### 3.3  Troubleshoot Build Errors

| Error | Fix |
|-------|-----|
| `Source option 17 is not supported` | Right-click project → Properties → Java Compiler → set to 17 |
| `Cannot resolve jakarta.xml.ws` | Right-click project → Maven → Update Project (Alt+F5) |
| `web.xml is missing` | Already handled by `failOnMissingWebXml` in pom.xml |

---

## STEP 4 — Deploy to GlassFish 7

### 4.1  Deploy from Eclipse

1. In the **Servers** tab → right-click **GlassFish 7** → **Add and Remove...**
2. Move **EmployeeService** from Available → Configured → **Finish**
3. Right-click server → **Publish** (or it publishes automatically)
4. Server console should show:
   ```
   WS00019: ... EmployeeService ... deployed ... endpoint address:
   http://localhost:8080/EmployeeService/EmployeeService
   ```

### 4.2  Deploy with Maven Autodeploy Profile

Configure the GlassFish autodeploy path in `pom.xml` properties, then:

```bash
mvn clean package -Pdevelopment,autodeploy
```

This builds the WAR and copies it to the GlassFish autodeploy directory automatically.

### 4.3  Deploy WAR Manually

1. Copy `target/EmployeeService.war` to `<glassfish>/glassfish/domains/domain1/autodeploy/`
2. GlassFish will auto-deploy within seconds
3. Check deployment status at **http://localhost:4848** → Applications

---

## STEP 5 — Verify the Service is Running

### 5.1  Check the WSDL

Open your browser and navigate to:

```
http://localhost:8080/EmployeeService/EmployeeService?wsdl
```

You should see a complete WSDL XML document. **Verify these critical details:**

- The `<soap12:binding>` element confirms SOAP 1.2
- The `targetNamespace` is `http://service.soap.enterprise.com/`
- All five operations are listed: `getEmployee`, `getAllEmployees`, `createEmployee`, `updateEmployee`, `deleteEmployee`

### 5.2  Check the Landing Page

Navigate to:

```
http://localhost:8080/EmployeeService/
```

You should see the HTML landing page with links and instructions.

---

## STEP 6 — Test the Service

### 6.1  Test with cURL (Terminal / Command Prompt)

**getAllEmployees:**

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d @test-requests/getAllEmployees.xml
```

**getEmployee(1):**

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d @test-requests/getEmployee.xml
```

**createEmployee:**

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d @test-requests/createEmployee.xml
```

Or construct inline requests:

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
                     xmlns:ser="http://service.soap.enterprise.com/">
    <soap:Body>
      <ser:getAllEmployees/>
    </soap:Body>
  </soap:Envelope>'
```

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
                     xmlns:ser="http://service.soap.enterprise.com/">
    <soap:Body>
      <ser:getEmployee>
        <employeeId>1</employeeId>
      </ser:getEmployee>
    </soap:Body>
  </soap:Envelope>'
```

```bash
curl -X POST http://localhost:8080/EmployeeService/EmployeeService \
  -H "Content-Type: application/soap+xml;charset=UTF-8" \
  -d '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
                     xmlns:ser="http://service.soap.enterprise.com/">
    <soap:Body>
      <ser:createEmployee>
        <employee>
          <firstName>Ana</firstName>
          <lastName>Hernández</lastName>
          <email>ana.hernandez@banxico.mx</email>
          <department>Risk Analysis</department>
        </employee>
      </ser:createEmployee>
    </soap:Body>
  </soap:Envelope>'
```

> **Important — XML namespace convention:** The wrapper elements (`getAllEmployees`, `getEmployee`, `createEmployee`, etc.) use the service namespace prefix (`ser:`), but their **child elements** (`employeeId`, `employee`, `firstName`, etc.) must be **unqualified** (no prefix). This is because the WSDL schema uses `elementFormDefault="unqualified"` (the default). Adding `ser:` to child elements will cause a SOAP fault.

### 6.2  Test with SoapUI

1. Open SoapUI → **File → New SOAP Project**
2. Set **Initial WSDL:** `http://localhost:8080/EmployeeService/EmployeeService?wsdl`
3. Click **OK** — SoapUI auto-generates requests for all operations
4. **IMPORTANT:** In each request, ensure the binding is SOAP 1.2:
   - Check that the `Content-Type` header is `application/soap+xml`
   - The envelope namespace should be `http://www.w3.org/2003/05/soap-envelope`
5. Double-click any operation → click the green play button to execute

### 6.3  Test with the Java Client

1. Make sure GlassFish is running with the service deployed
2. In Eclipse → navigate to `SoapClientTest.java`
3. Right-click → **Run As → Java Application**
4. The console should show all CRUD operations executing successfully

### 6.4  Automated Test Suite (JUnit 5 + Playwright)

The project includes **29 automated tests** covering SOAP API operations and browser-based UI validation.

> **Requirement:** GlassFish must be running with the service deployed before executing tests.

**Run all tests:**

```bash
mvn test
```

**Run by category using Maven profiles:**

```bash
# SOAP API tests only (CRUD operations, faults, protocol compliance)
mvn test -Ptesting-api

# UI tests only (landing page content, WSDL endpoint)
mvn test -Ptesting-ui

# Run with a visible browser window (non-headless)
mvn test -Ptesting-headed
```

**Test suite breakdown:**

| Test Class          | Tests | Coverage                                                        |
|---------------------|-------|-----------------------------------------------------------------|
| `SoapApiTest`       | 18    | All 5 CRUD operations, SOAP fault handling, protocol compliance |
| `LandingPageTest`   | 11    | Landing page content/links, WSDL availability and structure     |

**Test helper classes:**

| Class                   | Purpose                                                |
|-------------------------|--------------------------------------------------------|
| `PlaywrightBaseTest`    | Manages Playwright browser and API request lifecycle   |
| `SoapEnvelopeBuilder`   | Fluent builder for SOAP 1.2 request envelopes          |
| `SoapResponseParser`    | Regex-based XML parser for response fields and faults  |

---

## STEP 7 — Maven Profiles Reference

The `pom.xml` defines several profiles for different workflows:

| Profile          | Usage                                     | Command Example                              |
|------------------|-------------------------------------------|----------------------------------------------|
| `development`    | Sets GlassFish autodeploy path (dev)      | `mvn package -Pdevelopment,autodeploy`       |
| `testing`        | Sets GlassFish autodeploy path (test)     | `mvn package -Ptesting,autodeploy`           |
| `production`     | Sets GlassFish autodeploy path (prod)     | `mvn package -Pproduction,autodeploy`        |
| `autodeploy`     | Copies WAR to GlassFish autodeploy dir    | Combine with an environment profile above    |
| `autoclean`      | Deletes the target directory              | `mvn initialize -Pautoclean`                 |
| `no-tests`       | Skips all tests during build              | `mvn package -Pno-tests`                     |
| `testing-api`    | Runs only `SoapApiTest`                   | `mvn test -Ptesting-api`                     |
| `testing-ui`     | Runs only `LandingPageTest`               | `mvn test -Ptesting-ui`                      |
| `testing-headed` | Runs tests with visible browser           | `mvn test -Ptesting-headed`                  |

**Configure autodeploy paths** in the `pom.xml` `<properties>` section:

```xml
<gf_autodeploy_development>D:/glassfish7/glassfish/domains/domain1/autodeploy</gf_autodeploy_development>
<gf_autodeploy_testing>D:/glassfish7/glassfish/domains/domain1/autodeploy</gf_autodeploy_testing>
<gf_autodeploy_production>D:/glassfish7-produccion/glassfish/domains/domain1/autodeploy</gf_autodeploy_production>
```

---

## STEP 8 — Understanding the Key Design Decisions

### Why SEI + SIB Pattern?

This is the **most widely used** JAX-WS design pattern:

- **SEI (Service Endpoint Interface):** `EmployeeService.java` — Defines the contract. Annotated with `@WebService` and `@SOAPBinding`. Clients program against this interface.
- **SIB (Service Implementation Bean):** `EmployeeServiceImpl.java` — The concrete implementation. Links to the SEI via `endpointInterface`.

This separation is the industry standard because it enforces a clean contract, enables independent client/server evolution, and is what all JAX-WS documentation and tutorials teach.

### Why SOAP 1.2 Specifically?

The critical annotation is on `EmployeeServiceImpl.java`:

```java
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
```

Without this annotation, JAX-WS defaults to SOAP 1.1. The differences:

| Aspect          | SOAP 1.1                    | SOAP 1.2                              |
|----------------|-----------------------------|---------------------------------------|
| Namespace      | `...soap-envelope`          | `...2003/05/soap-envelope`            |
| Content-Type   | `text/xml`                  | `application/soap+xml`                |
| Fault model    | Single `faultcode`          | Structured `Code/Subcode/Reason`      |
| Action         | `SOAPAction` HTTP header    | `action` parameter in Content-Type    |

### Why Document/Literal/Wrapped?

This is the **WS-I Basic Profile** compliant style — the industry standard for interoperability across platforms (Java, .NET, Python, etc.).

---

## Common Troubleshooting

| Problem | Solution |
|---------|----------|
| **404 when accessing the WSDL** | Check that `serviceName` in `@WebService` matches the URL path segment. Verify context root in `glassfish-web.xml`. |
| **SOAP 1.1 response instead of 1.2** | Verify `@BindingType(SOAPBinding.SOAP12HTTP_BINDING)` is on the **implementation class**, not just the interface. |
| **`ClassNotFoundException: jakarta.xml.ws`** | The project must target GlassFish 7 runtime. Check Targeted Runtimes in project properties. |
| **Port conflict on 8080** | Change GlassFish port: Admin Console → Configurations → server-config → Network Config → Network Listeners |
| **Deployment fails with "Class not found"** | Run `Maven → Update Project` (Alt+F5), then `clean package` again. |
| **Client gets `MalformedURLException`** | Ensure GlassFish is running and the WSDL URL is accessible in a browser first. |
| **Tests fail with connection refused** | Start GlassFish and deploy the service before running tests. Playwright tests require a live server. |
| **SOAP fault: "must not be null"** | Child elements inside operation wrappers must be **unqualified** (no namespace prefix). Use `<employeeId>`, not `<ser:employeeId>`. Only the operation wrapper itself gets the `ser:` prefix. |
| **Playwright browser not installed** | Run `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"` to install browser binaries. |

---

## Quick Reference — Key URLs After Deployment

| Resource                | URL                                                               |
|------------------------|-------------------------------------------------------------------|
| **Landing Page**        | http://localhost:8080/EmployeeService/                           |
| **WSDL**                | http://localhost:8080/EmployeeService/EmployeeService?wsdl       |
| **SOAP Endpoint**       | http://localhost:8080/EmployeeService/EmployeeService            |
| **GlassFish Admin**     | http://localhost:4848                                            |

---

## File Reference

| File | Purpose |
|------|---------|
| `pom.xml` | Maven build: Jakarta EE 10 provided dependency, WAR packaging, JDK 17, 9 profiles |
| `Employee.java` | JAXB-annotated model with `@XmlRootElement`, `@XmlType` |
| `EmployeeServiceException.java` | `@WebFault` for structured SOAP faults |
| `EmployeeService.java` | SEI with `@WebService`, `@WebMethod`, `@SOAPBinding` |
| `EmployeeServiceImpl.java` | SIB with `@Stateless` EJB + `@BindingType(SOAP12HTTP_BINDING)` |
| `web.xml` | Jakarta EE 10 (version 6.0) deployment descriptor |
| `glassfish-web.xml` | Sets context root to `/EmployeeService` |
| `index.html` | Human-friendly landing page |
| `SoapClientTest.java` | Standalone Java client for manual integration testing |
| `PlaywrightBaseTest.java` | Base test class managing Playwright lifecycle |
| `SoapEnvelopeBuilder.java` | Fluent builder for SOAP 1.2 request XML |
| `SoapResponseParser.java` | Regex-based SOAP response field extractor |
| `SoapApiTest.java` | 18 automated SOAP API tests (CRUD + protocol) |
| `LandingPageTest.java` | 11 automated browser tests (landing page + WSDL) |
| `test-requests/*.xml` | Ready-to-use cURL payloads |
