package io.mockede2edemo.e2e.screen.communitymeetingplanner.android;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.checkpointWebView;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.switchToNativeContext;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.tapAndWaitForScreen;

public class WebChecklistScreenAndroid extends WebChecklistScreen {
    private static final Platform PLATFORM = Platform.android;

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
        checkpointWebView(driver, visually, PLATFORM, "Web Checklist Screen");
        return this;
    }

    @Override
    public WebChecklistScreen markChecklistReady() {
        driver.setWebViewContext();
        driver.waitForClickabilityOf(AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 20).click();
        checkpointWebView(driver, visually, PLATFORM, "Checklist Marked Ready");
        return this;
    }

    @Override
    public SummaryScreen completeWorkflow() {
        switchToNativeContext(driver, PLATFORM);
        tapAndWaitForScreen(driver, WEB_CHECKLIST_CONTINUE_BTN, SUMMARY_SCREEN);
        return SummaryScreen.get().waitForScreen();
    }
}
