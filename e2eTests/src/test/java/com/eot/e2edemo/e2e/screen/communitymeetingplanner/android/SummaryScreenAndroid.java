package com.eot.e2edemo.e2e.screen.communitymeetingplanner.android;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.tools.Wait;

import io.appium.java_client.AppiumBy;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;

public class SummaryScreenAndroid extends SummaryScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String SUMMARY_SCREEN = "summary.screen";
    private static final String SUMMARY_RESTART_BTN = "summary.button.restart";
    private static final String HOME_SCREEN = "home.screen";

    private final Driver driver;
    private final Visual visually;

    public SummaryScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SummaryScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(SUMMARY_SCREEN), 30);
        checkpointNative("Summary Screen");
        return this;
    }

    @Override
    public String getThankYouText() {
        // The mobile summary does not expose the thank-you text as a readable
        // accessibility node; verification is web-only.
        return "";
    }

    @Override
    public String getUniqueId() {
        return "";
    }

    @Override
    public HomeScreen restart() {
        tapAndWaitForScreen(SUMMARY_RESTART_BTN, HOME_SCREEN);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(HOME_SCREEN), 20);
        checkpointNative("Home Screen");
        return HomeScreen.get();
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
}
