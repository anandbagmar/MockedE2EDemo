package com.eot.e2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import com.eot.e2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;

public class NativeJourneyScreenWeb extends NativeJourneyScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String NATIVE_JOURNEY_SCREEN = "nativeJourney.screen";
    private static final String NATIVE_JOURNEY_VIEW = "nativeJourney.nativeView";
    private static final String NATIVE_JOURNEY_CONTINUE_BTN = "nativeJourney.button.continue";

    private final Driver driver;
    private final Visual visually;

    public NativeJourneyScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public NativeJourneyScreen waitForScreen() {
        driver.waitTillElementIsVisible(byTestId(NATIVE_JOURNEY_SCREEN), 20);
        driver.waitTillElementIsVisible(byTestId(NATIVE_JOURNEY_VIEW), 20);
        visually.checkWindow(APP_NAME, "Web Journey Screen");
        return this;
    }

    @Override
    public NativeHybridScreen continueToHybrid() {
        driver.waitForClickabilityOf(byTestId(NATIVE_JOURNEY_CONTINUE_BTN), 20).click();
        return NativeHybridScreen.get().waitForScreen();
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
