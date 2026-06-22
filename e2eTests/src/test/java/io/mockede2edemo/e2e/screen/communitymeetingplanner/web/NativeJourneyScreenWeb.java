package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeHybridScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;

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
        CmpWeb.waitVisible(driver, NATIVE_JOURNEY_SCREEN);
        CmpWeb.waitVisible(driver, NATIVE_JOURNEY_VIEW);
        visually.checkWindow(APP_NAME, "Web Journey Screen");
        return this;
    }

    @Override
    public NativeHybridScreen continueToHybrid() {
        CmpWeb.click(driver, NATIVE_JOURNEY_CONTINUE_BTN);
        return NativeHybridScreen.get().waitForScreen();
    }
}
