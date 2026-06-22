package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.WebElement;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.PlannerScreen;

public class HomeScreenWeb extends HomeScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String HOME_SCREEN = "home.screen";
    private static final String HOME_NAME_INPUT = "home.input.name";
    private static final String HOME_FLOW_ORIGINAL_BTN = "home.button.flow.original";
    private static final String HOME_FLOW_ALTERNATE_BTN = "home.button.flow.alternate";

    private final Driver driver;
    private final Visual visually;

    public HomeScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public HomeScreen waitForScreen() {
        CmpWeb.waitVisible(driver, HOME_SCREEN);
        CmpWeb.waitVisible(driver, HOME_NAME_INPUT);
        visually.checkWindow(APP_NAME, "App Launch");
        return this;
    }

    @Override
    public HomeScreen enterName(String name) {
        WebElement nameInput = CmpWeb.waitVisible(driver, HOME_NAME_INPUT);
        nameInput.clear();
        nameInput.sendKeys(name);
        return this;
    }

    @Override
    public PlannerScreen startPlanner(boolean alternateFlow) {
        CmpWeb.click(driver, alternateFlow ? HOME_FLOW_ALTERNATE_BTN : HOME_FLOW_ORIGINAL_BTN);
        return PlannerScreen.get().waitForScreen();
    }

    @Override
    public boolean isAtHome() {
        return driver.isElementPresent(CmpWeb.byTestId(HOME_SCREEN));
    }
}
