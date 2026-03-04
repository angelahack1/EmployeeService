package com.enterprise.soap.tests;

import com.enterprise.soap.helpers.PlaywrightBaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Browser-based tests for the SOAP service landing page (index.html)
 * and the auto-generated WSDL endpoint.
 *
 * <p>Prerequisite: GlassFish 7 running with EmployeeService deployed.</p>
 */
@DisplayName("Landing Page & WSDL")
class LandingPageTest extends PlaywrightBaseTest {

    /* ================================================================== */
    /*  index.html                                                        */
    /* ================================================================== */

    @Nested
    @DisplayName("index.html")
    class IndexHtml {

        @Test
        @DisplayName("Landing page loads with HTTP 200")
        void pageLoads() {
            Response response = page.navigate(BASE_URL + "/");
            assertNotNull(response);
            assertEquals(200, response.status());
        }

        @Test
        @DisplayName("Page title heading is visible")
        void titleVisible() {
            page.navigate(BASE_URL + "/");
            Locator heading = page.locator("h1");
            assertThat(heading).isVisible();
            assertThat(heading).containsText("SOAP 1.2 Employee Web Service");
        }

        @Test
        @DisplayName("Page mentions GlassFish 7 and Jakarta EE 10")
        void techStackMentioned() {
            page.navigate(BASE_URL + "/");
            Locator body = page.locator("body");
            assertThat(body).containsText("GlassFish 7 Full Platform");
            assertThat(body).containsText("Jakarta EE 10");
        }

        @Test
        @DisplayName("All five SOAP operations are listed")
        void operationsListed() {
            page.navigate(BASE_URL + "/");
            String[] operations = {
                    "getEmployee(employeeId)",
                    "getAllEmployees()",
                    "createEmployee(employee)",
                    "updateEmployee(employee)",
                    "deleteEmployee(employeeId)"
            };
            Locator body = page.locator("body");
            for (String op : operations) {
                assertThat(body).containsText(op);
            }
        }

        @Test
        @DisplayName("WSDL link is visible and has correct href")
        void wsdlLinkExists() {
            page.navigate(BASE_URL + "/");
            Locator wsdlLink = page.locator("a[href*='wsdl']");
            assertThat(wsdlLink).isVisible();
            assertThat(wsdlLink).hasAttribute("href", java.util.regex.Pattern.compile("EmployeeService\\?wsdl"));
        }

        @Test
        @DisplayName("Clicking WSDL link navigates to XML content")
        void wsdlLinkClickable() {
            page.navigate(BASE_URL + "/");
            Locator wsdlLink = page.locator("a[href*='wsdl']");

            Response response = page.waitForNavigation(() -> wsdlLink.click());

            assertNotNull(response);
            assertEquals(200, response.status());
            String contentType = response.headers().getOrDefault("content-type", "");
            assertTrue(contentType.toLowerCase().contains("xml"),
                    "WSDL Content-Type should contain 'xml', got: " + contentType);
        }
    }

    /* ================================================================== */
    /*  WSDL endpoint                                                     */
    /* ================================================================== */

    @Nested
    @DisplayName("WSDL Endpoint")
    class WsdlEndpoint {

        @Test
        @DisplayName("WSDL URL returns HTTP 200")
        void wsdlReturns200() {
            Response response = page.navigate(BASE_URL + "/EmployeeService?wsdl");
            assertNotNull(response);
            assertEquals(200, response.status());
        }

        @Test
        @DisplayName("WSDL contains SOAP 1.2 binding (soap12:binding)")
        void wsdlHasSoap12Binding() {
            page.navigate(BASE_URL + "/EmployeeService?wsdl");
            String content = page.content();
            assertTrue(content.contains("soap12:binding"),
                    "WSDL should contain soap12:binding for SOAP 1.2");
        }

        @Test
        @DisplayName("WSDL declares all five operations")
        void wsdlDeclaresAllOperations() {
            page.navigate(BASE_URL + "/EmployeeService?wsdl");
            String content = page.content();
            String[] expectedOps = {
                    "getEmployee", "getAllEmployees", "createEmployee",
                    "updateEmployee", "deleteEmployee"
            };
            for (String op : expectedOps) {
                assertTrue(content.contains(op),
                        "WSDL should contain operation: " + op);
            }
        }

        @Test
        @DisplayName("WSDL references the correct target namespace")
        void wsdlTargetNamespace() {
            page.navigate(BASE_URL + "/EmployeeService?wsdl");
            String content = page.content();
            assertTrue(content.contains("http://service.soap.enterprise.com/"),
                    "WSDL should reference the service target namespace");
        }
    }
}