package com.eot.e2edemo.e2e.screen.communitymeetingplanner.ios;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.tools.Wait;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.WebChecklistScreen;

public class WebChecklistScreenIOS extends WebChecklistScreen {
    private static final String APP_NAME = "Community Meeting Planner";

    private static final String WEB_CHECKLIST_SCREEN = "webChecklist.screen";
    private static final String WEB_CHECKLIST_WEBVIEW = "webChecklist.webview";
    private static final String WEB_CHECKLIST_READY = "webChecklist.ready";
    private static final String WEB_CHECKLIST_CONTINUE_BTN = "webChecklist.button.continue";
    private static final String WEB_CONFIRM_BUTTON_ID = "confirmButton";
    private static final String WEB_CONFIRM_NATIVE_LABEL = "Mark checklist as ready";
    private static final String SUMMARY_SCREEN = "summary.screen";

    private final Driver driver;
    private final Visual visually;

    public WebChecklistScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public WebChecklistScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(WEB_CHECKLIST_SCREEN), 20);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(WEB_CHECKLIST_READY), 30);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(WEB_CHECKLIST_WEBVIEW), 20);
        try {
            if (switchToWebViewContext()) {
                driver.waitForClickabilityOf(AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 20);
            }
        } catch (RuntimeException ignored) {
            // The iOS flow can fall back to the native accessibility tree.
        }
        checkpointWebView("Web Checklist Screen");
        return this;
    }

    @Override
    public WebChecklistScreen markChecklistReady() {
        tapConfirmButton();
        checkpointWebView("Checklist Marked Ready");
        return this;
    }

    @Override
    public SummaryScreen completeWorkflow() {
        switchToNativeContext();
        tapAndWaitForScreen(WEB_CHECKLIST_CONTINUE_BTN, SUMMARY_SCREEN);
        return SummaryScreen.get().waitForScreen();
    }

    /**
     * The confirm button lives inside the inline WebView. Try the webview CSS
     * selector first, then the native accessibility label, and finally a
     * coordinate tap for simulator states that do not expose the button at all.
     */
    private void tapConfirmButton() {
        try {
            if (switchToWebViewContext()) {
                driver.waitForClickabilityOf(AppiumBy.cssSelector("#" + WEB_CONFIRM_BUTTON_ID), 10).click();
                return;
            }
        } catch (RuntimeException ignored) {
            // Fall through to native / coordinate fallbacks.
        }

        try {
            switchToNativeContext();
            driver.waitForClickabilityOf(AppiumBy.accessibilityId(WEB_CONFIRM_NATIVE_LABEL), 10).click();
            return;
        } catch (RuntimeException ignored) {
            // Final fallback: coordinate tap.
        }

        Wait.waitFor(1);
        Map<String, Object> args = new HashMap<>();
        args.put("x", 187);
        args.put("y", 560);
        ((AppiumDriver) driver.getInnerDriver()).executeScript("mobile: tap", args);
    }

    private void checkpointWebView(String tag) {
        switchToWebViewContext();
        Wait.waitFor(3);
        visually.checkWindow(APP_NAME, tag);
    }

    /** Switch to the webview context; on iOS this is best-effort. */
    private boolean switchToWebViewContext() {
        try {
            driver.setWebViewContext();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Switch back to the native context; on iOS this is best-effort. */
    private void switchToNativeContext() {
        try {
            driver.setNativeAppContext();
        } catch (RuntimeException ignored) {
            // Best-effort on iOS.
        }
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
