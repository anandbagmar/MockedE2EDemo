package com.eot.e2edemo.e2e.screen.communitymeetingplanner.web;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import com.eot.e2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;

public class WebChecklistScreenWeb extends WebChecklistScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String CHECKLIST_SCREEN = "webChecklist.screen";
    private static final String CHECKLIST_CONFIRM_BTN = "webChecklist.button.confirm";
    private static final String CHECKLIST_READY = "webChecklist.ready";
    private static final String CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue";

    private final Driver driver;
    private final Visual visually;

    public WebChecklistScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public WebChecklistScreen waitForScreen() {
        driver.waitTillElementIsVisible(byTestId(CHECKLIST_SCREEN), 20);
        visually.checkWindow(APP_NAME, "Web Checklist Screen");
        return this;
    }

    @Override
    public WebChecklistScreen markChecklistReady() {
        driver.waitForClickabilityOf(byTestId(CHECKLIST_CONFIRM_BTN), 20).click();
        driver.waitTillElementIsVisible(byTestId(CHECKLIST_READY), 20);
        visually.checkWindow(APP_NAME, "Checklist Marked Ready");
        return this;
    }

    @Override
    public SummaryScreen completeWorkflow() {
        driver.waitForClickabilityOf(byTestId(CHECKLIST_CONTINUE_BTN), 20).click();
        return SummaryScreen.get().waitForScreen();
    }

    private static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }
}
