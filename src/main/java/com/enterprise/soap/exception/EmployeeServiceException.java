package com.enterprise.soap.exception;

import jakarta.xml.ws.WebFault;
import java.io.Serializable;

/**
 * Application-level SOAP fault.
 * <p>
 * The {@code @WebFault} annotation maps this exception to a WSDL fault element,
 * producing a well-formed {@code <soap:Fault>} in the SOAP 1.2 response.
 * </p>
 */
@WebFault(name = "EmployeeServiceFault",
          targetNamespace = "http://service.soap.enterprise.com/")
public class EmployeeServiceException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String faultInfo;

    public EmployeeServiceException(String message) {
        super(message);
        this.faultInfo = message;
    }

    public EmployeeServiceException(String message, Throwable cause) {
        super(message, cause);
        this.faultInfo = message;
    }

    /**
     * JAX-WS runtime calls this to populate the fault detail element.
     */
    public String getFaultInfo() {
        return faultInfo;
    }
}
