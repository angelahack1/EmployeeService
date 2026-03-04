package com.enterprise.soap.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight XML response parser for SOAP test assertions.
 *
 * <p>Uses regex extraction — deliberately avoids heavy XML/DOM
 * dependencies so the test module stays lean. This is perfectly
 * adequate for validating simple, non-nested tag values in SOAP
 * responses.</p>
 */
public final class SoapResponseParser {

    private SoapResponseParser() { /* utility class */ }

    /**
     * Extract every value of a given XML tag from the response.
     * Handles namespace-prefixed tags, e.g. {@code <ns2:firstName>}.
     *
     * @param xml     full SOAP XML response body
     * @param tagName local tag name (without namespace prefix)
     * @return list of inner-text values, possibly empty
     */
    public static List<String> extractAll(String xml, String tagName) {
        // Matches <prefix:tagName>value</prefix:tagName> or <tagName>value</tagName>
        Pattern pattern = Pattern.compile(
                "<(?:[\\w]+:)?" + Pattern.quote(tagName) + "[^>]*>"
                        + "([^<]*)"
                        + "</(?:[\\w]+:)?" + Pattern.quote(tagName) + ">"
        );
        Matcher matcher = pattern.matcher(xml);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group(1));
        }
        return results;
    }

    /**
     * Extract the first occurrence of a tag value, or {@code null}.
     */
    public static String extractFirst(String xml, String tagName) {
        List<String> values = extractAll(xml, tagName);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Returns {@code true} if the response contains a SOAP Fault.
     */
    public static boolean isSoapFault(String xml) {
        return xml.contains(":Fault") || xml.contains("<Fault");
    }

    /**
     * Extract the fault reason text from a SOAP 1.2 Fault.
     * Falls back to {@code <faultstring>} for SOAP 1.1 compatibility.
     */
    public static String extractFaultReason(String xml) {
        // SOAP 1.2: <env:Reason><env:Text xml:lang="en">...</env:Text></env:Reason>
        String reason = extractFirst(xml, "Text");
        if (reason != null) return reason;
        // SOAP 1.1 fallback
        return extractFirst(xml, "faultstring");
    }
}