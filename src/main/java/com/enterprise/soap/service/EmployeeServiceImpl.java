package com.enterprise.soap.service;

import com.enterprise.soap.model.Employee;
import com.enterprise.soap.exception.EmployeeServiceException;

import jakarta.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SOAP 1.2 Employee Web Service — EJB-based endpoint.
 * <p>
 * {@code @Stateless} + {@code @WebService} is the <b>most reliable</b>
 * deployment pattern for GlassFish 7.  The EJB container publishes
 * the endpoint automatically — no sun-jaxws.xml, no WSServlet,
 * no web.xml configuration needed.
 * </p>
 */
@Stateless
@WebService(
    serviceName     = "EmployeeService",
    portName        = "EmployeeServicePort",
    name            = "EmployeeService",
    targetNamespace = "http://service.soap.enterprise.com/",
    endpointInterface = "com.enterprise.soap.service.EmployeeService"
)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG =
            Logger.getLogger(EmployeeServiceImpl.class.getName());

    // ── In-memory store (static so it survives EJB pooling) ────────────
    private static final Map<Long, Employee> STORE = new LinkedHashMap<>();
    private static final AtomicLong ID_SEQ = new AtomicLong(0);

    static {
        addSample("Carlos",  "García",   "carlos.garcia@corp.mx",  "Cybersecurity");
        addSample("María",   "López",    "maria.lopez@corp.mx",    "Development");
        addSample("Roberto", "Martínez", "roberto.martinez@corp.mx","Infrastructure");
    }

    private static void addSample(String first, String last, String email, String dept) {
        long id = ID_SEQ.incrementAndGet();
        STORE.put(id, new Employee(id, first, last, email, dept));
    }

    // ── Web Service operations ─────────────────────────────────────────

    @Override
    @WebMethod(operationName = "getEmployee")
    @WebResult(name = "employee")
    public Employee getEmployee(
            @WebParam(name = "employeeId", targetNamespace = "http://service.soap.enterprise.com/") Long employeeId
    ) throws EmployeeServiceException {
        LOG.log(Level.INFO, "getEmployee called — id={0}", employeeId);
        if (employeeId == null) {
            throw new EmployeeServiceException("Employee ID must not be null.");
        }
        Employee emp = STORE.get(employeeId);
        if (emp == null) {
            throw new EmployeeServiceException("Employee not found: " + employeeId);
        }
        return emp;
    }

    @Override
    @WebMethod(operationName = "getAllEmployees")
    @WebResult(name = "employees")
    public List<Employee> getAllEmployees() {
        LOG.info("getAllEmployees called");
        return new ArrayList<>(STORE.values());
    }

    @Override
    @WebMethod(operationName = "createEmployee")
    @WebResult(name = "createdEmployee")
    public Employee createEmployee(
            @WebParam(name = "employee", targetNamespace = "") Employee employee
    ) throws EmployeeServiceException {
        LOG.info("createEmployee called");
        if (employee == null) {
            throw new EmployeeServiceException("Employee data must not be null.");
        }
        long newId = ID_SEQ.incrementAndGet();
        employee.setId(newId);
        STORE.put(newId, employee);
        return employee;
    }

    @Override
    @WebMethod(operationName = "updateEmployee")
    @WebResult(name = "updatedEmployee")
    public Employee updateEmployee(
            @WebParam(name = "employee", targetNamespace = "") Employee employee
    ) throws EmployeeServiceException {
        LOG.info("updateEmployee called");
        if (employee == null || employee.getId() == null) {
            throw new EmployeeServiceException("Employee and ID must not be null.");
        }
        if (!STORE.containsKey(employee.getId())) {
            throw new EmployeeServiceException("Not found: " + employee.getId());
        }
        STORE.put(employee.getId(), employee);
        return employee;
    }

    @Override
    @WebMethod(operationName = "deleteEmployee")
    @WebResult(name = "success")
    public boolean deleteEmployee(
            @WebParam(name = "employeeId", targetNamespace = "http://service.soap.enterprise.com/") Long employeeId
    ) throws EmployeeServiceException {
        LOG.log(Level.INFO, "deleteEmployee called — id={0}", employeeId);
        if (employeeId == null) {
            throw new EmployeeServiceException("Employee ID must not be null.");
        }
        if (STORE.remove(employeeId) == null) {
            throw new EmployeeServiceException("Not found: " + employeeId);
        }
        return true;
    }
}