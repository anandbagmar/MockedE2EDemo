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

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;

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
 *   Home → flow-selector modal → Planner → NativeJourney → NativeHybrid
 *     → GuestLookup (with validation) → WebChecklist (inline WebView)
 *     → Summary → back to Home
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

    private static final String APP_NAME = "Community Meeting Planner -iOS";
    private static final String IOS_BUNDLE_ID = "io.specmatic.e2edemo";

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
    private static final String PLANNER_MODE_PANEL       = "planner.mode.panel";
    private static final String PLANNER_ORIGINAL_BTN       = "planner.mode.button.original";
    private static final String PLANNER_ALTERNATE_BTN      = "planner.mode.button.alternate";

    // Planner screen (native, scrollable)
    private static final String PLANNER_SCREEN             = "planner.screen";
    private static final String PLANNER_NEXT_NATIVE_BTN    = "planner.button.next.native";   // "Next: Native Detail"

    // Guest Lookup screen (native, scrollable)
    private static final String GUEST_LOOKUP_SCREEN        = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT         = "guestLookup.input.count";      // TextInput – has accessibilityLabel
    private static final String GUEST_LOOKUP_FETCH_BTN     = "guestLookup.button.fetch";     // "Load Profiles"
    private static final String GUEST_LOOKUP_FETCH_LABEL   = "Load Profiles";
    private static final String GUEST_LOOKUP_LOADER        = "guestLookup.loader";           // Loading indicator shown while fetching profiles
    private static final String GUEST_LOOKUP_RESULTS       = "guestLookup.results";          // Results container rendered after API load
    private static final String GUEST_LOOKUP_RESULTS_TITLE = "guestLookup.section.results";  // SectionTitle inside the results container
    private static final String GUEST_LOOKUP_CARDS         = "guestLookup.cards";           // Wrapper around all guest cards
    private static final String GUEST_LOOKUP_CARDS_LIST    = "guestLookup.cards.list";      // Dedicated list wrapper for Eyes regioning
    private static final String GUEST_LOOKUP_ALERT_CARD    = "guestLookup.alert.card";
    private static final String GUEST_LOOKUP_ALERT_OK      = "guestLookup.alert.ok";
    private static final String GUEST_LOOKUP_ALERT_BACKDROP = "guestLookup.alert.backdrop";
    private static final String GUEST_LOOKUP_NEXT_BTN      = "guestLookup.button.next";      // "Next: Open Web Checklist"

    // Web Checklist screen (native shell + inline WebView)
    private static final String WEB_CHECKLIST_SCREEN       = "webChecklist.screen";
    private static final String WEB_CHECKLIST_WEBVIEW      = "webChecklist.webview";
    private static final String WEB_CHECKLIST_READY        = "webChecklist.ready";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue"; // "Complete Workflow" – disabled until checklist done

    // Native interlude screen
    private static final String NATIVE_JOURNEY_SCREEN      = "nativeJourney.screen";
    private static final String NATIVE_JOURNEY_MODE_NOTE   = "nativeJourney.mode.native.swift";
    private static final String NATIVE_JOURNEY_VIEW        = "nativeJourney.nativeView";
    private static final String NATIVE_JOURNEY_CONTINUE_BTN = "nativeJourney.button.continue";

    // Native hybrid screen
    private static final String NATIVE_HYBRID_SCREEN       = "nativeHybrid.screen";
    private static final String NATIVE_HYBRID_MODE_NOTE    = "nativeHybrid.mode.hybrid.swift";
    private static final String NATIVE_HYBRID_VIEW         = "nativeHybrid.nativeView";
    private static final String NATIVE_HYBRID_CONTINUE_BTN = "nativeHybrid.button.continue";

    // Summary screen (native, scrollable)
    private static final String SUMMARY_SCREEN             = "summary.screen";
    private static final String SUMMARY_RESTART_BTN        = "summary.button.restart";       // "Start Again"

    // ── WebView HTML element ID (inside inline CHECKLIST_HTML) ────────────────
    private static final String WEB_CONFIRM_BUTTON_ID      = "confirmButton";
    private static final String WEB_CONFIRM_BTN            = "Mark checklist as ready";      // Button label shown in the web content

    // ── Temp dir for extracted .app ───────────────────────────────────────────
    private Path extractedAppDir;

    // ══════════════════════════════════════════════════════════════════════════

    @BeforeMethod
    public void setUp(Method testInfo) throws IOException {
        logStep("iOS", "Preparing test " + testInfo.getName());
        System.out.printf("[iOS] Setting up test: %s  alternateFlow=%b  nml=%b%n",
                testInfo.getName(), USE_ALTERNATE_FLOW, IS_NML);

        uninstallIosSimulatorApp(IOS_BUNDLE_ID);

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
        options.setNewCommandTimeout(Duration.ofSeconds(30));
        options.setCapability("printPageSourceOnFindFailure", true);
        options.setCapability("appium:webviewConnectTimeout", 20000);
        options.setCapability("appium:webviewConnectRetries", 40);
        options.setCapability("appium:startIWDP", true);
        options.setCapability("appium:fullContextList", true);
        options.setCapability("appium:additionalWebviewBundleIds", "[\"*\"]");
        // autoAcceptAlerts intentionally omitted – the test handles the validation
        // alert manually so a checkpoint can be taken while it is visible.

        if (IS_NML && IS_EYES_ENABLED) {
            Eyes.setMobileCapabilities(options, APPLITOOLS_API_KEY);
        }

        try {
            driver = new IOSDriver(new URL(APPIUM_SERVER_URL), options);
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + APPIUM_SERVER_URL, e);
        }

        logStep("iOS", "Configuring Eyes");
        configureEyes(APP_NAME, testInfo);
        logStep("iOS", "Session ready");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Test
    // ══════════════════════════════════════════════════════════════════════════

    @Test(description = "Community Meeting Planner – full workflow end-to-end (iOS)")
    public void communityMeetingPlannerTest() {
        handleHomeScreen();
        handlePlannerScreen();
        handleNativeJourneyScreen();
        handleNativeHybridScreen();
        handleGuestLookupScreen();
        handleWebChecklistScreen();
        handleSummaryScreen();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void handleHomeScreen() {
        logStep("iOS", "Step 1 - wait for home screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpointNative("App Launch");

        logStep("iOS", "Step 2 - open planner modal");
        tapButtonAndWaitForScreen(HOME_PLANNER_BTN, PLANNER_MODE_PANEL);

        logStep("iOS", "Step 3 - select flow variant");
        if (USE_ALTERNATE_FLOW) {
            tapButtonAndWaitForScreen(PLANNER_ALTERNATE_BTN, PLANNER_SCREEN);
        } else {
            tapButtonAndWaitForScreen(PLANNER_ORIGINAL_BTN, PLANNER_SCREEN);
        }
    }

    private void handlePlannerScreen() {
        logStep("iOS", "Step 4 - verify planner screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(PLANNER_SCREEN));
        checkpointNative("Planner Screen");
    }

    private void handleNativeJourneyScreen() {
        logStep("iOS", "Step 5 - open Swift native detail screen");
        tapButtonAndWaitForScreen(PLANNER_NEXT_NATIVE_BTN, NATIVE_JOURNEY_SCREEN);

        logStep("iOS", "Step 6 - verify Swift native detail screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_JOURNEY_SCREEN), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_JOURNEY_MODE_NOTE), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_JOURNEY_VIEW), 30);
        checkpointNative("Swift Native Screen");

        logStep("iOS", "Step 7 - continue to hybrid native view screen");
        tapButtonAndWaitForScreen(NATIVE_JOURNEY_CONTINUE_BTN, NATIVE_HYBRID_SCREEN);
    }

    private void handleNativeHybridScreen() {
        logStep("iOS", "Step 8 - verify Swift hybrid native view screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_HYBRID_SCREEN), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_HYBRID_MODE_NOTE), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_HYBRID_VIEW), 30);
        checkpointNative("Swift Hybrid Screen");

        logStep("iOS", "Step 9 - continue from hybrid native view");
        tapButtonAndWaitForScreen(NATIVE_HYBRID_CONTINUE_BTN, GUEST_LOOKUP_SCREEN);
    }

    private void handleGuestLookupScreen() {
        logStep("iOS", "Step 10 - wait for guest lookup screen");
        waitForGuestLookupScreen();
        checkpointNative("Guest Lookup Screen");

        logStep("iOS", "Step 11 - validate out-of-range guest count");
        scrollToAndClick(GUEST_LOOKUP_INPUT);
        WebElement countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.clear();
        countInput.sendKeys("20");
        tapGuestLookupFetchButton();
        checkpointNative("Invalid Guest Count - Alert");

        logStep("iOS", "Step 12 - dismiss validation alert");
        dismissValidationAlert();

        logStep("iOS", "Step 13 - load guest profiles");
        scrollToAndClick(GUEST_LOOKUP_INPUT);
        countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.clear();
        countInput.sendKeys("10");
        tapGuestLookupFetchButton();

        logStep("iOS", "Step 14 - wait for guest results");
        waitForGuestLookupResults();
        checkpointWithMultipleMatchLevels(
                "Guest Profiles - Strict & Layout Content",
                Target.window().layout(AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST)).strict());

        logStep("iOS", "Step 15 - open web checklist");
        tapButtonAndWaitForScreen(GUEST_LOOKUP_NEXT_BTN, WEB_CHECKLIST_SCREEN);
    }

    private void handleWebChecklistScreen() {
        logStep("iOS", "Step 16 - wait for web checklist screen");
        waitForWebChecklistScreen();
        checkpointWebView("Web Checklist Screen");

        logStep("iOS", "Step 17 - complete web checklist");
        tapWebChecklistConfirmButton();
        checkpointWebView("Checklist Marked Ready");

        logStep("iOS", "Step 18 - complete workflow");
        switchToNativeContext();
        tapButtonAndWaitForScreen(WEB_CHECKLIST_CONTINUE_BTN, SUMMARY_SCREEN);
    }

    private void handleSummaryScreen() {
        logStep("iOS", "Step 19 - verify summary screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(SUMMARY_SCREEN));
        checkpointNative("Summary Screen");

        logStep("iOS", "Step 20 - restart flow");
        tapButtonAndWaitForScreen(SUMMARY_RESTART_BTN, HOME_SCREEN);

        logStep("iOS", "Step 21 - return home");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpointNative("Home Screen");
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
                Wait.waitTillElementIsClickable(driver, locator, 3).click();
                return;
            } catch (TimeoutException e) {
                lastFailure = e;
            }

            Map<String, Object> args = new HashMap<>();
            args.put("direction", "down");
            driver.executeScript("mobile: scroll", args);
            Wait.waitFor(1);
        }

        throw new RuntimeException(
                "Unable to scroll to element: " + accessibilityId,
                lastFailure
        );
    }

    /**
     * Wait for Guest Lookup to fully settle after navigation from Planner.
     * The screen root can appear before the input and button are actually ready,
     * so we wait for those controls as stronger readiness signals.
     */
    private void waitForGuestLookupScreen() {
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 20);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
    }

    /**
     * Tap the fetch button in the top action row.
     * We intentionally do not dismiss the keyboard here; the iOS flow is more
     * stable when we go straight from typing to tapping the action button.
     */
    private void tapGuestLookupFetchButton() {
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN), 10).click();
    }

    /**
     * Wait for Guest Lookup to actually finish loading profiles.
     * If the spinner appears, wait for it to clear before taking the result
     * checkpoint. This is more reliable than waiting on cards alone.
     */
    private void waitForGuestLookupResults() {
        By loaderLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_LOADER);
        By resultsContainerLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_RESULTS);
        By resultsTitleLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_RESULTS_TITLE);
        By cardsLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS);
        By cardsListLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST);

        try {
            Wait.waitTillElementIsPresent(driver, loaderLocator, 5);
        } catch (TimeoutException ignored) {
            // Fast responses may skip the spinner entirely.
        }

        Wait.waitTillElementExists(driver, resultsContainerLocator, 30);
        try {
            Wait.waitTillElementIsPresent(driver, resultsTitleLocator, 5);
        } catch (TimeoutException ignored) {
            // The section title can be below the fold on iOS; the container is the stronger readiness signal.
        }
        Wait.waitTillElementExists(driver, cardsLocator, 30);
        Wait.waitTillElementExists(driver, cardsListLocator, 30);
        Wait.waitTillElementDisappears(driver, loaderLocator);
    }

    /**
     * Tap a visible iOS CTA and wait for the next screen to become available.
     * This avoids continuing before the transition has actually completed.
     */
    private void tapButtonAndWaitForScreen(String accessibilityId, String nextScreenId) {
        By locator = AppiumBy.accessibilityId(accessibilityId);
        By nextScreenLocator = AppiumBy.accessibilityId(nextScreenId);

        TimeoutException lastFailure = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Wait.waitTillElementIsClickable(driver, locator, 10).click();
                Wait.waitTillElementIsPresent(driver, nextScreenLocator, 10);
                return;
            } catch (TimeoutException e) {
                lastFailure = e;
                Wait.waitFor(1);
            }
        }

        throw new RuntimeException(
                "Unable to tap button and reach next screen: " + accessibilityId + " -> " + nextScreenId,
                lastFailure
        );
    }

    /**
     * Wait for the Web Checklist screen to actually mount its webview.
     * The native screen root can appear before the inline WebView is ready.
     */
    private void waitForWebChecklistScreen() {
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(WEB_CHECKLIST_SCREEN), 20);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(WEB_CHECKLIST_READY), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(WEB_CHECKLIST_WEBVIEW), 20);
        waitForWebChecklistContent();
    }

    /**
     * Wait for the checklist button inside the WebView to be ready.
     * On iOS this gives us a concrete signal that the final screen content
     * has actually rendered, not just the native wrapper.
     */
    private void waitForWebChecklistContent() {
        try {
            switchToNativeContext();
            Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(WEB_CONFIRM_BTN), 20);
            return;
        } catch (Exception ignored) {
            // Some iOS runs only surface the final checklist button through the
            // native wrapper after the webview is mounted.
        }

        Wait.waitFor(2);
    }

    /**
     * Click the checklist button regardless of whether iOS exposes it through
     * the webview DOM or the native accessibility tree.
     */
    private void tapWebChecklistConfirmButton() {
        try {
            Wait.waitTillElementIsClickable(driver, AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 10).click();
            return;
        } catch (Exception ignored) {
            // Some iOS runs never surface a WEBVIEW context even though the
            // content is visible, so fall back to the native accessibility tree.
        }

        try {
            switchToNativeContext();
            Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(WEB_CONFIRM_BTN), 10).click();
            return;
        } catch (Exception ignored) {
            // Fall through to the coordinate tap.
        }

        Map<String, Object> args = new HashMap<>();
        args.put("x", 187);
        args.put("y", 560);
        driver.executeScript("mobile: tap", args);
    }

    /**
     * Dismiss the guest-count validation alert.
     * First try the alert's OK action, then fall back to tapping the modal
     * backdrop directly. This keeps the tap inside the alert layer instead of
     * risking an interaction with the screen underneath.
     */
    private void dismissValidationAlert() {
        By alertCardLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_CARD);
        By okButtonLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_OK);
        By backdropLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_BACKDROP);

        Wait.waitTillElementIsPresent(driver, alertCardLocator, 5);

        try {
            Wait.waitTillElementIsClickable(driver, okButtonLocator, 5).click();
            Wait.waitTillElementDisappears(driver, alertCardLocator);
            Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 5);
            return;
        } catch (Exception ignored) {
            // The OK button may not be hittable in some simulator states.
        }

        try {
            Wait.waitTillElementIsClickable(driver, backdropLocator, 5).click();
            Wait.waitTillElementDisappears(driver, alertCardLocator);
            Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 5);
            return;
        } catch (Exception ignored) {
            // Fall through to the final error if neither route works.
        }

        throw new RuntimeException("Unable to dismiss guest count validation alert");
    }

    /**
     * Dismiss the soft keyboard on iOS by tapping a neutral area above the input
     * panel. React Native's TextInput doesn't expose a keyboard "Done" button that
     * WDA can find via the standard hideKeyboard command, so a coordinate tap is
     * used instead.
     */
    private void hideKeyboard() {
        try {
            ((IOSDriver) driver).hideKeyboard();
            return;
        } catch (Exception ignored) {
            // Fall back to a neutral tap when WDA cannot dismiss the keyboard directly.
        }

        try {
            Map<String, Object> args = new HashMap<>();
            args.put("x", 20);
            args.put("y", 80);
            driver.executeScript("mobile: tap", args);
        } catch (Exception ignored) {
            // Keyboard may already be gone.
        }
    }

    /** Switch Appium context to the first available WEBVIEW. */
    private boolean switchToWebViewContext() {
        if (!(driver instanceof SupportsContextSwitching)) return false;
        SupportsContextSwitching ctx = (SupportsContextSwitching) driver;
        for (int attempt = 0; attempt < 10; attempt++) {
            for (Object contextHandle : ctx.getContextHandles()) {
                String contextName = contextHandleName(contextHandle);
                if (contextName != null && contextName.startsWith("WEBVIEW")) {
                    ctx.context(contextName);
                    System.out.printf("[iOS] Switched to context: %s%n", contextName);
                    return true;
                }
            }
            Wait.waitFor(1);
        }
        return false;
    }

    private String contextHandleName(Object contextHandle) {
        if (contextHandle instanceof String) {
            return (String) contextHandle;
        }

        if (contextHandle instanceof Map<?, ?>) {
            Map<?, ?> contextMap = (Map<?, ?>) contextHandle;
            Object name = contextMap.get("name");
            if (name instanceof String) {
                return (String) name;
            }

            Object id = contextMap.get("id");
            if (id instanceof String) {
                return (String) id;
            }
        }

        return null;
    }

    /** Switch back to NATIVE_APP context. */
    private void switchToNativeContext() {
        if (driver instanceof SupportsContextSwitching) {
            ((SupportsContextSwitching) driver).context("NATIVE_APP");
            System.out.println("[iOS] Switched back to NATIVE_APP context");
        }
    }

    /**
     * Capture an Eyes checkpoint while staying in the native app context.
     */
    private void checkpointNative(String tag) {
        switchToNativeContext();
        eyes.checkWindow(tag);
    }

    /**
     * Capture an Eyes checkpoint after switching into the webview context.
     */
    private void checkpointWebView(String tag) {
        if (!switchToWebViewContext()) {
            System.out.printf("[iOS] WEBVIEW context not exposed; taking native checkpoint for %s%n", tag);
        }
        eyes.checkWindow(tag);
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
