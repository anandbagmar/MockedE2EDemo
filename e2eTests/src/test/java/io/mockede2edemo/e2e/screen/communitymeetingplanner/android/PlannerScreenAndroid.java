package io.mockede2edemo.e2e.screen.communitymeetingplanner.android;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.PlannerScreen;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.checkpointNative;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.tapAndWaitForScreen;

public class PlannerScreenAndroid extends PlannerScreen {
    private static final Platform PLATFORM = Platform.android;

    private static final String PLANNER_SECTION_INTRO = "planner.section.intro";
    private static final String PLANNER_NEXT_NATIVE_BTN = "planner.button.next.native";
    private static final String NATIVE_JOURNEY_SCREEN = "nativeJourney.screen";

    private final Driver driver;
    private final Visual visually;

    public PlannerScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public PlannerScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(PLANNER_SECTION_INTRO), 20);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(PLANNER_NEXT_NATIVE_BTN), 20);
        checkpointNative(driver, visually, PLATFORM, "Planner Screen");
        return this;
    }

    @Override
    public NativeJourneyScreen proceedToNativeJourney() {
        tapAndWaitForScreen(driver, PLANNER_NEXT_NATIVE_BTN, NATIVE_JOURNEY_SCREEN);
        return NativeJourneyScreen.get().waitForScreen();
    }
}
