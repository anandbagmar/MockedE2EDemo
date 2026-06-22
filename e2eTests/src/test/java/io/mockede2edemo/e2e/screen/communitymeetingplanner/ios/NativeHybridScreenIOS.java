package io.mockede2edemo.e2e.screen.communitymeetingplanner.ios;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.GuestLookupScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;
import io.specmatic.utils.Wait;

public class NativeHybridScreenIOS extends NativeHybridScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String NATIVE_HYBRID_SCREEN = "nativeHybrid.screen";
    private static final String NATIVE_HYBRID_MODE_NOTE = "nativeHybrid.mode.hybrid.swift";
    private static final String NATIVE_HYBRID_VIEW = "nativeHybrid.nativeView";
    private static final String NATIVE_HYBRID_CONTINUE_BTN = "nativeHybrid.button.continue";
    private static final String GUEST_LOOKUP_SCREEN = "guestLookup.screen";

    private final Driver driver;
    private final Visual visually;

    public NativeHybridScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public NativeHybridScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(NATIVE_HYBRID_SCREEN), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(NATIVE_HYBRID_MODE_NOTE), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(NATIVE_HYBRID_VIEW), 30);
        checkpointNative("Swift Hybrid Screen");
        return this;
    }

    @Override
    public GuestLookupScreen continueToGuestLookup() {
        tapAndWaitForScreen(NATIVE_HYBRID_CONTINUE_BTN, GUEST_LOOKUP_SCREEN);
        return GuestLookupScreen.get().waitForScreen();
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
