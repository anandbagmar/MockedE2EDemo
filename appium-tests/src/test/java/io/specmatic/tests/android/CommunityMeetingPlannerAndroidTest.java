package io.specmatic.tests.android;

import com.applitools.eyes.appium.Eyes;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.specmatic.tests.BaseTest;
import io.specmatic.utils.Wait;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * CommunityMeetingPlannerAndroidTest
 *
 * Traverses the Community Meeting Planner workflow on Android.
 *
 * Flow selection:
 *   USE_ALTERNATE_FLOW = false  →  Original flow  (standard happy-path)
 *   USE_ALTERNATE_FLOW = true   →  Alternate flow (different attendee / meeting type)
 *
 * Override at runtime:
 *   mvn test -DUSE_ALTERNATE_FLOW=true -Pandroid
 *
 * App source:
 *   Loads from dist/ — run scripts/build-android-apks.sh [debug|release] first.
 *   For NML app run with IS_NML=true or set IS_NML env var.
 */
public class CommunityMeetingPlannerAndroidTest extends BaseTest {

    private static final String APP_NAME = "Community Meeting Planner (Android)";

    /**
     * Toggle between the original and alternate user flow.
     *
     * Original flow  – schedules a "Team Standup" meeting with 3 attendees (virtual).
     * Alternate flow – schedules a "Community Workshop" meeting with 5 attendees (in-person).
     */
    private static final boolean USE_ALTERNATE_FLOW =
            "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
                    || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));

    // ── React Native testID / accessibility-label values ──────────────────
    // (These match the testID constants in App.tsx and the WebView content.)

    // Native layer
    private static final String HOME_SCREEN          = "home.screen";
    private static final String HOME_BTN_RECHARGE    = "home.btn.recharge";   // entry into planner
    private static final String RECHARGE_SCREEN      = "recharge.screen";
    private static final String RECHARGE_WEBVIEW     = "recharge.webview";

    // ── WebView / web-content IDs ─────────────────────────────────────────
    // Update these to match your Specmatic mock server's HTML element IDs/xpaths.

    // Planner home
    private static final String WEB_PLANNER_TITLE     = "planner-title";
    private static final String WEB_NEW_MEETING_BTN   = "btn-new-meeting";

    // Create meeting form
    private static final String WEB_MEETING_TITLE_INPUT = "input-meeting-title";
    private static final String WEB_MEETING_DATE_INPUT  = "input-meeting-date";
    private static final String WEB_MEETING_TIME_INPUT  = "input-meeting-time";

    // Attendee fields
    private static final String WEB_ATTENDEE_1_INPUT  = "input-attendee-1";
    private static final String WEB_ATTENDEE_2_INPUT  = "input-attendee-2";
    private static final String WEB_ATTENDEE_3_INPUT  = "input-attendee-3";
    private static final String WEB_ATTENDEE_4_INPUT  = "input-attendee-4";
    private static final String WEB_ATTENDEE_5_INPUT  = "input-attendee-5";

    // Meeting type radio/select
    private static final String WEB_TYPE_VIRTUAL      = "type-virtual";
    private static final String WEB_TYPE_IN_PERSON    = "type-in-person";

    // Form actions
    private static final String WEB_SUBMIT_BTN        = "btn-submit-meeting";
    private static final String WEB_CONFIRM_BTN       = "btn-confirm";
    private static final String WEB_CONFIRMATION_MSG  = "confirmation-message";

    // ── Flow data ─────────────────────────────────────────────────────────

    // Original flow
    private static final String ORIG_TITLE    = "Team Standup";
    private static final String ORIG_DATE     = "2026-05-01";
    private static final String ORIG_TIME     = "09:00";
    private static final String[] ORIG_ATTENDEES = {"alice@example.com", "bob@example.com", "carol@example.com"};
    private static final String ORIG_TYPE     = WEB_TYPE_VIRTUAL;

    // Alternate flow
    private static final String ALT_TITLE     = "Community Workshop";
    private static final String ALT_DATE      = "2026-05-15";
    private static final String ALT_TIME      = "14:00";
    private static final String[] ALT_ATTENDEES = {"dave@example.com", "eve@example.com",
            "frank@example.com", "grace@example.com", "heidi@example.com"};
    private static final String ALT_TYPE      = WEB_TYPE_IN_PERSON;

    // ══════════════════════════════════════════════════════════════════════

    @BeforeMethod
    public void setUp(Method testInfo) {
        System.out.printf("[Android] Setting up test: %s  alternateFlow=%b  nml=%b%n",
                testInfo.getName(), USE_ALTERNATE_FLOW, IS_NML);

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName("Android");
        options.setAutoGrantPermissions(true);
        options.setFullReset(true);
        options.setCapability("printPageSourceOnFindFailure", true);

        String appPath = resolveAppPath("android");
        options.setApp(appPath);
        System.out.printf("[Android] Using app: %s%n", appPath);

        if (IS_NML && IS_EYES_ENABLED) {
            // NML static instrumentation: inject Applitools capabilities into the driver
            Eyes.setMobileCapabilities(options, APPLITOOLS_API_KEY);
        }

        try {
            driver = new AndroidDriver(new URL(APPIUM_SERVER_URL), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + APPIUM_SERVER_URL, e);
        }

        configureEyes(APP_NAME, testInfo);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tests
    // ══════════════════════════════════════════════════════════════════════

    @Test(description = "Community Meeting Planner – schedule a meeting end-to-end")
    public void communityMeetingPlannerTest() {
        if (USE_ALTERNATE_FLOW) {
            runAlternateFlow();
        } else {
            runOriginalFlow();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Original flow  –  Team Standup / Virtual / 3 attendees
    // ══════════════════════════════════════════════════════════════════════

    private void runOriginalFlow() {
        System.out.println("[Android] Running ORIGINAL flow");

        // 1. Home screen
        WebElement homeScreen = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");

        // 2. Open the Community Meeting Planner (WebView)
        WebElement plannerBtn = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(HOME_BTN_RECHARGE));
        checkpoint("Home – Planner Button");
        plannerBtn.click();

        // 3. Wait for WebView / Planner home
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(RECHARGE_SCREEN));
        switchToWebViewContext();

        WebElement plannerTitle = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_PLANNER_TITLE));
        checkpoint("Planner Home");
        plannerTitle.getText(); // assert visible

        // 4. Tap "New Meeting"
        WebElement newMeetingBtn = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_NEW_MEETING_BTN));
        checkpoint("New Meeting Button");
        newMeetingBtn.click();

        // 5. Fill meeting details
        fillMeetingDetails(ORIG_TITLE, ORIG_DATE, ORIG_TIME, ORIG_TYPE);

        // 6. Add attendees
        fillAttendees(ORIG_ATTENDEES);

        checkpoint("Meeting Form – Filled (Original)");

        // 7. Submit
        submitAndConfirm();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Alternate flow  –  Community Workshop / In-person / 5 attendees
    // ══════════════════════════════════════════════════════════════════════

    private void runAlternateFlow() {
        System.out.println("[Android] Running ALTERNATE flow");

        // 1. Home screen
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");

        // 2. Open the Community Meeting Planner (WebView)
        WebElement plannerBtn = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(HOME_BTN_RECHARGE));
        checkpoint("Home – Planner Button");
        plannerBtn.click();

        // 3. Wait for WebView / Planner home
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(RECHARGE_SCREEN));
        switchToWebViewContext();

        Wait.waitTillElementIsPresent(driver, AppiumBy.id(WEB_PLANNER_TITLE));
        checkpoint("Planner Home");

        // 4. Tap "New Meeting"
        Wait.waitTillElementIsClickable(driver, AppiumBy.id(WEB_NEW_MEETING_BTN)).click();

        // 5. Fill meeting details (alternate)
        fillMeetingDetails(ALT_TITLE, ALT_DATE, ALT_TIME, ALT_TYPE);

        // 6. Add attendees (more than original)
        fillAttendees(ALT_ATTENDEES);

        checkpoint("Meeting Form – Filled (Alternate)");

        // 7. Submit
        submitAndConfirm();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Shared helpers
    // ══════════════════════════════════════════════════════════════════════

    /** Switch Appium context to the first available WEBVIEW. */
    private void switchToWebViewContext() {
        Wait.waitFor(2); // let WebView finish loading
        for (String ctx : driver.getContextHandles()) {
            if (ctx.startsWith("WEBVIEW")) {
                driver.context(ctx);
                System.out.printf("[Android] Switched to context: %s%n", ctx);
                return;
            }
        }
        System.out.println("[Android] No WEBVIEW context found – staying in NATIVE_APP context");
    }

    private void fillMeetingDetails(String title, String date, String time, String meetingTypeId) {
        // Title
        WebElement titleInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_MEETING_TITLE_INPUT));
        titleInput.clear();
        titleInput.sendKeys(title);
        checkpoint("Meeting Title Entered");

        // Date
        WebElement dateInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_MEETING_DATE_INPUT));
        dateInput.clear();
        dateInput.sendKeys(date);
        checkpoint("Meeting Date Entered");

        // Time
        WebElement timeInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_MEETING_TIME_INPUT));
        timeInput.clear();
        timeInput.sendKeys(time);
        checkpoint("Meeting Time Entered");

        // Meeting type
        WebElement typeOption = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(meetingTypeId));
        typeOption.click();
        checkpoint("Meeting Type Selected: " + meetingTypeId);
    }

    private void fillAttendees(String[] attendees) {
        String[] inputIds = {
                WEB_ATTENDEE_1_INPUT, WEB_ATTENDEE_2_INPUT, WEB_ATTENDEE_3_INPUT,
                WEB_ATTENDEE_4_INPUT, WEB_ATTENDEE_5_INPUT
        };
        for (int i = 0; i < attendees.length && i < inputIds.length; i++) {
            WebElement field = Wait.waitTillElementIsPresent(driver,
                    AppiumBy.id(inputIds[i]));
            field.clear();
            field.sendKeys(attendees[i]);
        }
        checkpoint("Attendees Filled (" + attendees.length + ")");
    }

    private void submitAndConfirm() {
        // Submit form
        WebElement submitBtn = Wait.waitTillElementIsClickable(driver,
                AppiumBy.id(WEB_SUBMIT_BTN));
        checkpoint("Review Screen");
        submitBtn.click();

        // Confirm on review/summary screen
        WebElement confirmBtn = Wait.waitTillElementIsClickable(driver,
                AppiumBy.id(WEB_CONFIRM_BTN));
        checkpoint("Confirmation Screen");
        confirmBtn.click();

        // Final confirmation message
        Wait.waitTillElementIsPresent(driver, AppiumBy.id(WEB_CONFIRMATION_MSG));
        checkpoint("Meeting Scheduled – Confirmation");
    }
}
