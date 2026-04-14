package io.specmatic.tests.ios;

import com.applitools.eyes.appium.Eyes;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.specmatic.tests.BaseTest;
import io.specmatic.utils.Wait;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * CommunityMeetingPlannerIOSTest
 *
 * Traverses the Community Meeting Planner workflow on iOS (simulator).
 *
 * Flow selection:
 *   USE_ALTERNATE_FLOW = false  →  Original flow  (standard happy-path)
 *   USE_ALTERNATE_FLOW = true   →  Alternate flow (different attendee / meeting type)
 *
 * Override at runtime:
 *   mvn test -DUSE_ALTERNATE_FLOW=true -Pios
 *
 * App source:
 *   Loads from dist/ — run scripts/build-ios-app.sh [debug|release] first.
 *   The .app.zip is extracted to a temp dir automatically.
 *   For NML app run with IS_NML=true or set IS_NML env var.
 */
public class CommunityMeetingPlannerIOSTest extends BaseTest {

    private static final String APP_NAME = "Community Meeting Planner (iOS)";

    /**
     * Toggle between the original and alternate user flow.
     *
     * Original flow  – schedules a "Team Standup" meeting with 3 attendees (virtual).
     * Alternate flow – schedules a "Community Workshop" meeting with 5 attendees (in-person).
     */
    private static final boolean USE_ALTERNATE_FLOW =
            "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
                    || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));

    // iOS simulator to target (override via system property IOS_DEVICE_NAME)
    private static final String IOS_DEVICE_NAME =
            System.getProperty("IOS_DEVICE_NAME", "iPhone 16");

    // iOS platform version (override via system property IOS_PLATFORM_VERSION)
    private static final String IOS_PLATFORM_VERSION =
            System.getProperty("IOS_PLATFORM_VERSION", "18.4");

    // ── React Native testID / accessibility-label values ──────────────────

    // Native layer
    private static final String HOME_SCREEN       = "home.screen";
    private static final String HOME_BTN_RECHARGE = "home.btn.recharge";
    private static final String RECHARGE_SCREEN   = "recharge.screen";

    // ── WebView / web-content IDs ─────────────────────────────────────────
    // Update these to match your Specmatic mock server's HTML element IDs.

    private static final String WEB_PLANNER_TITLE      = "planner-title";
    private static final String WEB_NEW_MEETING_BTN    = "btn-new-meeting";
    private static final String WEB_MEETING_TITLE_INPUT= "input-meeting-title";
    private static final String WEB_MEETING_DATE_INPUT = "input-meeting-date";
    private static final String WEB_MEETING_TIME_INPUT = "input-meeting-time";
    private static final String WEB_ATTENDEE_1_INPUT   = "input-attendee-1";
    private static final String WEB_ATTENDEE_2_INPUT   = "input-attendee-2";
    private static final String WEB_ATTENDEE_3_INPUT   = "input-attendee-3";
    private static final String WEB_ATTENDEE_4_INPUT   = "input-attendee-4";
    private static final String WEB_ATTENDEE_5_INPUT   = "input-attendee-5";
    private static final String WEB_TYPE_VIRTUAL       = "type-virtual";
    private static final String WEB_TYPE_IN_PERSON     = "type-in-person";
    private static final String WEB_SUBMIT_BTN         = "btn-submit-meeting";
    private static final String WEB_CONFIRM_BTN        = "btn-confirm";
    private static final String WEB_CONFIRMATION_MSG   = "confirmation-message";

    // ── Flow data ─────────────────────────────────────────────────────────

    private static final String ORIG_TITLE      = "Team Standup";
    private static final String ORIG_DATE       = "2026-05-01";
    private static final String ORIG_TIME       = "09:00";
    private static final String[] ORIG_ATTENDEES = {"alice@example.com", "bob@example.com", "carol@example.com"};
    private static final String ORIG_TYPE       = WEB_TYPE_VIRTUAL;

    private static final String ALT_TITLE       = "Community Workshop";
    private static final String ALT_DATE        = "2026-05-15";
    private static final String ALT_TIME        = "14:00";
    private static final String[] ALT_ATTENDEES = {"dave@example.com", "eve@example.com",
            "frank@example.com", "grace@example.com", "heidi@example.com"};
    private static final String ALT_TYPE        = WEB_TYPE_IN_PERSON;

    // ── Temp dir for extracted .app ────────────────────────────────────────

    private Path extractedAppDir;

    // ══════════════════════════════════════════════════════════════════════

    @BeforeMethod
    public void setUp(Method testInfo) throws IOException {
        System.out.printf("[iOS] Setting up test: %s  alternateFlow=%b  nml=%b%n",
                testInfo.getName(), USE_ALTERNATE_FLOW, IS_NML);

        // Extract the .app.zip from dist/ to a temp dir
        String zipPath = resolveAppPath("ios");
        extractedAppDir = extractAppZip(zipPath);
        String appPath = findDotApp(extractedAppDir);
        System.out.printf("[iOS] Using .app: %s%n", appPath);

        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");
        options.setDeviceName(IOS_DEVICE_NAME);
        options.setPlatformVersion(IOS_PLATFORM_VERSION);
        options.setApp(appPath);
        options.setAutoAcceptAlerts(true);
        options.setFullReset(true);
        options.setCapability("printPageSourceOnFindFailure", true);

        if (IS_NML && IS_EYES_ENABLED) {
            Eyes.setMobileCapabilities(options, APPLITOOLS_API_KEY);
        }

        try {
            driver = new IOSDriver(new URL(APPIUM_SERVER_URL), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + APPIUM_SERVER_URL, e);
        }

        configureEyes(APP_NAME, testInfo);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tests
    // ══════════════════════════════════════════════════════════════════════

    @Test(description = "Community Meeting Planner – schedule a meeting end-to-end (iOS)")
    public void communityMeetingPlannerTest() {
        if (USE_ALTERNATE_FLOW) {
            runAlternateFlow();
        } else {
            runOriginalFlow();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Original flow
    // ══════════════════════════════════════════════════════════════════════

    private void runOriginalFlow() {
        System.out.println("[iOS] Running ORIGINAL flow");

        // 1. Home screen
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");

        // 2. Open planner
        WebElement plannerBtn = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(HOME_BTN_RECHARGE));
        checkpoint("Home – Planner Button");
        plannerBtn.click();

        // 3. WebView loads
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(RECHARGE_SCREEN));
        switchToWebViewContext();

        Wait.waitTillElementIsPresent(driver, AppiumBy.id(WEB_PLANNER_TITLE));
        checkpoint("Planner Home");

        // 4. New meeting
        Wait.waitTillElementIsClickable(driver, AppiumBy.id(WEB_NEW_MEETING_BTN)).click();

        // 5. Fill details
        fillMeetingDetails(ORIG_TITLE, ORIG_DATE, ORIG_TIME, ORIG_TYPE);
        fillAttendees(ORIG_ATTENDEES);
        checkpoint("Meeting Form – Filled (Original)");

        // 6. Submit
        submitAndConfirm();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Alternate flow
    // ══════════════════════════════════════════════════════════════════════

    private void runAlternateFlow() {
        System.out.println("[iOS] Running ALTERNATE flow");

        // 1. Home screen
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");

        // 2. Open planner
        Wait.waitTillElementIsClickable(driver,
                AppiumBy.accessibilityId(HOME_BTN_RECHARGE)).click();

        // 3. WebView loads
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(RECHARGE_SCREEN));
        switchToWebViewContext();

        Wait.waitTillElementIsPresent(driver, AppiumBy.id(WEB_PLANNER_TITLE));
        checkpoint("Planner Home");

        // 4. New meeting
        Wait.waitTillElementIsClickable(driver, AppiumBy.id(WEB_NEW_MEETING_BTN)).click();

        // 5. Fill details (alternate data)
        fillMeetingDetails(ALT_TITLE, ALT_DATE, ALT_TIME, ALT_TYPE);
        fillAttendees(ALT_ATTENDEES);
        checkpoint("Meeting Form – Filled (Alternate)");

        // 6. Submit
        submitAndConfirm();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Shared helpers
    // ══════════════════════════════════════════════════════════════════════

    /** Switch Appium context to the first available WEBVIEW. */
    private void switchToWebViewContext() {
        Wait.waitFor(2);
        for (String ctx : driver.getContextHandles()) {
            if (ctx.startsWith("WEBVIEW")) {
                driver.context(ctx);
                System.out.printf("[iOS] Switched to context: %s%n", ctx);
                return;
            }
        }
        System.out.println("[iOS] No WEBVIEW context found – staying in NATIVE_APP context");
    }

    private void fillMeetingDetails(String title, String date, String time, String meetingTypeId) {
        WebElement titleInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_MEETING_TITLE_INPUT));
        titleInput.clear();
        titleInput.sendKeys(title);
        checkpoint("Meeting Title Entered");

        WebElement dateInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_MEETING_DATE_INPUT));
        dateInput.clear();
        dateInput.sendKeys(date);
        checkpoint("Meeting Date Entered");

        WebElement timeInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.id(WEB_MEETING_TIME_INPUT));
        timeInput.clear();
        timeInput.sendKeys(time);
        checkpoint("Meeting Time Entered");

        Wait.waitTillElementIsClickable(driver, AppiumBy.id(meetingTypeId)).click();
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
        Wait.waitTillElementIsClickable(driver, AppiumBy.id(WEB_SUBMIT_BTN)).click();
        checkpoint("Review Screen");

        Wait.waitTillElementIsClickable(driver, AppiumBy.id(WEB_CONFIRM_BTN)).click();
        checkpoint("Confirmation Screen");

        Wait.waitTillElementIsPresent(driver, AppiumBy.id(WEB_CONFIRMATION_MSG));
        checkpoint("Meeting Scheduled – Confirmation");
    }

    // ── .app.zip extraction helpers ───────────────────────────────────────

    /** Extract a .app.zip to a temporary directory and return the temp dir path. */
    private Path extractAppZip(String zipPath) throws IOException {
        Path tempDir = Files.createTempDirectory("ios-app-");
        try (ZipInputStream zis = new ZipInputStream(
                Files.newInputStream(new File(zipPath).toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path dest = tempDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
        System.out.printf("[iOS] Extracted app to: %s%n", tempDir);
        return tempDir;
    }

    /** Find the first *.app directory inside the extracted temp dir. */
    private String findDotApp(Path baseDir) throws IOException {
        return Files.walk(baseDir)
                .filter(p -> p.toString().endsWith(".app") && Files.isDirectory(p))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No .app found inside extracted zip at: " + baseDir))
                .toAbsolutePath()
                .toString();
    }
}
