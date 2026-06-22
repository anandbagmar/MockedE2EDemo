package io.mockede2edemo.e2e.screen.communitymeetingplanner.android;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.checkpointNative;
import static io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile.CmpMobileSupport.tapAndWaitForScreen;

public class SummaryScreenAndroid extends SummaryScreen {
    private static final Platform PLATFORM = Platform.android;

    private static final String SUMMARY_SCREEN = "summary.screen";
    private static final String SUMMARY_RESTART_BTN = "summary.button.restart";
    private static final String HOME_SCREEN = "home.screen";

    private final Driver driver;
    private final Visual visually;

    public SummaryScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SummaryScreen waitForScreen() {
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(SUMMARY_SCREEN), 30);
        checkpointNative(driver, visually, PLATFORM, "Summary Screen");
        return this;
    }

    @Override
    public String getThankYouText() {
        // The mobile summary does not expose the thank-you text as a readable
        // accessibility node; verification is web-only.
        return "";
    }

    @Override
    public String getUniqueId() {
        return "";
    }

    @Override
    public HomeScreen restart() {
        tapAndWaitForScreen(driver, SUMMARY_RESTART_BTN, HOME_SCREEN);
        driver.waitTillElementIsVisible(AppiumBy.accessibilityId(HOME_SCREEN), 20);
        checkpointNative(driver, visually, PLATFORM, "Home Screen");
        return HomeScreen.get();
    }
}
