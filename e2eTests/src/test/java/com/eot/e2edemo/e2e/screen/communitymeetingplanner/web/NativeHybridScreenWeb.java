package com.eot.e2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import com.eot.e2edemo.e2e.screen.communitymeetingplanner.GuestLookupScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;

public class NativeHybridScreenWeb extends NativeHybridScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String NATIVE_HYBRID_SCREEN = "nativeHybrid.screen";
    private static final String NATIVE_HYBRID_VIEW = "nativeHybrid.nativeView";
    private static final String NATIVE_HYBRID_CONTINUE_BTN = "nativeHybrid.button.continue";

    private final Driver driver;
    private final Visual visually;

    public NativeHybridScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public NativeHybridScreen waitForScreen() {
        driver.waitTillElementIsVisible(byTestId(NATIVE_HYBRID_SCREEN), 20);
        driver.waitTillElementIsVisible(byTestId(NATIVE_HYBRID_VIEW), 20);
        visually.checkWindow(APP_NAME, "Web Hybrid Screen");
        return this;
    }

    @Override
    public GuestLookupScreen continueToGuestLookup() {
        driver.waitForClickabilityOf(byTestId(NATIVE_HYBRID_CONTINUE_BTN), 20).click();
        return GuestLookupScreen.get().waitForScreen();
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
