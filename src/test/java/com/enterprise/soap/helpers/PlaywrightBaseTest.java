package com.enterprise.soap.helpers;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for all Playwright test classes.
 *
 * <p>Manages the full Playwright lifecycle:</p>
 * <ul>
 *   <li>{@code @BeforeAll} — starts Playwright + launches browser</li>
 *   <li>{@code @BeforeEach} — creates a fresh BrowserContext + Page</li>
 *   <li>{@code @AfterEach} — closes context (isolates tests)</li>
 *   <li>{@code @AfterAll} — closes browser + stops Playwright</li>
 * </ul>
 *
 * <p>Also provides an {@link APIRequestContext} for direct HTTP calls
 * (used by the SOAP API tests that don't need a browser).</p>
 *
 * <h3>Configuration via system properties</h3>
 * <ul>
 *   <li>{@code soap.base.url} — GlassFish base URL (default: {@code http://localhost:8080/EmployeeService})</li>
 *   <li>{@code playwright.headless} — {@code true} (default) or {@code false} to watch the browser</li>
 * </ul>
 */
public abstract class PlaywrightBaseTest {

    // ──────────────────────────────────────────────────────────
    //  Shared across ALL tests in the class (static lifecycle)
    // ──────────────────────────────────────────────────────────
    protected static Playwright playwright;
    protected static Browser browser;
    protected static APIRequestContext apiContext;

    // ──────────────────────────────────────────────────────────
    //  Fresh per-test (instance lifecycle)
    // ──────────────────────────────────────────────────────────
    protected BrowserContext context;
    protected Page page;

    // ──────────────────────────────────────────────────────────
    //  Configuration
    // ──────────────────────────────────────────────────────────
    protected static final String BASE_URL =
            System.getProperty("soap.base.url", "http://localhost:8080/EmployeeService");

    protected static final String ENDPOINT = BASE_URL + "/EmployeeService";

    protected static final String SOAP_CONTENT_TYPE = "application/soap+xml;charset=UTF-8";

    private static boolean isHeadless() {
        return !"false".equalsIgnoreCase(System.getProperty("playwright.headless"));
    }

    /* ================================================================== */
    /*  Lifecycle — class level                                           */
    /* ================================================================== */

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(isHeadless())
        );

        // APIRequestContext for raw HTTP/SOAP calls (no browser needed)
        apiContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(BASE_URL)
        );
    }

    @AfterAll
    static void closeBrowser() {
        if (apiContext != null) apiContext.dispose();
        if (browser != null)   browser.close();
        if (playwright != null) playwright.close();
    }

    /* ================================================================== */
    /*  Lifecycle — per test                                              */
    /* ================================================================== */

    @BeforeEach
    void createContext() {
        context = browser.newContext(
                new Browser.NewContextOptions().setBaseURL(BASE_URL)
        );
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    /* ================================================================== */
    /*  Helper: POST a SOAP envelope and return the response              */
    /* ================================================================== */

    /**
     * Sends a SOAP 1.2 POST to the EmployeeService endpoint.
     *
     * @param soapBody complete SOAP envelope XML
     * @return the API response
     */
    protected APIResponse soapPost(String soapBody) {
        return apiContext.post("/EmployeeService/EmployeeService", RequestOptions.create()
                .setHeader("Content-Type", SOAP_CONTENT_TYPE)
                .setData(soapBody)
        );
    }
}