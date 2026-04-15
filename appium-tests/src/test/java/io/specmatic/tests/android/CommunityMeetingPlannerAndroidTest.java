package io.specmatic.tests.android;

import com.applitools.eyes.appium.Eyes;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.specmatic.tests.BaseTest;
import io.specmatic.utils.Wait;
import org.openqa.selenium.By;
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
 * Exercises the full Community Meeting Planner workflow on Android:
 *   Home → flow-selector modal → Planner → GuestLookup (with validation)
 *     → WebChecklist (inline WebView) → Summary → back to Home
 *
 * Flow selection:
 *   USE_ALTERNATE_FLOW = false  →  Original flow
 *   USE_ALTERNATE_FLOW = true   →  Alternate flow
 *
 * Override at runtime:
 *   ./gradlew test -DUSE_ALTERNATE_FLOW=true -Pandroid
 */
public class CommunityMeetingPlannerAndroidTest extends BaseTest {

    private static final String APP_NAME = "Community Meeting Planner (Android)";
    private static final String ANDROID_PACKAGE = "io.specmatic.e2edemo";

    private static final boolean USE_ALTERNATE_FLOW =
            "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
                    || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));

    // ── Native layer locators ─────────────────────────────────────────────────
    // All SafeAreaView screens have accessibilityLabel (= testID) after App.tsx fix.
    // All PrimaryButton / SecondaryButton elements always have accessibilityLabel = testID.
    // → AppiumBy.accessibilityId() matches content-desc on Android for all of these.

    private static final String HOME_SCREEN                = "home.screen";
    private static final String HOME_PLANNER_BTN           = "home.button.planner";

    // Flow-selector modal
    private static final String PLANNER_ORIGINAL_BTN       = "planner.mode.button.original";
    private static final String PLANNER_ALTERNATE_BTN      = "planner.mode.button.alternate";

    // Planner screen (native, scrollable)
    private static final String PLANNER_SCREEN             = "planner.screen";
    private static final String PLANNER_NEXT_BTN           = "planner.button.next";          // "Next: Load Guest Profiles"

    // Guest Lookup screen (native, scrollable)
    private static final String GUEST_LOOKUP_SCREEN        = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT         = "guestLookup.input.count";      // TextInput – has accessibilityLabel
    private static final String GUEST_LOOKUP_FETCH_BTN     = "guestLookup.button.fetch";     // "Load Profiles"
    private static final String GUEST_LOOKUP_RESULTS       = "guestLookup.section.results";  // SectionTitle – has accessibilityLabel; appears only after API load
    private static final String GUEST_LOOKUP_NEXT_BTN      = "guestLookup.button.next";      // "Next: Open Web Checklist"

    // Web Checklist screen (native shell + inline WebView)
    private static final String WEB_CHECKLIST_SCREEN       = "webChecklist.screen";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue"; // "Complete Workflow" – disabled until checklist done

    // Summary screen (native, scrollable)
    private static final String SUMMARY_SCREEN             = "summary.screen";
    private static final String SUMMARY_RESTART_BTN        = "summary.button.restart";       // "Start Again"

    // ── WebView HTML element ID (inside inline CHECKLIST_HTML) ────────────────
    private static final String WEB_CONFIRM_BTN            = "confirmButton";                // "Mark checklist as ready"

    // ══════════════════════════════════════════════════════════════════════════

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
        options.setCapability("appium:enforceAppInstall", true);
        options.setCapability("printPageSourceOnFindFailure", true);

        uninstallAndroidPackage(ANDROID_PACKAGE);

        String appPath = resolveAppPath("android");
        options.setApp(appPath);
        // SplashActivity transitions immediately to MainActivity; tell Appium to wait for MainActivity.
        options.setAppWaitActivity("io.specmatic.e2edemo.MainActivity");
        System.out.printf("[Android] Using app: %s%n", appPath);

        if (IS_NML && IS_EYES_ENABLED) {
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

    // ══════════════════════════════════════════════════════════════════════════
    // Test
    // ══════════════════════════════════════════════════════════════════════════

    @Test(description = "Community Meeting Planner – full workflow end-to-end")
    public void communityMeetingPlannerTest() {

        // ── Step 1: App launch ────────────────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("App Launch");

        // ── Step 2: Open Community Meeting Planner modal ──────────────────────
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(HOME_PLANNER_BTN)).click();

        // ── Step 3: Select original or alternate flow ─────────────────────────
        if (USE_ALTERNATE_FLOW) {
            Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(PLANNER_ALTERNATE_BTN)).click();
        } else {
            Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(PLANNER_ORIGINAL_BTN)).click();
        }

        // ── Step 4: Planner screen ────────────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(PLANNER_SCREEN));
        checkpoint("Planner Screen");

        // ── Step 5: Scroll to bottom → "Next: Load Guest Profiles" ───────────
        scrollIntoView(PLANNER_NEXT_BTN);
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(PLANNER_NEXT_BTN)).click();
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 10);

        // ── Step 6: Guest Lookup screen ───────────────────────────────────────
        checkpoint("Guest Lookup Screen");

        // ── Step 7: Enter 20 (out of range 1–15) → click "Load Profiles" ──────
        WebElement countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.click();
        countInput.clear();
        countInput.sendKeys("20");
        hideKeyboard();
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN)).click();
        checkpoint("Invalid Guest Count - Alert");

        // ── Step 8: Dismiss the validation alert ──────────────────────────────
        dismissValidationAlert();

        // ── Step 9: Enter 10 (valid) → click "Load Profiles" ─────────────────
        countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.click();
        countInput.clear();
        countInput.sendKeys("10");
        hideKeyboard();
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN)).click();

        // ── Step 10: Wait for API results (up to 30 s) ───────────────────────
        // guestLookup.section.results (SectionTitle) has accessibilityLabel and renders
        // only when guests.length > 0, making it a reliable load-complete signal.
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_RESULTS), 30);
        checkpoint("Guest Profiles Loaded");

        // ── Step 11: Scroll to bottom → "Next: Open Web Checklist" ───────────
        scrollIntoView(GUEST_LOOKUP_NEXT_BTN);
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_NEXT_BTN)).click();

        // ── Step 12: Web Checklist screen ─────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(WEB_CHECKLIST_SCREEN));
        checkpoint("Web Checklist Screen");

        // ── Step 13: Switch to WebView → click "Mark checklist as ready" ──────
        // The WebChecklist screen renders an inline HTML page (not a URL-based WebView).
        switchToWebViewContext();
        Wait.waitTillElementIsClickable(driver, AppiumBy.cssSelector("#" + WEB_CONFIRM_BTN)).click();
        checkpoint("Checklist Marked Ready");

        // ── Step 14: Switch back to native → click "Complete Workflow" ────────
        // The native button is enabled once the WebView posts 'checklist-complete'.
        switchToNativeContext();
        Wait.waitTillElementIsClickable(driver,
                AppiumBy.accessibilityId(WEB_CHECKLIST_CONTINUE_BTN)).click();

        // ── Step 15: Summary screen ───────────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(SUMMARY_SCREEN));
        checkpoint("Summary Screen");

        // ── Step 16: Scroll to bottom → "Start Again" ────────────────────────
        scrollIntoView(SUMMARY_RESTART_BTN);
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(SUMMARY_RESTART_BTN)).click();

        // ── Step 17: Back on Home screen ──────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Dismiss the soft keyboard via the AndroidDriver cast. */
    private void hideKeyboard() {
        ((AndroidDriver) driver).hideKeyboard();
    }

    /**
     * Scroll within the first scrollable view until the element with the given
     * content-desc is on screen, then click it.
     */
    private void scrollIntoView(String accessibilityId) {
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
                ".scrollIntoView(new UiSelector().description(\"" + accessibilityId + "\"))"));
    }

    private void dismissValidationAlert() {
        By okButton = AppiumBy.androidUIAutomator("new UiSelector().textMatches(\"(?i)ok\")");

        try {
            Wait.waitTillElementIsClickable(driver, okButton, 5).click();
            return;
        } catch (Exception ignored) {
            // Some Android builds render this as a native alert instead of an in-app dialog.
        }

        try {
            Wait.waitTillAlertIsPresent(driver).accept();
        } catch (Exception e) {
            throw new RuntimeException("Unable to dismiss guest count validation dialog", e);
        }
    }

    /** Switch Appium context to the first available WEBVIEW. */
    private void switchToWebViewContext() {
        Wait.waitFor(2); // allow WebView to finish loading
        if (!(driver instanceof SupportsContextSwitching)) return;
        SupportsContextSwitching ctx = (SupportsContextSwitching) driver;
        for (String c : ctx.getContextHandles()) {
            if (c.startsWith("WEBVIEW")) {
                ctx.context(c);
                System.out.printf("[Android] Switched to context: %s%n", c);
                return;
            }
        }
        System.out.println("[Android] No WEBVIEW context found – staying in NATIVE_APP");
    }

    /** Switch back to NATIVE_APP context. */
    private void switchToNativeContext() {
        if (driver instanceof SupportsContextSwitching) {
            ((SupportsContextSwitching) driver).context("NATIVE_APP");
            System.out.println("[Android] Switched back to NATIVE_APP context");
        }
    }
}
