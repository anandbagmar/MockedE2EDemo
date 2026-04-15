package io.specmatic.tests.ios;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.applitools.eyes.appium.Eyes;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.specmatic.tests.BaseTest;
import io.specmatic.utils.Wait;

/**
 * CommunityMeetingPlannerIOSTest
 *
 * Exercises the full Community Meeting Planner workflow on iOS (simulator):
 *   Home → flow-selector modal → Planner → GuestLookup (with validation)
 *     → WebChecklist (inline WebView) → Summary → back to Home
 *
 * Flow selection:
 *   USE_ALTERNATE_FLOW = false  →  Original flow
 *   USE_ALTERNATE_FLOW = true   →  Alternate flow
 *
 * Override at runtime:
 *   ./gradlew test -DUSE_ALTERNATE_FLOW=true -Pios
 *
 * Note: autoAcceptAlerts is intentionally NOT set so the test can take a
 * checkpoint while the validation alert is visible and dismiss it explicitly.
 */
public class CommunityMeetingPlannerIOSTest extends BaseTest {

    private static final String APP_NAME = "Community Meeting Planner (iOS)";

    private static final boolean USE_ALTERNATE_FLOW =
            "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
                    || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));

    private static final String IOS_DEVICE_NAME =
            System.getProperty("IOS_DEVICE_NAME", "iPhone 17 Pro");

    private static final String IOS_PLATFORM_VERSION =
            System.getProperty("IOS_PLATFORM_VERSION", "26.4");

    // ── Native layer locators ─────────────────────────────────────────────────
    // On iOS, testID → accessibilityIdentifier, so AppiumBy.accessibilityId()
    // matches testID values directly for all elements.

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
    private static final String GUEST_LOOKUP_RESULTS       = "guestLookup.section.results";  // SectionTitle – renders only after API load
    private static final String GUEST_LOOKUP_NEXT_BTN      = "guestLookup.button.next";      // "Next: Open Web Checklist"

    // Web Checklist screen (native shell + inline WebView)
    private static final String WEB_CHECKLIST_SCREEN       = "webChecklist.screen";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue"; // "Complete Workflow" – disabled until checklist done

    // Summary screen (native, scrollable)
    private static final String SUMMARY_SCREEN             = "summary.screen";
    private static final String SUMMARY_RESTART_BTN        = "summary.button.restart";       // "Start Again"

    // ── WebView HTML element ID (inside inline CHECKLIST_HTML) ────────────────
    private static final String WEB_CONFIRM_BTN            = "Mark checklist as ready";      // WebView button label exposed by XCUITest

    // ── Temp dir for extracted .app ───────────────────────────────────────────
    private Path extractedAppDir;

    // ══════════════════════════════════════════════════════════════════════════

    @BeforeMethod
    public void setUp(Method testInfo) throws IOException {
        System.out.printf("[iOS] Setting up test: %s  alternateFlow=%b  nml=%b%n",
                testInfo.getName(), USE_ALTERNATE_FLOW, IS_NML);

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
        options.setFullReset(true);
        options.setCapability("printPageSourceOnFindFailure", true);
        // autoAcceptAlerts intentionally omitted – the test handles the validation
        // alert manually so a checkpoint can be taken while it is visible.

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

    // ══════════════════════════════════════════════════════════════════════════
    // Test
    // ══════════════════════════════════════════════════════════════════════════

    @Test(description = "Community Meeting Planner – full workflow end-to-end (iOS)")
    public void communityMeetingPlannerTest() {

        // ── Step 1: App launch ────────────────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("App Launch");

        // ── Step 2: Open Community Meeting Planner modal ──────────────────────
        scrollToAndClick(HOME_PLANNER_BTN);

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
        scrollToAndClick(PLANNER_NEXT_BTN);

        // ── Step 6: Guest Lookup screen ───────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN));
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
        driver.switchTo().alert().accept();

        // ── Step 9: Enter 10 (valid) → click "Load Profiles" ─────────────────
        countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.click();
        countInput.clear();
        countInput.sendKeys("10");
        hideKeyboard();
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN)).click();

        // ── Step 10: Wait for API results (up to 30 s) ───────────────────────
        // XCUITest can keep section markers "not visible" even after they are
        // present, so use the next-step button as the load-complete signal.
        Wait.waitTillElementExists(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_NEXT_BTN), 30);
        checkpoint("Guest Profiles Loaded");

        // ── Step 11: Scroll to bottom → "Next: Open Web Checklist" ───────────
        scrollToAndClick(GUEST_LOOKUP_NEXT_BTN);

        // ── Step 12: Web Checklist screen ─────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(WEB_CHECKLIST_SCREEN));
        checkpoint("Web Checklist Screen");

        // ── Step 13: Switch to WebView → click "Mark checklist as ready" ──────
        // The WebChecklist screen renders an inline HTML page (not a URL-based WebView).
        switchToWebViewContext();
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(WEB_CONFIRM_BTN)).click();
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
        scrollToAndClick(SUMMARY_RESTART_BTN);

        // ── Step 17: Back on Home screen ──────────────────────────────────────
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Scroll the first scrollable container to the bottom so that elements
     * rendered at the end of a list become visible (XCUITest marks off-screen
     * elements as visible="false", preventing waitTillElementIsPresent from
     * matching them even when they exist in the hierarchy).
     */
    private void scrollToBottom() {
        Map<String, Object> args = new HashMap<>();
        args.put("direction", "down");
        driver.executeScript("mobile: scroll", args);
    }

    /**
     * Scroll within the nearest scrollable container using repeated XCUITest
     * mobile:scroll gestures until the element matching the given accessibility
     * identifier (= testID) becomes clickable, then click it.
     */
    private void scrollToAndClick(String accessibilityId) {
        By locator = AppiumBy.accessibilityId(accessibilityId);
        TimeoutException lastFailure = null;

        for (int attempt = 0; attempt < 8; attempt++) {
            try {
                Wait.waitTillElementIsClickable(driver, locator, 2).click();
                return;
            } catch (TimeoutException e) {
                lastFailure = e;
            }

            Map<String, Object> args = new HashMap<>();
            args.put("direction", "down");
            driver.executeScript("mobile: scroll", args);
        }

        throw new RuntimeException(
                "Unable to scroll to element: " + accessibilityId,
                lastFailure
        );
    }

    /**
     * Dismiss the soft keyboard on iOS by tapping a neutral area above the input
     * panel. React Native's TextInput doesn't expose a keyboard "Done" button that
     * WDA can find via the standard hideKeyboard command, so a coordinate tap is
     * used instead.
     */
    private void hideKeyboard() {
        Map<String, Object> args = new HashMap<>();
        args.put("x", 201);
        args.put("y", 200); // safe static-text area above the input panel
        driver.executeScript("mobile: tap", args);
    }

    /** Switch Appium context to the first available WEBVIEW. */
    private void switchToWebViewContext() {
        Wait.waitFor(2); // allow WebView to finish loading
        if (!(driver instanceof SupportsContextSwitching)) return;
        SupportsContextSwitching ctx = (SupportsContextSwitching) driver;
        for (String c : ctx.getContextHandles()) {
            if (c.startsWith("WEBVIEW")) {
                ctx.context(c);
                System.out.printf("[iOS] Switched to context: %s%n", c);
                return;
            }
        }
        System.out.println("[iOS] No WEBVIEW context found – staying in NATIVE_APP");
    }

    /** Switch back to NATIVE_APP context. */
    private void switchToNativeContext() {
        if (driver instanceof SupportsContextSwitching) {
            ((SupportsContextSwitching) driver).context("NATIVE_APP");
            System.out.println("[iOS] Switched back to NATIVE_APP context");
        }
    }

    // ── .app.zip extraction helpers ───────────────────────────────────────────

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
