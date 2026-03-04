package com.enterprise.soap.helpers;

/**
 * Builds SOAP 1.2 request envelopes for every operation
 * exposed by the EmployeeService.
 *
 * <p>Each method returns a complete {@code <soap:Envelope>} string
 * ready to POST against the SOAP endpoint.</p>
 */
public final class SoapEnvelopeBuilder {

    private static final String SOAP_NS = "http://www.w3.org/2003/05/soap-envelope";
    private static final String SVC_NS  = "http://service.soap.enterprise.com/";

    private SoapEnvelopeBuilder() { /* utility class */ }

    /* ------------------------------------------------------------------ */
    /*  getAllEmployees                                                    */
    /* ------------------------------------------------------------------ */
    public static String getAllEmployees() {
        return envelope("<ser:getAllEmployees/>");
    }

    /* ------------------------------------------------------------------ */
    /*  getEmployee                                                       */
    /* ------------------------------------------------------------------ */
    public static String getEmployee(long id) {
        return envelope(
            "<ser:getEmployee>" +
            "  <employeeId>" + id + "</employeeId>" +
            "</ser:getEmployee>"
        );
    }

    /* ------------------------------------------------------------------ */
    /*  createEmployee                                                    */
    /* ------------------------------------------------------------------ */
    public static String createEmployee(String firstName, String lastName,
                                        String email, String department) {
        return envelope(
            "<ser:createEmployee>" +
            "  <employee>" +
            "    <firstName>" + esc(firstName) + "</firstName>" +
            "    <lastName>"  + esc(lastName)  + "</lastName>" +
            "    <email>"     + esc(email)     + "</email>" +
            "    <department>"+ esc(department)+ "</department>" +
            "  </employee>" +
            "</ser:createEmployee>"
        );
    }

    /* ------------------------------------------------------------------ */
    /*  updateEmployee                                                    */
    /* ------------------------------------------------------------------ */
    public static String updateEmployee(long id, String firstName, String lastName,
                                        String email, String department) {
        return envelope(
            "<ser:updateEmployee>" +
            "  <employee>" +
            "    <id>" + id + "</id>" +
            "    <firstName>" + esc(firstName) + "</firstName>" +
            "    <lastName>"  + esc(lastName)  + "</lastName>" +
            "    <email>"     + esc(email)     + "</email>" +
            "    <department>"+ esc(department)+ "</department>" +
            "  </employee>" +
            "</ser:updateEmployee>"
        );
    }

    /* ------------------------------------------------------------------ */
    /*  deleteEmployee                                                    */
    /* ------------------------------------------------------------------ */
    public static String deleteEmployee(long id) {
        return envelope(
            "<ser:deleteEmployee>" +
            "  <employeeId>" + id + "</employeeId>" +
            "</ser:deleteEmployee>"
        );
    }

    /* ------------------------------------------------------------------ */
    /*  Internal helpers                                                  */
    /* ------------------------------------------------------------------ */
    private static String envelope(String bodyContent) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soap:Envelope xmlns:soap=\"" + SOAP_NS + "\"" +
               "               xmlns:ser=\"" + SVC_NS + "\">" +
               "  <soap:Header/>" +
               "  <soap:Body>" +
               "    " + bodyContent +
               "  </soap:Body>" +
               "</soap:Envelope>";
    }

    private static String esc(String value) {
        if (value == null) return "";
        return value
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&apos;");
    }
}