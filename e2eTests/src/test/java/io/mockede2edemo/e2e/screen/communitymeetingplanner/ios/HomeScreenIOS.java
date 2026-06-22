package io.mockede2edemo.e2e.screen.communitymeetingplanner.ios;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.tools.Wait;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.PlannerScreen;

public class HomeScreenIOS extends HomeScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String HOME_SCREEN = "home.screen";
    private static final String HOME_PLANNER_BTN = "home.button.planner";
    private static final String PLANNER_ORIGINAL_BTN = "planner.mode.button.original";
    private static final String PLANNER_ALTERNATE_BTN = "planner.mode.button.alternate";

    private final Driver driver;
    private final Visual visually;

    public HomeScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public HomeScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(HOME_SCREEN), 20);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(HOME_PLANNER_BTN), 20);
        checkpointNative("App Launch");
        return this;
    }

    @Override
    public HomeScreen enterName(String name) {
        // The mobile home screen has no name field.
        return this;
    }

    @Override
    public PlannerScreen startPlanner(boolean alternateFlow) {
        driver.waitForClickabilityOf(AppiumBy.accessibilityId(HOME_PLANNER_BTN), 5).click();
        Wait.waitFor(1);
        driver.waitForClickabilityOf(
                AppiumBy.accessibilityId(alternateFlow ? PLANNER_ALTERNATE_BTN : PLANNER_ORIGINAL_BTN), 20).click();
        Wait.waitFor(1);
        return PlannerScreen.get().waitForScreen();
    }

    @Override
    public boolean isAtHome() {
        return driver.isElementPresentByAccessibilityId(HOME_SCREEN);
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
}
