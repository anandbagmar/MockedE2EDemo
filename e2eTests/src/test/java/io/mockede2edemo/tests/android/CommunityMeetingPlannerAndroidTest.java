package io.mockede2edemo.tests.android;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.mockede2edemo.tests.Wait;
import io.specmatic.tests.BaseTest;

/**
 * CommunityMeetingPlannerAndroidTest
 *
 * Exercises the full Community Meeting Planner workflow on Android:
 * Home → flow-selector modal → Planner → NativeJourney → NativeHybrid
 * → GuestLookup (with validation) → WebChecklist (inline WebView)
 * → Summary → back to Home
 *
 * Flow selection:
 * USE_ALTERNATE_FLOW = false → Original flow
 * USE_ALTERNATE_FLOW = true → Alternate flow
 *
 * Override at runtime:
 * ./gradlew test -DUSE_ALTERNATE_FLOW=true -Pandroid
 */
public class CommunityMeetingPlannerAndroidTest extends BaseTest {

    private static final String APP_NAME = "Community Meeting Planner - Android";
    private static final String ANDROID_PACKAGE = "io.specmatic.e2edemo";

    private static final boolean USE_ALTERNATE_FLOW = "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
            || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));

    // ── Native layer locators ─────────────────────────────────────────────────
    // All SafeAreaView screens have accessibilityLabel (= testID) after App.tsx
    // fix.
    // All PrimaryButton / SecondaryButton elements always have accessibilityLabel =
    // testID.
    // → AppiumBy.accessibilityId() matches content-desc on Android for all of
    // these.

    private static final String HOME_SCREEN = "home.screen";
    private static final String HOME_PLANNER_BTN = "home.button.planner";

    // Flow-selector modal
    private static final String PLANNER_MODE_PANEL = "planner.mode.panel";
    private static final String PLANNER_ORIGINAL_BTN = "planner.mode.button.original";
    private static final String PLANNER_ALTERNATE_BTN = "planner.mode.button.alternate";

    // Planner screen (native, scrollable)
    private static final String PLANNER_SCREEN = "planner.screen";
    private static final String PLANNER_NEXT_NATIVE_BTN = "planner.button.next.native"; // "Next: Native Detail"

    // Guest Lookup screen (native, scrollable)
    private static final String GUEST_LOOKUP_SCREEN = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT = "guestLookup.input.count"; // TextInput – has accessibilityLabel
    private static final String GUEST_LOOKUP_FETCH_BTN = "guestLookup.button.fetch"; // "Load Profiles"
    private static final String GUEST_LOOKUP_FETCH_LABEL = "Load Profiles";
    private static final String GUEST_LOOKUP_RESULTS = "guestLookup.section.results"; // SectionTitle – has
                                                                                      // accessibilityLabel; appears
                                                                                      // only after API load
    private static final String GUEST_LOOKUP_CARDS = "guestLookup.cards"; // Wrapper around all guest cards
    private static final String GUEST_LOOKUP_CARDS_LIST = "guestLookup.cards.list"; // Dedicated list wrapper for Eyes
                                                                                    // regioning
    private static final String GUEST_LOOKUP_ALERT_CARD = "guestLookup.alert.card";
    private static final String GUEST_LOOKUP_ALERT_OK = "guestLookup.alert.ok";
    private static final String GUEST_LOOKUP_ALERT_BACKDROP = "guestLookup.alert.backdrop";
    private static final String GUEST_LOOKUP_NEXT_BTN = "guestLookup.button.next"; // "Next: Open Web Checklist"

    // Web Checklist screen (native shell + inline WebView)
    private static final String WEB_CHECKLIST_SCREEN = "webChecklist.screen";
    private static final String WEB_CHECKLIST_WEBVIEW = "webChecklist.webview";
    private static final String WEB_CHECKLIST_READY = "webChecklist.ready";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue"; // "Complete Workflow" –
                                                                                             // disabled until checklist
                                                                                             // done

    // Native interlude screen
    private static final String NATIVE_JOURNEY_SCREEN = "nativeJourney.screen";
    private static final String NATIVE_JOURNEY_MODE_NOTE = "nativeJourney.mode.native.androidx";
    private static final String NATIVE_JOURNEY_VIEW = "nativeJourney.nativeView";
    private static final String NATIVE_JOURNEY_CONTINUE_BTN = "nativeJourney.button.continue";

    // Native hybrid screen
    private static final String NATIVE_HYBRID_SCREEN = "nativeHybrid.screen";
    private static final String NATIVE_HYBRID_MODE_NOTE = "nativeHybrid.mode.hybrid.androidx";
    private static final String NATIVE_HYBRID_VIEW = "nativeHybrid.nativeView";
    private static final String NATIVE_HYBRID_CONTINUE_BTN = "nativeHybrid.button.continue";

    // Summary screen (native, scrollable)
    private static final String SUMMARY_SCREEN = "summary.screen";
    private static final String SUMMARY_RESTART_BTN = "summary.button.restart"; // "Start Again"

    // ── WebView HTML element ID (inside inline CHECKLIST_HTML) ────────────────
    private static final String WEB_CONFIRM_BTN = "confirmButton"; // "Mark checklist as ready"

    // ══════════════════════════════════════════════════════════════════════════

    @BeforeMethod
    public void setUp(Method testInfo) {
        logStep("Android", "Preparing test " + testInfo.getName());
        System.out.printf("[Android] Setting up test: %s  alternateFlow=%b  nml=%b%n",
                testInfo.getName(), USE_ALTERNATE_FLOW, IS_NML);

        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName("Android");
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(30));
        options.setCapability("appium:enforceAppInstall", true);
        options.setCapability("printPageSourceOnFindFailure", true);

        uninstallAndroidPackage(ANDROID_PACKAGE);

        String appPath = resolveAppPath("android");
        options.setApp(appPath);
        // SplashActivity transitions immediately to MainActivity; tell Appium to wait
        // for MainActivity.
        options.setAppWaitActivity("io.specmatic.e2edemo.MainActivity");
        System.out.printf("[Android] Using app: %s%n", appPath);

        if (IS_NML && IS_EYES_ENABLED) {
            Eyes.setMobileCapabilities(options, APPLITOOLS_API_KEY);
        }

        try {
            driver = new AndroidDriver(new URL(APPIUM_SERVER_URL), options);
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + APPIUM_SERVER_URL, e);
        }

        logStep("Android", "Configuring Eyes");
        configureEyes(APP_NAME, testInfo);
        logStep("Android", "Session ready");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Test
    // ══════════════════════════════════════════════════════════════════════════

    @Test(description = "Community Meeting Planner – full workflow end-to-end")
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
        logStep("Android", "Step 1 - wait for home screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("App Launch");

        logStep("Android", "Step 2 - open planner modal");
        Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(HOME_PLANNER_BTN), 5).click();

        logStep("Android", "Step 3 - select flow variant");
        if (USE_ALTERNATE_FLOW) {
            Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(PLANNER_ALTERNATE_BTN), 5).click();
        } else {
            Wait.waitTillElementIsClickable(driver, AppiumBy.accessibilityId(PLANNER_ORIGINAL_BTN), 5).click();
        }
        Wait.waitFor(1);
    }

    private void handlePlannerScreen() {
        logStep("Android", "Step 4 - verify planner screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId("planner.section.intro"), 20);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(PLANNER_NEXT_NATIVE_BTN), 20);
        checkpoint("Planner Screen");
    }

    private void handleNativeJourneyScreen() {
        logStep("Android", "Step 5 - open AndroidX native detail screen");
        tapButtonAndWaitForScreen(PLANNER_NEXT_NATIVE_BTN, NATIVE_JOURNEY_SCREEN);

        logStep("Android", "Step 6 - verify AndroidX native detail screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_JOURNEY_SCREEN), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_JOURNEY_MODE_NOTE), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_JOURNEY_VIEW), 30);
        checkpoint("AndroidX Native Screen");

        logStep("Android", "Step 7 - continue to hybrid native view screen");
        tapButtonAndWaitForScreen(NATIVE_JOURNEY_CONTINUE_BTN, NATIVE_HYBRID_SCREEN);
    }

    private void handleNativeHybridScreen() {
        logStep("Android", "Step 8 - verify AndroidX hybrid native view screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_HYBRID_SCREEN), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_HYBRID_MODE_NOTE), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(NATIVE_HYBRID_VIEW), 30);
        checkpoint("AndroidX Hybrid Screen");

        logStep("Android", "Step 9 - continue from hybrid native view");
        tapButtonAndWaitForScreen(NATIVE_HYBRID_CONTINUE_BTN, GUEST_LOOKUP_SCREEN);
        waitForGuestLookupScreen();
    }

    private void handleGuestLookupScreen() {
        logStep("Android", "Step 10 - verify guest lookup screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 20);
        checkpoint("Guest Lookup Screen");

        logStep("Android", "Step 11 - validate out-of-range guest count");
        scrollIntoView(GUEST_LOOKUP_INPUT);
        WebElement countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.click();
        countInput.clear();
        countInput.sendKeys("20");
        hideKeyboard();
        tapGuestLookupFetchButton();
        checkpoint("Invalid Guest Count - Alert");

        logStep("Android", "Step 12 - dismiss validation alert");
        dismissValidationAlert();

        logStep("Android", "Step 13 - load guest profiles");
        scrollIntoView(GUEST_LOOKUP_INPUT);
        countInput = Wait.waitTillElementIsPresent(driver,
                AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        countInput.click();
        countInput.clear();
        countInput.sendKeys("10");
        hideKeyboard();
        tapGuestLookupFetchButton();

        logStep("Android", "Step 14 - wait for guest results");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS), 30);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST), 30);
        checkpointWithMultipleMatchLevels(
                "Guest Profiles - Strict & Layout Content",
                Target.window().layout(AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST)).strict());

        logStep("Android", "Step 15 - open web checklist");
        tapButtonAndWaitForScreen(GUEST_LOOKUP_NEXT_BTN, WEB_CHECKLIST_SCREEN);
    }

    private void handleWebChecklistScreen() {
        logStep("Android", "Step 16 - wait for web checklist screen");
        waitForWebChecklistScreen();
        checkpoint("Web Checklist Screen");

        logStep("Android", "Step 17 - complete web checklist");
        switchToWebViewContext();
        Wait.waitTillElementIsClickable(driver, AppiumBy.cssSelector("#" + WEB_CONFIRM_BTN)).click();
        checkpoint("Checklist Marked Ready");

        logStep("Android", "Step 18 - complete workflow");
        switchToNativeContext();
        tapButtonAndWaitForScreen(WEB_CHECKLIST_CONTINUE_BTN, SUMMARY_SCREEN);
    }

    private void handleSummaryScreen() {
        logStep("Android", "Step 19 - verify summary screen");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(SUMMARY_SCREEN));
        checkpoint("Summary Screen");

        logStep("Android", "Step 20 - restart flow");
        tapButtonAndWaitForScreen(SUMMARY_RESTART_BTN, HOME_SCREEN);

        logStep("Android", "Step 21 - return home");
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(HOME_SCREEN));
        checkpoint("Home Screen");
    }

    /** Dismiss the soft keyboard via the AndroidDriver cast. */
    private void hideKeyboard() {
        ((AndroidDriver) driver).hideKeyboard();
    }

    /**
     * Scroll within the first scrollable view until the element with the given
     * content-desc is on screen, then click it.
     */
    private void scrollIntoView(String accessibilityId) {
        By locator = AppiumBy.accessibilityId(accessibilityId);
        RuntimeException lastFailure = null;

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
                                ".scrollIntoView(new UiSelector().description(\"" + accessibilityId + "\"))"));
                Wait.waitTillElementIsClickable(driver, locator, 5);
                return;
            } catch (RuntimeException e) {
                lastFailure = e;
                Wait.waitFor(1);
            }
        }

        throw new RuntimeException("Unable to scroll to element: " + accessibilityId, lastFailure);
    }

    /**
     * Tap a top-of-screen CTA and verify that the next screen appears.
     */
    private void tapButtonAndWaitForScreen(String buttonId, String nextScreenId) {
        By buttonLocator = AppiumBy.accessibilityId(buttonId);
        By nextScreenLocator = AppiumBy.accessibilityId(nextScreenId);

        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Wait.waitTillElementIsClickable(driver, buttonLocator, 10).click();
                Wait.waitTillElementIsPresent(driver, nextScreenLocator, 8);
                return;
            } catch (RuntimeException e) {
                lastFailure = e;
                Wait.waitFor(1);
            }
        }

        throw new RuntimeException(
                "Unable to tap button and reach next screen: " + buttonId + " -> " + nextScreenId,
                lastFailure);
    }

    /**
     * Wait for Guest Lookup to fully settle after navigating from Planner.
     * The screen root can appear before the controls are actually usable, so
     * wait for the input and fetch button as stronger readiness signals.
     */
    private void waitForGuestLookupScreen() {
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 20);
        Wait.waitTillElementIsPresent(driver, AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
        Wait.waitTillElementIsPresent(driver, findGuestLookupFetchButtonLocator(), 20);
    }

    /**
     * The fetch button is already on the Guest Lookup screen; it does not need
     * list scrolling. Waiting for it to be clickable and tapping directly is
     * much less fragile than trying to scroll to it while the screen is still
     * transitioning or the keyboard is dismissing.
     */
    private void tapGuestLookupFetchButton() {
        Wait.waitTillElementIsClickable(driver, findGuestLookupFetchButtonLocator(), 20).click();
    }

    private By findGuestLookupFetchButtonLocator() {
        By accessibilityLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN);

        try {
            Wait.waitTillElementIsPresent(driver, accessibilityLocator, 3);
            return accessibilityLocator;
        } catch (RuntimeException ignored) {
            return AppiumBy.androidUIAutomator(
                    "new UiSelector().textMatches(\"(?i)" + GUEST_LOOKUP_FETCH_LABEL + "\")");
        }
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
     * This is a much stronger signal than the native shell alone.
     */
    private void waitForWebChecklistContent() {
        switchToWebViewContext();
        Wait.waitTillElementIsClickable(driver, AppiumBy.cssSelector("#" + WEB_CONFIRM_BTN), 20);
    }

    private void dismissValidationAlert() {
        By alertCard = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_CARD);
        By okButton = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_OK);
        By backdrop = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_BACKDROP);

        Wait.waitTillElementIsPresent(driver, alertCard, 5);

        try {
            Wait.waitTillElementIsClickable(driver, okButton, 5).click();
            Wait.waitTillElementDisappears(driver, alertCard);
            return;
        } catch (Exception ignored) {
            // The OK button may not be hittable in some simulator states.
        }

        try {
            Wait.waitTillElementIsClickable(driver, backdrop, 5).click();
            Wait.waitTillElementDisappears(driver, alertCard);
        } catch (Exception e) {
            throw new RuntimeException("Unable to dismiss guest count validation dialog", e);
        }
    }

    /** Switch Appium context to the first available WEBVIEW. */
    private void switchToWebViewContext() {
        if (!(driver instanceof SupportsContextSwitching))
            return;
        SupportsContextSwitching ctx = (SupportsContextSwitching) driver;
        for (int attempt = 0; attempt < 10; attempt++) {
            for (String c : ctx.getContextHandles()) {
                if (c.startsWith("WEBVIEW")) {
                    ctx.context(c);
                    System.out.printf("[Android] Switched to context: %s%n", c);
                    return;
                }
            }
            Wait.waitFor(1);
        }
        throw new RuntimeException("No WEBVIEW context found for checklist screen");
    }

    /** Switch back to NATIVE_APP context. */
    private void switchToNativeContext() {
        if (driver instanceof SupportsContextSwitching) {
            ((SupportsContextSwitching) driver).context("NATIVE_APP");
            System.out.println("[Android] Switched back to NATIVE_APP context");
        }
    }
}
