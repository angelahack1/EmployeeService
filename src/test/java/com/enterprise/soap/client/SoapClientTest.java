package com.enterprise.soap.client;

import com.enterprise.soap.model.Employee;
import com.enterprise.soap.service.EmployeeService;

import jakarta.xml.ws.Service;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Standalone SOAP 1.2 test client.
 * <p>
 * Run this <b>after</b> the service is deployed on GlassFish.
 * It uses the JAX-WS dynamic proxy to invoke every operation.
 * </p>
 *
 * <h3>How to run</h3>
 * <pre>
 *   1. Deploy soap-service.war on GlassFish 7
 *   2. Right-click this class in Eclipse → Run As → Java Application
 *      (make sure jakarta.xml.ws-api is on the classpath,
 *       or run within a managed Jakarta EE client container)
 * </pre>
 */
public class SoapClientTest {

    // Must match the @WebService attributes in EmployeeServiceImpl
    private static final String WSDL_URL =
            "http://localhost:8080/EmployeeService/EmployeeService?wsdl";
    private static final String NAMESPACE =
            "http://service.soap.enterprise.com/";
    private static final String SERVICE_NAME =
            "EmployeeService";

    public static void main(String[] args) {
        try {
            System.out.println("══════════════════════════════════════════════");
            System.out.println("  SOAP 1.2 Client Test — Employee Service");
            System.out.println("══════════════════════════════════════════════\n");

            // ── 1. Build the service proxy ─────────────────────────────
            URL wsdlUrl = URI.create(WSDL_URL).toURL();
            QName serviceName = new QName(NAMESPACE, SERVICE_NAME);
            Service service = Service.create(wsdlUrl, serviceName);
            EmployeeService port = service.getPort(EmployeeService.class);

            System.out.println("[OK] Connected to WSDL: " + WSDL_URL + "\n");

            // ── 2. getAllEmployees ──────────────────────────────────────
            System.out.println("─── getAllEmployees ───");
            List<Employee> employees = port.getAllEmployees();
            for (Employee emp : employees) {
                System.out.println("  " + emp);
            }
            System.out.println("  Total: " + employees.size() + " employees\n");

            // ── 3. getEmployee ─────────────────────────────────────────
            System.out.println("─── getEmployee(1) ───");
            Employee single = port.getEmployee(1L);
            System.out.println("  " + single + "\n");

            // ── 4. createEmployee ──────────────────────────────────────
            System.out.println("─── createEmployee ───");
            Employee newEmp = new Employee();
            newEmp.setFirstName("Ana");
            newEmp.setLastName("Hernández");
            newEmp.setEmail("ana.hernandez@banxico.mx");
            newEmp.setDepartment("Risk Analysis");
            Employee created = port.createEmployee(newEmp);
            System.out.println("  Created: " + created + "\n");

            // ── 5. updateEmployee ──────────────────────────────────────
            System.out.println("─── updateEmployee ───");
            created.setDepartment("Quantitative Risk");
            Employee updated = port.updateEmployee(created);
            System.out.println("  Updated: " + updated + "\n");

            // ── 6. deleteEmployee ──────────────────────────────────────
            System.out.println("─── deleteEmployee(" + created.getId() + ") ───");
            boolean deleted = port.deleteEmployee(created.getId());
            System.out.println("  Deleted: " + deleted + "\n");

            // ── 7. Verify deletion ─────────────────────────────────────
            System.out.println("─── Final employee list ───");
            for (Employee emp : port.getAllEmployees()) {
                System.out.println("  " + emp);
            }

            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  ALL TESTS PASSED ✓");
            System.out.println("══════════════════════════════════════════════");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
