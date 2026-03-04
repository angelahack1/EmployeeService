package com.enterprise.soap.tests;

import com.enterprise.soap.helpers.PlaywrightBaseTest;
import com.enterprise.soap.helpers.SoapEnvelopeBuilder;
import com.enterprise.soap.helpers.SoapResponseParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API-level tests for the SOAP 1.2 Employee Web Service.
 *
 * <p>Uses Playwright's {@code APIRequestContext} to POST raw SOAP
 * envelopes and validates the XML responses.</p>
 *
 * <p><strong>Note:</strong> The service uses a static in-memory store
 * seeded with 3 employees (IDs 1–3). Tests that create/update/delete
 * mutate this store. Redeploy the WAR for a clean slate:
 * {@code asadmin deploy --force .../soap-service.war}</p>
 */
@DisplayName("SOAP API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SoapApiTest extends PlaywrightBaseTest {

    /* ================================================================== */
    /*  getAllEmployees                                                    */
    /* ================================================================== */

    @Nested
    @DisplayName("getAllEmployees")
    class GetAllEmployees {

        @Test
        @DisplayName("Returns HTTP 200")
        void returns200() {
            APIResponse resp = soapPost(SoapEnvelopeBuilder.getAllEmployees());
            assertEquals(200, resp.status());
        }

        @Test
        @DisplayName("Response is a valid SOAP 1.2 envelope")
        void validSoap12Envelope() {
            String xml = soapPost(SoapEnvelopeBuilder.getAllEmployees()).text();
            assertTrue(xml.contains("http://www.w3.org/2003/05/soap-envelope"),
                    "Response should reference SOAP 1.2 namespace");
            assertTrue(xml.contains("Body"),
                    "Response should contain a <Body> element");
        }

        @Test
        @DisplayName("Contains at least the 3 seed employees")
        void containsSeedEmployees() {
            String xml = soapPost(SoapEnvelopeBuilder.getAllEmployees()).text();
            List<String> firstNames = SoapResponseParser.extractAll(xml, "firstName");

            assertTrue(firstNames.size() >= 3,
                    "Expected at least 3 employees, got " + firstNames.size());
            assertTrue(firstNames.contains("Carlos"),
                    "Seed employee 'Carlos' should be present");
        }

        @Test
        @DisplayName("Each employee has all required fields")
        void allFieldsPresent() {
            String xml = soapPost(SoapEnvelopeBuilder.getAllEmployees()).text();
            for (String field : new String[]{"id", "firstName", "lastName", "email", "department"}) {
                List<String> values = SoapResponseParser.extractAll(xml, field);
                assertFalse(values.isEmpty(),
                        "Field '" + field + "' should be present in the response");
            }
        }
    }

    /* ================================================================== */
    /*  getEmployee                                                       */
    /* ================================================================== */

    @Nested
    @DisplayName("getEmployee")
    class GetEmployee {

        @Test
        @DisplayName("Returns employee with id=1 (Carlos)")
        void returnsEmployee1() {
            APIResponse resp = soapPost(SoapEnvelopeBuilder.getEmployee(1));
            assertEquals(200, resp.status());

            String xml = resp.text();
            assertEquals("Carlos", SoapResponseParser.extractFirst(xml, "firstName"));
        }

        @Test
        @DisplayName("Employee 1 is in the Cybersecurity department")
        void correctDepartment() {
            String xml = soapPost(SoapEnvelopeBuilder.getEmployee(1)).text();
            assertEquals("Cybersecurity", SoapResponseParser.extractFirst(xml, "department"));
        }

        @Test
        @DisplayName("Employee 2 has the correct email")
        void employee2Email() {
            String xml = soapPost(SoapEnvelopeBuilder.getEmployee(2)).text();
            assertEquals("maria.lopez@corp.mx", SoapResponseParser.extractFirst(xml, "email"));
        }

        @Test
        @DisplayName("Non-existent employee returns a SOAP Fault")
        void faultOnMissing() {
            String xml = soapPost(SoapEnvelopeBuilder.getEmployee(9999)).text();
            assertTrue(SoapResponseParser.isSoapFault(xml),
                    "Expected a SOAP Fault for non-existent employee");
            assertNotNull(SoapResponseParser.extractFaultReason(xml),
                    "Fault should include a reason/text");
        }
    }

    /* ================================================================== */
    /*  createEmployee                                                    */
    /* ================================================================== */

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("Creates a new employee and returns it with an auto-generated ID")
        void createsWithId() {
            String xml = soapPost(
                    SoapEnvelopeBuilder.createEmployee(
                            "Ana", "Hernández", "ana@corp.mx", "Risk Analysis")
            ).text();

            assertFalse(SoapResponseParser.isSoapFault(xml),
                    "Should not be a SOAP Fault");
            assertEquals("Ana", SoapResponseParser.extractFirst(xml, "firstName"));

            String id = SoapResponseParser.extractFirst(xml, "id");
            assertNotNull(id, "New employee should have an ID");
            assertTrue(Long.parseLong(id) > 0, "ID should be positive");
        }

        @Test
        @DisplayName("Newly created employee appears in getAllEmployees")
        void appearsInList() {
            // Create
            String createXml = soapPost(
                    SoapEnvelopeBuilder.createEmployee(
                            "TestUser", "ForList", "testlist@corp.mx", "QA")
            ).text();
            String newId = SoapResponseParser.extractFirst(createXml, "id");
            assertNotNull(newId);

            // Verify in list
            String listXml = soapPost(SoapEnvelopeBuilder.getAllEmployees()).text();
            List<String> allIds = SoapResponseParser.extractAll(listXml, "id");
            assertTrue(allIds.contains(newId),
                    "Newly created ID " + newId + " should appear in getAllEmployees");
        }
    }

    /* ================================================================== */
    /*  updateEmployee                                                    */
    /* ================================================================== */

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("Updates an existing employee's fields")
        void updatesFields() {
            // Create a fresh employee
            String createXml = soapPost(
                    SoapEnvelopeBuilder.createEmployee(
                            "ToUpdate", "Before", "before@corp.mx", "OldDept")
            ).text();
            long id = Long.parseLong(SoapResponseParser.extractFirst(createXml, "id"));

            // Update it
            String updateXml = soapPost(
                    SoapEnvelopeBuilder.updateEmployee(
                            id, "ToUpdate", "After", "after@corp.mx", "NewDept")
            ).text();

            assertFalse(SoapResponseParser.isSoapFault(updateXml));
            assertEquals("After",   SoapResponseParser.extractFirst(updateXml, "lastName"));
            assertEquals("NewDept", SoapResponseParser.extractFirst(updateXml, "department"));
        }

        @Test
        @DisplayName("Updating a non-existent employee returns a SOAP Fault")
        void faultOnMissing() {
            String xml = soapPost(
                    SoapEnvelopeBuilder.updateEmployee(
                            99999, "Ghost", "User", "ghost@void", "Nowhere")
            ).text();
            assertTrue(SoapResponseParser.isSoapFault(xml));
        }
    }

    /* ================================================================== */
    /*  deleteEmployee                                                    */
    /* ================================================================== */

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("Deletes an employee and returns success=true")
        void deletesSuccessfully() {
            // Create throwaway
            String createXml = soapPost(
                    SoapEnvelopeBuilder.createEmployee(
                            "ToDelete", "Me", "delete@corp.mx", "Temp")
            ).text();
            long id = Long.parseLong(SoapResponseParser.extractFirst(createXml, "id"));

            // Delete
            String deleteXml = soapPost(SoapEnvelopeBuilder.deleteEmployee(id)).text();
            assertFalse(SoapResponseParser.isSoapFault(deleteXml));
            assertEquals("true", SoapResponseParser.extractFirst(deleteXml, "success"));
        }

        @Test
        @DisplayName("Deleted employee is no longer retrievable (SOAP Fault)")
        void goneAfterDelete() {
            // Create and delete
            String createXml = soapPost(
                    SoapEnvelopeBuilder.createEmployee(
                            "Ephemeral", "Employee", "ephemeral@corp.mx", "Temp")
            ).text();
            long id = Long.parseLong(SoapResponseParser.extractFirst(createXml, "id"));
            soapPost(SoapEnvelopeBuilder.deleteEmployee(id));

            // Retrieve → should fault
            String getXml = soapPost(SoapEnvelopeBuilder.getEmployee(id)).text();
            assertTrue(SoapResponseParser.isSoapFault(getXml),
                    "Deleted employee should produce a SOAP Fault on retrieval");
        }

        @Test
        @DisplayName("Deleting a non-existent employee returns a SOAP Fault")
        void faultOnMissing() {
            String xml = soapPost(SoapEnvelopeBuilder.deleteEmployee(99999)).text();
            assertTrue(SoapResponseParser.isSoapFault(xml));
        }
    }

    /* ================================================================== */
    /*  Protocol compliance & edge cases                                  */
    /* ================================================================== */

    @Nested
    @DisplayName("Protocol & edge cases")
    class ProtocolCompliance {

        @Test
        @DisplayName("Response Content-Type is application/soap+xml (SOAP 1.2)")
        void soap12ContentType() {
            APIResponse resp = soapPost(SoapEnvelopeBuilder.getAllEmployees());
            String ct = resp.headers().getOrDefault("content-type", "");
            assertTrue(ct.toLowerCase().contains("application/soap+xml"),
                    "SOAP 1.2 requires application/soap+xml, got: " + ct);
        }

        @Test
        @DisplayName("Malformed XML body produces a fault or server error")
        void malformedXmlFault() {
            APIResponse resp = apiContext.post("/EmployeeService/EmployeeService",
                    RequestOptions.create()
                            .setHeader("Content-Type", SOAP_CONTENT_TYPE)
                            .setData("<this-is-not-a-soap-envelope/>")
            );
            String xml = resp.text();
            boolean isError = resp.status() == 500 || SoapResponseParser.isSoapFault(xml);
            assertTrue(isError,
                    "Malformed XML should produce HTTP 500 or a SOAP Fault");
        }

        @Test
        @DisplayName("Empty body does not return a valid employee")
        void emptyBodyError() {
            APIResponse resp = apiContext.post("/EmployeeService/EmployeeService",
                    RequestOptions.create()
                            .setHeader("Content-Type", SOAP_CONTENT_TYPE)
                            .setData("")
            );
            String xml = resp.text();
            boolean isError = resp.status() >= 400
                    || SoapResponseParser.isSoapFault(xml)
                    || xml.trim().isEmpty();
            assertTrue(isError,
                    "Empty body should not return a successful employee response");
        }
    }
}