package io.mockede2edemo.e2e.screen.communitymeetingplanner.ios;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.GuestLookupScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.checkpointNative;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.tapAndWaitForScreen;

public class NativeHybridScreenIOS extends NativeHybridScreen {
    private static final Platform PLATFORM = Platform.iOS;

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
        checkpointNative(driver, visually, PLATFORM, "Swift Hybrid Screen");
        return this;
    }

    @Override
    public GuestLookupScreen continueToGuestLookup() {
        tapAndWaitForScreen(driver, NATIVE_HYBRID_CONTINUE_BTN, GUEST_LOOKUP_SCREEN);
        return GuestLookupScreen.get().waitForScreen();
    }
}
