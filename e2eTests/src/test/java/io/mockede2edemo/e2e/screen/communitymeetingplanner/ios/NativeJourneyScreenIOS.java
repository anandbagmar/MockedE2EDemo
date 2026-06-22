package io.mockede2edemo.e2e.screen.communitymeetingplanner.ios;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.checkpointNative;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.tapAndWaitForScreen;

public class NativeJourneyScreenIOS extends NativeJourneyScreen {
    private static final Platform PLATFORM = Platform.iOS;

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
        checkpointNative(driver, visually, PLATFORM, "Swift Native Screen");
        return this;
    }

    @Override
    public NativeHybridScreen continueToHybrid() {
        tapAndWaitForScreen(driver, NATIVE_JOURNEY_CONTINUE_BTN, NATIVE_HYBRID_SCREEN);
        return NativeHybridScreen.get().waitForScreen();
    }
}
