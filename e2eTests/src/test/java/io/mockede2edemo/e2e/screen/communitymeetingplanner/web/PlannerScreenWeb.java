package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.NativeJourneyScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.PlannerScreen;

public class PlannerScreenWeb extends PlannerScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String PLANNER_SCREEN = "planner.screen";
    private static final String PLANNER_INTRO = "planner.section.intro";
    private static final String PLANNER_NEXT_BTN = "planner.button.next";

    private final Driver driver;
    private final Visual visually;

    public PlannerScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public PlannerScreen waitForScreen() {
        driver.waitTillElementIsVisible(byTestId(PLANNER_SCREEN), 20);
        driver.waitTillElementIsVisible(byTestId(PLANNER_INTRO), 20);
        visually.checkWindow(APP_NAME, "Planner Screen");
        return this;
    }

    @Override
    public NativeJourneyScreen proceedToNativeJourney() {
        driver.waitForClickabilityOf(byTestId(PLANNER_NEXT_BTN), 20).click();
        return NativeJourneyScreen.get().waitForScreen();
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
