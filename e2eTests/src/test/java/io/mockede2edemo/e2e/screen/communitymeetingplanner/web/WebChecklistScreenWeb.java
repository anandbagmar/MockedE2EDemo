package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;

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
        CmpWeb.waitVisible(driver, CHECKLIST_SCREEN);
        visually.checkWindow(APP_NAME, "Web Checklist Screen");
        return this;
    }

    @Override
    public WebChecklistScreen markChecklistReady() {
        CmpWeb.click(driver, CHECKLIST_CONFIRM_BTN);
        CmpWeb.waitVisible(driver, CHECKLIST_READY);
        visually.checkWindow(APP_NAME, "Checklist Marked Ready");
        return this;
    }

    @Override
    public SummaryScreen completeWorkflow() {
        CmpWeb.click(driver, CHECKLIST_CONTINUE_BTN);
        return SummaryScreen.get().waitForScreen();
    }
}
