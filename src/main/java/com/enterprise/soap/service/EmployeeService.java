package com.enterprise.soap.service;

import com.enterprise.soap.exception.EmployeeServiceException;
import com.enterprise.soap.model.Employee;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import java.util.List;

/**
 * Service Endpoint Interface (SEI) — the SOAP contract.
 * <p>
 * This is the <b>most widely used JAX-WS pattern</b>:
 * <ol>
 *   <li>Define the SEI (this interface) with {@code @WebService}.</li>
 *   <li>Implement it in a concrete class that references this interface
 *       via {@code endpointInterface}.</li>
 * </ol>
 * </p>
 *
 * <h3>SOAP 1.2 Binding</h3>
 * <ul>
 *   <li>{@code SOAPBinding.Style.DOCUMENT} — industry standard (WS-I compliant).</li>
 *   <li>{@code SOAPBinding.Use.LITERAL} — no SOAP encoding overhead.</li>
 *   <li>{@code SOAPBinding.ParameterStyle.WRAPPED} — each operation gets a wrapper element.</li>
 * </ul>
 */
@WebService(
    name            = "EmployeeService",
    targetNamespace = "http://service.soap.enterprise.com/"
)
@SOAPBinding(
    style          = SOAPBinding.Style.DOCUMENT,
    use            = SOAPBinding.Use.LITERAL,
    parameterStyle = SOAPBinding.ParameterStyle.WRAPPED
)
public interface EmployeeService {

    /**
     * Retrieve an employee by ID.
     */
    @WebMethod(operationName = "getEmployee")
    @WebResult(name = "employee")
    Employee getEmployee(
            @WebParam(name = "employeeId") Long employeeId
    ) throws EmployeeServiceException;

    /**
     * Retrieve all employees.
     */
    @WebMethod(operationName = "getAllEmployees")
    @WebResult(name = "employees")
    List<Employee> getAllEmployees();

    /**
     * Create a new employee record.
     */
    @WebMethod(operationName = "createEmployee")
    @WebResult(name = "createdEmployee")
    Employee createEmployee(
            @WebParam(name = "employee") Employee employee
    ) throws EmployeeServiceException;

    /**
     * Update an existing employee.
     */
    @WebMethod(operationName = "updateEmployee")
    @WebResult(name = "updatedEmployee")
    Employee updateEmployee(
            @WebParam(name = "employee") Employee employee
    ) throws EmployeeServiceException;

    /**
     * Delete an employee by ID.  Returns {@code true} on success.
     */
    @WebMethod(operationName = "deleteEmployee")
    @WebResult(name = "success")
    boolean deleteEmployee(
            @WebParam(name = "employeeId") Long employeeId
    ) throws EmployeeServiceException;
}
