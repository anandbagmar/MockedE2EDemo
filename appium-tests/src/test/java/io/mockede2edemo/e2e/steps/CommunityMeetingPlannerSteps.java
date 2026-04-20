package io.mockede2edemo.e2e.steps;

import java.util.HashMap;
import java.util.Map;

import com.applitools.ICheckSettings;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.appium.AppiumCheckSettings;
import com.applitools.eyes.appium.Target;
import com.znsio.teswiz.runner.Visual;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mockede2edemo.e2e.entities.MOCKEDE2EDEMO_TEST_CONTEXT;
import io.specmatic.utils.Wait;

public class CommunityMeetingPlannerSteps {
    private static final Logger LOGGER = LogManager.getLogger(CommunityMeetingPlannerSteps.class);

    private static final String APP_NAME = "Community Meeting Planner";

    private static final String HOME_SCREEN = "home.screen";
    private static final String HOME_PLANNER_BTN = "home.button.planner";

    private static final String PLANNER_MODE_PANEL = "planner.mode.panel";
    private static final String PLANNER_MODE_MODAL = "planner.mode.modal";
    private static final String PLANNER_ORIGINAL_BTN = "planner.mode.button.original";
    private static final String PLANNER_ALTERNATE_BTN = "planner.mode.button.alternate";
    private static final String PLANNER_SECTION_INTRO = "planner.section.intro";

    private static final String PLANNER_SCREEN = "planner.screen";
    private static final String PLANNER_NEXT_NATIVE_BTN = "planner.button.next.native";

    private static final String GUEST_LOOKUP_SCREEN = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT = "guestLookup.input.count";
    private static final String GUEST_LOOKUP_FETCH_BTN = "guestLookup.button.fetch";
    private static final String GUEST_LOOKUP_FETCH_LABEL = "Load Profiles";
    private static final String GUEST_LOOKUP_LOADER = "guestLookup.loader";
    private static final String GUEST_LOOKUP_RESULTS = "guestLookup.results";
    private static final String GUEST_LOOKUP_RESULTS_TITLE = "guestLookup.section.results";
    private static final String GUEST_LOOKUP_CARDS = "guestLookup.cards";
    private static final String GUEST_LOOKUP_CARDS_LIST = "guestLookup.cards.list";
    private static final String GUEST_LOOKUP_ALERT_CARD = "guestLookup.alert.card";
    private static final String GUEST_LOOKUP_ALERT_OK = "guestLookup.alert.ok";
    private static final String GUEST_LOOKUP_ALERT_BACKDROP = "guestLookup.alert.backdrop";
    private static final String GUEST_LOOKUP_NEXT_BTN = "guestLookup.button.next";

    private static final String WEB_CHECKLIST_SCREEN = "webChecklist.screen";
    private static final String WEB_CHECKLIST_WEBVIEW = "webChecklist.webview";
    private static final String WEB_CHECKLIST_READY = "webChecklist.ready";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue";
    private static final String WEB_CONFIRM_BUTTON_ID = "confirmButton";
    private static final String WEB_CONFIRM_BTN = "Mark checklist as ready";

    private static final String NATIVE_JOURNEY_SCREEN = "nativeJourney.screen";
    private static final String NATIVE_JOURNEY_MODE_NOTE_ANDROID = "nativeJourney.mode.native.androidx";
    private static final String NATIVE_JOURNEY_MODE_NOTE_IOS = "nativeJourney.mode.native.swift";
    private static final String NATIVE_JOURNEY_VIEW = "nativeJourney.nativeView";
    private static final String NATIVE_JOURNEY_CONTINUE_BTN = "nativeJourney.button.continue";

    private static final String NATIVE_HYBRID_SCREEN = "nativeHybrid.screen";
    private static final String NATIVE_HYBRID_MODE_NOTE_ANDROID = "nativeHybrid.mode.hybrid.androidx";
    private static final String NATIVE_HYBRID_MODE_NOTE_IOS = "nativeHybrid.mode.hybrid.swift";
    private static final String NATIVE_HYBRID_VIEW = "nativeHybrid.nativeView";
    private static final String NATIVE_HYBRID_CONTINUE_BTN = "nativeHybrid.button.continue";

    private static final String SUMMARY_SCREEN = "summary.screen";
    private static final String SUMMARY_RESTART_BTN = "summary.button.restart";

    private final TestExecutionContext context;
    private boolean appLaunchCheckpointTaken;

    public CommunityMeetingPlannerSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
    }

    @Given("I launch the Community Meeting Planner app")
    public void iLaunchTheCommunityMeetingPlannerApp() {
        ensureDriver();
        waitForHomeScreen();
        checkpointNative(Runner.getPlatform(), "App Launch");
        appLaunchCheckpointTaken = true;
    }

    @When("I run the Community Meeting Planner workflow")
    public void iRunTheCommunityMeetingPlannerWorkflow() {
        ensureDriver();
        Platform platform = Runner.getPlatform();
        LOGGER.info("Running Community Meeting Planner workflow on " + platform);

        handleHomeScreen(platform);
        handlePlannerScreen(platform);
        handleNativeJourneyScreen(platform);
        handleNativeHybridScreen(platform);
        handleGuestLookupScreen(platform);
        handleWebChecklistScreen(platform);
        handleSummaryScreen(platform);
    }

    @Then("I should be back on the home screen")
    public void iShouldBeBackOnTheHomeScreen() {
        waitForHomeScreen();
    }

    private void ensureDriver() {
        if (!Drivers.isDriverAssignedForUser(MOCKEDE2EDEMO_TEST_CONTEXT.ME)) {
            Drivers.createDriverFor(MOCKEDE2EDEMO_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        }
    }

    private Driver driver() {
        return Drivers.getDriverForCurrentUser(Thread.currentThread().getId());
    }

    private AppiumDriver appiumDriver() {
        return (AppiumDriver) driver().getInnerDriver();
    }

    private void waitForHomeScreen() {
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(HOME_SCREEN), 20);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(HOME_PLANNER_BTN), 20);
    }

    private void handleHomeScreen(Platform platform) {
        LOGGER.info("Waiting for home screen on " + platform);
        waitForHomeScreen();
        if (!appLaunchCheckpointTaken) {
            checkpointNative(platform, "App Launch");
            appLaunchCheckpointTaken = true;
        }

        Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.accessibilityId(HOME_PLANNER_BTN), 5).click();
        Wait.waitFor(1);

        if (isAlternateFlow()) {
            Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.accessibilityId(PLANNER_ALTERNATE_BTN), 20).click();
        } else {
            Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.accessibilityId(PLANNER_ORIGINAL_BTN), 20).click();
        }
        Wait.waitFor(1);
    }

    private void handlePlannerScreen(Platform platform) {
        LOGGER.info("Verifying planner screen on " + platform);
        if (platform == Platform.android) {
            Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(PLANNER_SECTION_INTRO), 20);
        } else {
            Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(PLANNER_SCREEN), 20);
        }
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(PLANNER_NEXT_NATIVE_BTN), 20);
        checkpointNative(platform, "Planner Screen");
    }

    private void handleNativeJourneyScreen(Platform platform) {
        LOGGER.info("Opening native journey screen on " + platform);
        tapAndWaitForScreen(PLANNER_NEXT_NATIVE_BTN, NATIVE_JOURNEY_SCREEN);

        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(NATIVE_JOURNEY_SCREEN), 30);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(nativeJourneyModeNote(platform)), 30);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(NATIVE_JOURNEY_VIEW), 30);
        checkpointNative(platform, platform == Platform.android ? "AndroidX Native Screen" : "Swift Native Screen");

        tapAndWaitForScreen(NATIVE_JOURNEY_CONTINUE_BTN, NATIVE_HYBRID_SCREEN);
    }

    private void handleNativeHybridScreen(Platform platform) {
        LOGGER.info("Verifying native hybrid screen on " + platform);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(NATIVE_HYBRID_SCREEN), 30);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(nativeHybridModeNote(platform)), 30);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(NATIVE_HYBRID_VIEW), 30);
        checkpointNative(platform, platform == Platform.android ? "AndroidX Hybrid Screen" : "Swift Hybrid Screen");

        tapAndWaitForScreen(NATIVE_HYBRID_CONTINUE_BTN, GUEST_LOOKUP_SCREEN);
    }

    private void handleGuestLookupScreen(Platform platform) {
        LOGGER.info("Handling guest lookup screen on " + platform);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 20);
        checkpointNative(platform, "Guest Lookup Screen");
        waitForGuestLookupReady(platform);

        scrollIntoView(platform, GUEST_LOOKUP_INPUT);
        WebElement countInput = Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
        countInput.click();
        countInput.clear();
        countInput.sendKeys("20");
        if (platform == Platform.android) {
            hideKeyboard(platform);
        }
        tapGuestLookupFetchButton(platform);
        checkpointNative(platform, "Invalid Guest Count - Alert");
        dismissValidationAlert();

        scrollIntoView(platform, GUEST_LOOKUP_INPUT);
        countInput = Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
        countInput.click();
        countInput.clear();
        countInput.sendKeys("10");
        if (platform == Platform.iOS) {
            Wait.waitFor(2);
        }
        if (platform == Platform.android) {
            hideKeyboard(platform);
        }
        tapGuestLookupFetchButton(platform);
        if (platform == Platform.iOS) {
            Wait.waitFor(5);
            checkpointNative(platform, "Guest Profiles - Loaded 10 Sample Guests");
        } else {
            waitForGuestLookupResults(platform);
            checkpointWithMultipleMatchLevels(
                    "Guest Profiles - Strict & Layout Content",
                    (ICheckSettings) Target.window().layout(AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST)).strict());
        }

        tapAndWaitForScreen(GUEST_LOOKUP_NEXT_BTN, WEB_CHECKLIST_SCREEN);
    }

    private void handleWebChecklistScreen(Platform platform) {
        LOGGER.info("Handling web checklist screen on " + platform);
        waitForWebChecklistScreen(platform);
        checkpointWebView(platform, "Web Checklist Screen");
        tapWebChecklistConfirmButton(platform);
        checkpointWebView(platform, "Checklist Marked Ready");
        switchToNativeContext(platform);
        tapAndWaitForScreen(WEB_CHECKLIST_CONTINUE_BTN, SUMMARY_SCREEN);
    }

    private void handleSummaryScreen(Platform platform) {
        LOGGER.info("Handling summary screen on " + platform);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(SUMMARY_SCREEN), 30);
        checkpointNative(platform, "Summary Screen");
        tapAndWaitForScreen(SUMMARY_RESTART_BTN, HOME_SCREEN);
        waitForHomeScreen();
        checkpointNative(platform, "Home Screen");
    }

    private void waitForGuestLookupReady(Platform platform) {
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 20);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
        if (platform == Platform.android) {
            Wait.waitTillElementIsPresent(appiumDriver(), findGuestLookupFetchButtonLocator(platform), 20);
        }
    }

    private void waitForGuestLookupResults(Platform platform) {
        if (platform == Platform.iOS) {
            try {
                Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_LOADER), 5);
            } catch (RuntimeException ignored) {
                // Fast responses can skip the spinner entirely.
            }
            Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_RESULTS), 30);
            try {
                Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_RESULTS_TITLE), 5);
            } catch (RuntimeException ignored) {
                // The section title can be below the fold on iOS.
            }
        }

        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS), 30);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST), 30);
    }

    private void waitForWebChecklistScreen(Platform platform) {
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(WEB_CHECKLIST_SCREEN), 20);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(WEB_CHECKLIST_READY), 30);
        Wait.waitTillElementIsPresent(appiumDriver(), AppiumBy.accessibilityId(WEB_CHECKLIST_WEBVIEW), 20);
        if (platform == Platform.android) {
            switchToWebViewContext(platform);
            Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 20);
        } else {
            try {
                switchToWebViewContext(platform);
                Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 20);
            } catch (RuntimeException ignored) {
                // The iOS flow can fall back to the native accessibility tree.
            }
        }
    }

    private void tapWebChecklistConfirmButton(Platform platform) {
        try {
            switchToWebViewContext(platform);
            Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 10).click();
            return;
        } catch (RuntimeException ignored) {
            // Fall through to native/context fallbacks.
        }

        try {
            switchToNativeContext(platform);
            Wait.waitTillElementIsClickable(appiumDriver(), AppiumBy.accessibilityId(WEB_CONFIRM_BTN), 10).click();
            return;
        } catch (RuntimeException ignored) {
            // Final fallback uses a coordinate tap for simulator states that
            // do not expose the webview button to Appium at all.
        }

        Map<String, Object> args = new HashMap<>();
        args.put("x", 187);
        args.put("y", 560);
        ((AppiumDriver) driver().getInnerDriver()).executeScript("mobile: tap", args);
    }

    private void dismissValidationAlert() {
        By alertCard = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_CARD);
        By okButton = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_OK);
        By backdrop = AppiumBy.accessibilityId(GUEST_LOOKUP_ALERT_BACKDROP);

        Wait.waitTillElementIsPresent(appiumDriver(), alertCard, 5);

        try {
            Wait.waitTillElementIsClickable(appiumDriver(), okButton, 5).click();
            Wait.waitTillElementDisappears(appiumDriver(), alertCard);
            return;
        } catch (RuntimeException ignored) {
            // The OK button may not always be hit-testable.
        }

        try {
            Wait.waitTillElementIsClickable(appiumDriver(), backdrop, 5).click();
            Wait.waitTillElementDisappears(appiumDriver(), alertCard);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to dismiss guest count validation dialog", e);
        }
    }

    private void scrollIntoView(Platform platform, String accessibilityId) {
        By locator = AppiumBy.accessibilityId(accessibilityId);
        if (platform == Platform.android) {
            appiumDriver().findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
                    ".scrollIntoView(new UiSelector().description(\"" + accessibilityId + "\"))"));
            Wait.waitTillElementIsClickable(appiumDriver(), locator, 5);
            return;
        }

        TimeoutException lastFailure = null;
        for (int attempt = 0; attempt < 8; attempt++) {
            try {
                Wait.waitTillElementIsClickable(appiumDriver(), locator, 3);
                return;
            } catch (TimeoutException e) {
                lastFailure = e;
            }

            Map<String, Object> args = new HashMap<>();
            args.put("direction", "down");
            appiumDriver().executeScript("mobile: scroll", args);
            Wait.waitFor(1);
        }

        throw new RuntimeException("Unable to scroll to element: " + accessibilityId, lastFailure);
    }

    private void tapGuestLookupFetchButton(Platform platform) {
        driver().waitForClickabilityOf(findGuestLookupFetchButtonLocator(platform), 20).click();
    }

    private By findGuestLookupFetchButtonLocator(Platform platform) {
        By accessibilityLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN);
        try {
            Wait.waitTillElementIsPresent(appiumDriver(), accessibilityLocator, 3);
            return accessibilityLocator;
        } catch (RuntimeException ignored) {
            if (platform == Platform.android) {
                return AppiumBy.androidUIAutomator(
                        "new UiSelector().textMatches(\"(?i)" + GUEST_LOOKUP_FETCH_LABEL + "\")");
            }
            return accessibilityLocator;
        }
    }

    private void hideKeyboard(Platform platform) {
        try {
            driver().hideKeyboard();
        } catch (RuntimeException ignored) {
            if (platform == Platform.iOS) {
                Map<String, Object> args = new HashMap<>();
                args.put("x", 20);
                args.put("y", 80);
                ((AppiumDriver) driver().getInnerDriver()).executeScript("mobile: tap", args);
            }
        }
    }

    private void tapAndWaitForScreen(String buttonId, String nextScreenId) {
        By buttonLocator = AppiumBy.accessibilityId(buttonId);
        By nextScreenLocator = AppiumBy.accessibilityId(nextScreenId);

        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Wait.waitTillElementIsClickable(appiumDriver(), buttonLocator, 10).click();
                Wait.waitTillElementIsPresent(appiumDriver(), nextScreenLocator, 10);
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

    private boolean switchToWebViewContext(Platform platform) {
        try {
            driver().setWebViewContext();
            return true;
        } catch (RuntimeException e) {
            if (platform == Platform.android) {
                throw e;
            }
            // iOS can remain in native context if the webview is not surfaced.
        }
        return false;
    }

    private void switchToNativeContext(Platform platform) {
        try {
            driver().setNativeAppContext();
        } catch (RuntimeException e) {
            if (platform == Platform.android) {
                throw e;
            }
        }
    }

    private String nativeJourneyModeNote(Platform platform) {
        return platform == Platform.android
                ? NATIVE_JOURNEY_MODE_NOTE_ANDROID
                : NATIVE_JOURNEY_MODE_NOTE_IOS;
    }

    private String nativeHybridModeNote(Platform platform) {
        return platform == Platform.android
                ? NATIVE_HYBRID_MODE_NOTE_ANDROID
                : NATIVE_HYBRID_MODE_NOTE_IOS;
    }

    private Visual visual() {
        return driver().getVisual();
    }

    private void checkpoint(String tag) {
        Wait.waitFor(3);
        LOGGER.info("Applitools checkpoint: " + tag);
        visual().checkWindow(APP_NAME, tag);
    }

    private void checkpointWithMatchLevel(String tag, MatchLevel matchLevel) {
        Wait.waitFor(3);
        LOGGER.info("Applitools checkpoint: " + tag + " (match level: " + matchLevel + ")");
        visual().checkWindow(APP_NAME, tag, matchLevel);
    }

    private void checkpointWithMultipleMatchLevels(String tag, ICheckSettings settings) {
        Wait.waitFor(3);
        LOGGER.info("Applitools checkpoint: " + tag + " (multiple match levels)");
        visual().check(APP_NAME, tag, (AppiumCheckSettings) settings);
    }

    private void checkpointNative(Platform platform, String tag) {
        if (platform == Platform.android || platform == Platform.iOS) {
            switchToNativeContext(platform);
        }
        checkpoint(tag);
    }

    private void checkpointWebView(Platform platform, String tag) {
        if (!switchToWebViewContext(platform)) {
            LOGGER.info("WEBVIEW context not exposed; taking native checkpoint for " + tag);
        }
        checkpoint(tag);
    }

    private boolean isAlternateFlow() {
        return "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
                || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));
    }
}
