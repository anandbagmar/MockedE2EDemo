package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;

public class SummaryScreenWeb extends SummaryScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String SUMMARY_SCREEN = "summary.screen";
    private static final String SUMMARY_THANKYOU_TEXT = "summary.thankYou.text";
    private static final String SUMMARY_UNIQUE_ID = "summary.uniqueId";
    private static final String SUMMARY_RESTART_BTN = "summary.button.restart";

    private final Driver driver;
    private final Visual visually;

    public SummaryScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SummaryScreen waitForScreen() {
        driver.waitTillElementIsVisible(byTestId(SUMMARY_SCREEN), 30);
        visually.checkWindow(APP_NAME, "Summary Screen");
        return this;
    }

    @Override
    public String getThankYouText() {
        return driver.waitTillElementIsVisible(byTestId(SUMMARY_THANKYOU_TEXT), 20).getText().trim();
    }

    @Override
    public String getUniqueId() {
        return driver.waitTillElementIsVisible(byTestId(SUMMARY_UNIQUE_ID), 20).getText().trim();
    }

    @Override
    public HomeScreen restart() {
        driver.waitForClickabilityOf(byTestId(SUMMARY_RESTART_BTN), 20).click();
        return HomeScreen.get().waitForScreen();
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
