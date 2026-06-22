package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.GuestLookupScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;

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
        CmpWeb.waitVisible(driver, NATIVE_HYBRID_SCREEN);
        CmpWeb.waitVisible(driver, NATIVE_HYBRID_VIEW);
        visually.checkWindow(APP_NAME, "Web Hybrid Screen");
        return this;
    }

    @Override
    public GuestLookupScreen continueToGuestLookup() {
        CmpWeb.click(driver, NATIVE_HYBRID_CONTINUE_BTN);
        return GuestLookupScreen.get().waitForScreen();
    }
}
