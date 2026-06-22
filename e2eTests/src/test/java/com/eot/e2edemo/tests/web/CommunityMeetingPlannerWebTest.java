package com.eot.e2edemo.tests.web;

import java.time.Duration;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import com.eot.e2edemo.tests.Wait;

/**
 * CommunityMeetingPlannerWebTest
 *
 * Web sibling to the Android/iOS Appium tests. Drives the responsive webapp
 * (../../../webapp) through the same workflow using the shared, unique
 * {@code data-testid} locators that mirror the mobile {@code testID} scheme:
 *
 * Home → flow selection → Planner → GuestLookup (with validation)
 * → Checklist → Summary → back to Home
 *
 * Unlike the mobile tests, this is a self-contained Selenium + TestNG test and
 * deliberately does NOT extend {@link com.eot.e2edemo.tests.BaseTest} (which boots
 * an Appium server and uses the Appium flavour of Applitools Eyes).
 *
 * Configuration (system properties / env vars):
 * WEB_BASE_URL base URL of the running webapp (default http://localhost:5173)
 * USE_ALTERNATE_FLOW "true" to exercise the alternate flow (default original)
 * headless "true" to run Chrome headless (default false / visible)
 *
 * Run:
 * cd e2eTests
 * ./gradlew runWeb # against http://localhost:5173
 * ./gradlew runWeb -DWEB_BASE_URL=https://anandbagmar.github.io/MockedE2EDemo/
 * ./gradlew runWeb -DUSE_ALTERNATE_FLOW=true -Dheadless=true
 */
public class CommunityMeetingPlannerWebTest {

    private static final String DEFAULT_BASE_URL =
            // "http://localhost:5173";
            "https://essenceoftesting.com/MockedE2EDemo";

    private static final int WEB_TIMEOUT_SECONDS = 20;

    private static final boolean USE_ALTERNATE_FLOW = flag("USE_ALTERNATE_FLOW");
    private static final boolean HEADLESS = flag("headless");

    private static final Pattern UNIQUE_ID_PATTERN = Pattern.compile("CMP-[A-Z0-9]+");

    // ── Locators (data-testid values shared with the mobile testID scheme) ────
    private static final String HOME_SCREEN = "home.screen";
    private static final String HOME_NAME_INPUT = "home.input.name";
    private static final String HOME_FLOW_ORIGINAL_BTN = "home.button.flow.original";
    private static final String HOME_FLOW_ALTERNATE_BTN = "home.button.flow.alternate";

    private static final String PLANNER_SCREEN = "planner.screen";
    private static final String PLANNER_INTRO = "planner.section.intro";
    private static final String PLANNER_NEXT_BTN = "planner.button.next";

    private static final String NATIVE_JOURNEY_SCREEN = "nativeJourney.screen";
    private static final String NATIVE_JOURNEY_CONTINUE_BTN = "nativeJourney.button.continue";
    private static final String NATIVE_HYBRID_SCREEN = "nativeHybrid.screen";
    private static final String NATIVE_HYBRID_CONTINUE_BTN = "nativeHybrid.button.continue";

    private static final String GUEST_LOOKUP_SCREEN = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT = "guestLookup.input.count";
    private static final String GUEST_LOOKUP_FETCH_BTN = "guestLookup.button.fetch";
    private static final String GUEST_LOOKUP_RESULTS = "guestLookup.results";
    private static final String GUEST_LOOKUP_FIRST_CARD = "guestLookup.profile.1.card";
    private static final String GUEST_LOOKUP_ALERT_CARD = "guestLookup.alert.card";
    private static final String GUEST_LOOKUP_ALERT_TITLE = "guestLookup.alert.title";
    private static final String GUEST_LOOKUP_ALERT_OK = "guestLookup.alert.ok";
    private static final String GUEST_LOOKUP_NEXT_BTN = "guestLookup.button.next";

    private static final String CHECKLIST_SCREEN = "webChecklist.screen";
    private static final String CHECKLIST_CONFIRM_BTN = "webChecklist.button.confirm";
    private static final String CHECKLIST_READY = "webChecklist.ready";
    private static final String CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue";

    private static final String SUMMARY_SCREEN = "summary.screen";
    private static final String SUMMARY_THANKYOU_TEXT = "summary.thankYou.text";
    private static final String SUMMARY_UNIQUE_ID = "summary.uniqueId";
    private static final String SUMMARY_RESTART_BTN = "summary.button.restart";

    private static final String TEST_NAME = "Test Automation";

    private WebDriver driver;
    private String baseUrl;

    @BeforeMethod
    public void setUp() {
        baseUrl = property("WEB_BASE_URL", DEFAULT_BASE_URL);
        System.out.printf("[Web] Setting up test  baseUrl=%s  alternateFlow=%b  headless=%b%n",
                baseUrl, USE_ALTERNATE_FLOW, HEADLESS);

        WebDriverManager.chromedriver().setup();

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);

        ChromeOptions options = new ChromeOptions();
        options.setCapability("goog:loggingPrefs", logPrefs);
        options.addArguments("--window-size=1280,900");
        if (HEADLESS) {
            // --no-sandbox / --disable-dev-shm-usage are required for headless
            // Chrome on CI runners (e.g. GitHub Actions).
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(description = "Community Meeting Planner web – full workflow end-to-end")
    public void communityMeetingPlannerWebTest() {
        handleHomeScreen();
        handlePlannerScreen();
        handleNativeInterludeScreens();
        handleGuestLookupScreen();
        handleChecklistScreen();
        handleSummaryScreen();
    }

    // ── Steps ─────────────────────────────────────────────────────────────────

    private void handleHomeScreen() {
        logStep("Step 1 - open webapp and wait for home screen");
        driver.get(baseUrl.replaceAll("/+$", "") + "/#/");
        waitVisible(HOME_SCREEN);

        logStep("Step 2 - enter name");
        WebElement nameInput = waitVisible(HOME_NAME_INPUT);
        nameInput.clear();
        nameInput.sendKeys(TEST_NAME);

        logStep("Step 3 - choose flow variant");
        click(USE_ALTERNATE_FLOW ? HOME_FLOW_ALTERNATE_BTN : HOME_FLOW_ORIGINAL_BTN);
    }

    private void handlePlannerScreen() {
        logStep("Step 4 - verify planner screen");
        waitVisible(PLANNER_SCREEN);
        waitVisible(PLANNER_INTRO);

        logStep("Step 5 - continue to native detail");
        click(PLANNER_NEXT_BTN);
    }

    /**
     * Web parity for the mobile native interlude (Step 2A/2B). The webapp now
     * renders the same two journey steps as standard web components between the
     * planner and guest lookup, so the workflow stays identical across platforms.
     */
    private void handleNativeInterludeScreens() {
        logStep("Step 5a - verify native journey screen");
        waitVisible(NATIVE_JOURNEY_SCREEN);
        click(NATIVE_JOURNEY_CONTINUE_BTN);

        logStep("Step 5b - verify native hybrid screen");
        waitVisible(NATIVE_HYBRID_SCREEN);
        click(NATIVE_HYBRID_CONTINUE_BTN);
    }

    private void handleGuestLookupScreen() {
        logStep("Step 6 - verify guest lookup screen");
        waitVisible(GUEST_LOOKUP_SCREEN);

        logStep("Step 7 - validate out-of-range guest count");
        WebElement countInput = waitVisible(GUEST_LOOKUP_INPUT);
        countInput.clear();
        countInput.sendKeys("20");
        click(GUEST_LOOKUP_FETCH_BTN);

        WebElement alertTitle = waitVisible(GUEST_LOOKUP_ALERT_TITLE);
        Assert.assertEquals(alertTitle.getText().trim(), "Invalid guest count",
                "Expected validation alert for out-of-range guest count");

        logStep("Step 8 - dismiss validation alert");
        click(GUEST_LOOKUP_ALERT_OK);
        waitInvisible(GUEST_LOOKUP_ALERT_CARD);

        logStep("Step 9 - load guest profiles");
        countInput = waitVisible(GUEST_LOOKUP_INPUT);
        countInput.clear();
        countInput.sendKeys("3");
        click(GUEST_LOOKUP_FETCH_BTN);

        logStep("Step 10 - wait for guest results");
        waitVisible(GUEST_LOOKUP_RESULTS);
        waitVisible(GUEST_LOOKUP_FIRST_CARD);

        logStep("Step 11 - open checklist");
        click(GUEST_LOOKUP_NEXT_BTN);
    }

    private void handleChecklistScreen() {
        logStep("Step 12 - verify checklist screen");
        waitVisible(CHECKLIST_SCREEN);

        logStep("Step 13 - confirm checklist");
        click(CHECKLIST_CONFIRM_BTN);
        waitVisible(CHECKLIST_READY);

        logStep("Step 14 - complete workflow");
        click(CHECKLIST_CONTINUE_BTN);
    }

    private void handleSummaryScreen() {
        logStep("Step 15 - verify summary screen");
        waitVisible(SUMMARY_SCREEN);

        String thankYou = waitVisible(SUMMARY_THANKYOU_TEXT).getText().trim();
        Assert.assertTrue(thankYou.contains(TEST_NAME),
                "Thank-you message should contain the entered name. Was: " + thankYou);

        String uniqueId = waitVisible(SUMMARY_UNIQUE_ID).getText().trim();
        Assert.assertTrue(UNIQUE_ID_PATTERN.matcher(uniqueId).matches(),
                "Unique id should match CMP-XXXX format. Was: " + uniqueId);
        System.out.printf("[Web] Summary shows uniqueId=%s%n", uniqueId);

        logStep("Step 16 - verify unique id was written to the browser log");
        assertUniqueIdLogged(uniqueId);

        logStep("Step 17 - restart flow and return home");
        click(SUMMARY_RESTART_BTN);
        waitVisible(HOME_SCREEN);
    }

    /**
     * The webapp logs the unique id to the browser console (the web equivalent
     * of the mobile app log). Assert that an entry carrying the displayed id is
     * present in Chrome's BROWSER log.
     */
    private void assertUniqueIdLogged(String uniqueId) {
        LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
        String matched = null;
        for (LogEntry entry : entries) {
            if (entry.getMessage().contains(uniqueId)) {
                matched = entry.getMessage();
                break;
            }
        }
        Assert.assertNotNull(matched,
                "Expected a browser console log entry containing uniqueId=" + uniqueId);

        Matcher idInLog = UNIQUE_ID_PATTERN.matcher(matched);
        Assert.assertTrue(idInLog.find() && idInLog.group().equals(uniqueId),
                "Logged uniqueId should match the one shown on screen. Log line: " + matched);
        System.out.printf("[Web] Browser log confirms: %s%n", matched.trim());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }

    private WebElement waitVisible(String testId) {
        return Wait.waitTillElementIsPresent(driver, byTestId(testId), WEB_TIMEOUT_SECONDS);
    }

    private void waitInvisible(String testId) {
        Wait.waitTillElementDisappears(driver, byTestId(testId));
    }

    private void click(String testId) {
        Wait.waitTillElementIsClickable(driver, byTestId(testId), WEB_TIMEOUT_SECONDS).click();
    }

    private void logStep(String step) {
        System.out.printf("%n[Web] STEP: %s%n", step);
    }

    private static boolean flag(String key) {
        return "true".equalsIgnoreCase(System.getenv(key))
                || "true".equalsIgnoreCase(System.getProperty(key));
    }

    private static String property(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
