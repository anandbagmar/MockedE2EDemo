package com.eot.e2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import com.eot.e2edemo.e2e.screen.communitymeetingplanner.GuestLookupScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;

public class GuestLookupScreenWeb extends GuestLookupScreen {
    private static final String APP_NAME = "Community Meeting Planner";
    private static final String EXPECTED_ALERT_TITLE = "Invalid guest count";

    private static final String GUEST_LOOKUP_SCREEN = "guestLookup.screen";
    private static final String GUEST_LOOKUP_INPUT = "guestLookup.input.count";
    private static final String GUEST_LOOKUP_FETCH_BTN = "guestLookup.button.fetch";
    private static final String GUEST_LOOKUP_RESULTS = "guestLookup.results";
    private static final String GUEST_LOOKUP_FIRST_CARD = "guestLookup.profile.1.card";
    private static final String GUEST_LOOKUP_ALERT_CARD = "guestLookup.alert.card";
    private static final String GUEST_LOOKUP_ALERT_TITLE = "guestLookup.alert.title";
    private static final String GUEST_LOOKUP_ALERT_OK = "guestLookup.alert.ok";
    private static final String GUEST_LOOKUP_NEXT_BTN = "guestLookup.button.next";

    private final Driver driver;
    private final Visual visually;

    public GuestLookupScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public GuestLookupScreen waitForScreen() {
        driver.waitTillElementIsVisible(byTestId(GUEST_LOOKUP_SCREEN), 20);
        driver.waitTillElementIsVisible(byTestId(GUEST_LOOKUP_INPUT), 20);
        visually.checkWindow(APP_NAME, "Guest Lookup Screen");
        return this;
    }

    @Override
    public GuestLookupScreen fetchProfilesExpectingValidationError(int count) {
        enterCount(count);
        driver.waitForClickabilityOf(byTestId(GUEST_LOOKUP_FETCH_BTN), 20).click();

        WebElement alertTitle = driver.waitTillElementIsVisible(byTestId(GUEST_LOOKUP_ALERT_TITLE), 20);
        visually.checkWindow(APP_NAME, "Invalid Guest Count - Alert");
        if (!EXPECTED_ALERT_TITLE.equalsIgnoreCase(alertTitle.getText().trim())) {
            throw new RuntimeException(
                    "Expected validation alert '" + EXPECTED_ALERT_TITLE + "' for out-of-range guest count, but was: "
                            + alertTitle.getText());
        }
        return this;
    }

    @Override
    public GuestLookupScreen dismissValidationAlert() {
        driver.waitForClickabilityOf(byTestId(GUEST_LOOKUP_ALERT_OK), 20).click();
        driver.waitTillElementIsInvisible(byTestId(GUEST_LOOKUP_ALERT_CARD), 20);
        return this;
    }

    @Override
    public GuestLookupScreen loadProfiles(int count) {
        enterCount(count);
        driver.waitForClickabilityOf(byTestId(GUEST_LOOKUP_FETCH_BTN), 20).click();
        driver.waitTillElementIsVisible(byTestId(GUEST_LOOKUP_RESULTS), 30);
        driver.waitTillElementIsVisible(byTestId(GUEST_LOOKUP_FIRST_CARD), 30);
        visually.checkWindow(APP_NAME, "Guest Profiles Loaded");
        return this;
    }

    @Override
    public WebChecklistScreen openChecklist() {
        driver.waitForClickabilityOf(byTestId(GUEST_LOOKUP_NEXT_BTN), 20).click();
        return WebChecklistScreen.get().waitForScreen();
    }

    private void enterCount(int count) {
        WebElement countInput = driver.waitTillElementIsVisible(byTestId(GUEST_LOOKUP_INPUT), 20);
        countInput.clear();
        countInput.sendKeys(String.valueOf(count));
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
