package io.mockede2edemo.e2e.screen.communitymeetingplanner.android;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.applitools.eyes.appium.AppiumCheckSettings;
import com.applitools.eyes.appium.Target;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.GuestLookupScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;
import io.specmatic.utils.Wait;

public class GuestLookupScreenAndroid extends GuestLookupScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String GUEST_LOOKUP_SCREEN = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT = "guestLookup.input.count";
    private static final String GUEST_LOOKUP_FETCH_BTN = "guestLookup.button.fetch";
    private static final String GUEST_LOOKUP_FETCH_LABEL = "Load Profiles";
    private static final String GUEST_LOOKUP_CARDS = "guestLookup.cards";
    private static final String GUEST_LOOKUP_CARDS_LIST = "guestLookup.cards.list";
    private static final String GUEST_LOOKUP_ALERT_CARD = "guestLookup.alert.card";
    private static final String GUEST_LOOKUP_ALERT_OK = "guestLookup.alert.ok";
    private static final String GUEST_LOOKUP_ALERT_BACKDROP = "guestLookup.alert.backdrop";
    private static final String GUEST_LOOKUP_NEXT_BTN = "guestLookup.button.next";
    private static final String WEB_CHECKLIST_SCREEN = "webChecklist.screen";

    private final Driver driver;
    private final Visual visually;

    public GuestLookupScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public GuestLookupScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(GUEST_LOOKUP_SCREEN), 20);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN), 20);
        checkpointNative("Guest Lookup Screen");
        return this;
    }

    @Override
    public GuestLookupScreen fetchProfilesExpectingValidationError(int count) {
        enterCount(count);
        driver.hideKeyboard();
        tapFetch();
        checkpointNative("Invalid Guest Count - Alert");
        return this;
    }

    @Override
    public GuestLookupScreen dismissValidationAlert() {
        // dismissValidationAlert(driver, GUEST_LOOKUP_ALERT_CARD,
        // GUEST_LOOKUP_ALERT_OK, GUEST_LOOKUP_ALERT_BACKDROP);
        return this;
    }

    @Override
    public GuestLookupScreen loadProfiles(int count) {
        enterCount(count);
        driver.hideKeyboard();
        tapFetch();
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST), 30);
        Wait.waitFor(3);
        visually.check(APP_NAME, "Guest Profiles - Strict & Layout Content",
                (AppiumCheckSettings) Target.window()
                        .layout(AppiumBy.accessibilityId(GUEST_LOOKUP_CARDS_LIST)).strict());
        return this;
    }

    @Override
    public WebChecklistScreen openChecklist() {
        tapAndWaitForScreen(GUEST_LOOKUP_NEXT_BTN, WEB_CHECKLIST_SCREEN);
        return WebChecklistScreen.get().waitForScreen();
    }

    private void checkpointNative(String tag) {
        driver.setNativeAppContext();
        Wait.waitFor(3);
        visually.checkWindow(APP_NAME, tag);
    }

    private void tapAndWaitForScreen(String buttonAccId, String nextScreenAccId) {
        By button = AppiumBy.accessibilityId(buttonAccId);
        By nextScreen = AppiumBy.accessibilityId(nextScreenAccId);
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                driver.waitForClickabilityOf(button, 10).click();
                driver.waitTillElementIsPresent(nextScreen, 10);
                return;
            } catch (RuntimeException e) {
                lastFailure = e;
                Wait.waitFor(1);
            }
        }
        throw new RuntimeException(
                "Unable to tap button and reach next screen: " + buttonAccId + " -> " + nextScreenAccId, lastFailure);
    }

    private void enterCount(int count) {
        driver.scrollTillElementIntoView(AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT));
        WebElement countInput = driver.waitTillElementIsVisible(AppiumBy.accessibilityId(GUEST_LOOKUP_INPUT), 20);
        countInput.click();
        countInput.clear();
        countInput.sendKeys(String.valueOf(count));
    }

    private void tapFetch() {
        driver.waitForClickabilityOf(findFetchButtonLocator(), 20).click();
    }

    private By findFetchButtonLocator() {
        By accessibilityLocator = AppiumBy.accessibilityId(GUEST_LOOKUP_FETCH_BTN);
        if (driver.isElementPresentByAccessibilityId(GUEST_LOOKUP_FETCH_BTN)) {
            return accessibilityLocator;
        }
        return AppiumBy.androidUIAutomator(
                "new UiSelector().textMatches(\"(?i)" + GUEST_LOOKUP_FETCH_LABEL + "\")");
    }
}
