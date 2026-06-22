package com.eot.e2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import com.eot.e2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.PlannerScreen;

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
        driver.waitTillElementIsVisible(byTestId(HOME_SCREEN), 20);
        driver.waitTillElementIsVisible(byTestId(HOME_NAME_INPUT), 20);
        visually.checkWindow(APP_NAME, "App Launch");
        return this;
    }

    @Override
    public HomeScreen enterName(String name) {
        WebElement nameInput = driver.waitTillElementIsVisible(byTestId(HOME_NAME_INPUT), 20);
        nameInput.clear();
        nameInput.sendKeys(name);
        return this;
    }

    @Override
    public PlannerScreen startPlanner(boolean alternateFlow) {
        driver.waitForClickabilityOf(byTestId(alternateFlow ? HOME_FLOW_ALTERNATE_BTN : HOME_FLOW_ORIGINAL_BTN), 20)
                .click();
        return PlannerScreen.get().waitForScreen();
    }

    @Override
    public boolean isAtHome() {
        return driver.isElementPresent(byTestId(HOME_SCREEN));
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
