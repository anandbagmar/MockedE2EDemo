package com.eot.e2edemo.e2e.screen.communitymeetingplanner.ios;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.tools.Wait;

import io.appium.java_client.AppiumBy;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;

public class NativeJourneyScreenIOS extends NativeJourneyScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String NATIVE_JOURNEY_SCREEN = "nativeJourney.screen";
    private static final String NATIVE_JOURNEY_MODE_NOTE = "nativeJourney.mode.native.swift";
    private static final String NATIVE_JOURNEY_VIEW = "nativeJourney.nativeView";
    private static final String NATIVE_JOURNEY_CONTINUE_BTN = "nativeJourney.button.continue";
    private static final String NATIVE_HYBRID_SCREEN = "nativeHybrid.screen";

    private final Driver driver;
    private final Visual visually;

    public NativeJourneyScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public NativeJourneyScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(NATIVE_JOURNEY_SCREEN), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(NATIVE_JOURNEY_MODE_NOTE), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(NATIVE_JOURNEY_VIEW), 30);
        checkpointNative("Swift Native Screen");
        return this;
    }

    @Override
    public NativeHybridScreen continueToHybrid() {
        tapAndWaitForScreen(NATIVE_JOURNEY_CONTINUE_BTN, NATIVE_HYBRID_SCREEN);
        return NativeHybridScreen.get().waitForScreen();
    }

    private void checkpointNative(String tag) {
        try {
            driver.setNativeAppContext();
        } catch (RuntimeException ignored) {
            // On iOS the native context switch is best-effort.
        }
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
