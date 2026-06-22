package io.mockede2edemo.e2e.screen.communitymeetingplanner.android;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.tools.Wait;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;

public class WebChecklistScreenAndroid extends WebChecklistScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String WEB_CHECKLIST_SCREEN = "webChecklist.screen";
    private static final String WEB_CHECKLIST_WEBVIEW = "webChecklist.webview";
    private static final String WEB_CHECKLIST_READY = "webChecklist.ready";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue";
    private static final String WEB_CONFIRM_BUTTON_ID = "confirmButton";
    private static final String SUMMARY_SCREEN = "summary.screen";

    private final Driver driver;
    private final Visual visually;

    public WebChecklistScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public WebChecklistScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(WEB_CHECKLIST_SCREEN), 20);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(WEB_CHECKLIST_READY), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(WEB_CHECKLIST_WEBVIEW), 20);
        driver.setWebViewContext();
        driver.waitForClickabilityOf(AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 20);
        checkpointWebView("Web Checklist Screen");
        return this;
    }

    @Override
    public WebChecklistScreen markChecklistReady() {
        driver.setWebViewContext();
        driver.waitForClickabilityOf(AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 20).click();
        checkpointWebView("Checklist Marked Ready");
        return this;
    }

    @Override
    public SummaryScreen completeWorkflow() {
        driver.setNativeAppContext();
        tapAndWaitForScreen(WEB_CHECKLIST_CONTINUE_BTN, SUMMARY_SCREEN);
        return SummaryScreen.get().waitForScreen();
    }

    private void checkpointWebView(String tag) {
        driver.setWebViewContext();
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
