package io.mockede2edemo.e2e.screen.communitymeetingplanner.android;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.tools.Wait;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.PlannerScreen;

public class PlannerScreenAndroid extends PlannerScreen {
    private static final String APP_NAME = "Community Meeting Planner";

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
        checkpointNative("Planner Screen");
        return this;
    }

    @Override
    public NativeJourneyScreen proceedToNativeJourney() {
        tapAndWaitForScreen(PLANNER_NEXT_NATIVE_BTN, NATIVE_JOURNEY_SCREEN);
        return NativeJourneyScreen.get().waitForScreen();
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
