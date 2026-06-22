package io.mockede2edemo.e2e.screen.communitymeetingplanner.mobile;

import org.openqa.selenium.By;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.specmatic.utils.Wait;

/**
 * App-specific helpers shared by the Community Meeting Planner mobile
 * (android / iOS) screens.
 *
 * This intentionally contains ONLY behaviour that the teswiz {@link Driver}
 * does not already provide. For element waits, clicks, scrolling, keyboard and
 * context switching, the screens call {@link Driver} directly. What remains
 * here is:
 *   - Applitools checkpoints (Visual + context switch + settle),
 *   - tap-a-CTA-and-wait-for-the-next-screen with retries,
 *   - dismissing the guest-count validation dialog (needs disappearance waits).
 */
public final class CmpMobileSupport {

    public static final String APP_NAME = "Community Meeting Planner";

    private CmpMobileSupport() {}

    public static void checkpoint(Visual visually, String tag) {
        Wait.waitFor(3);
        visually.checkWindow(APP_NAME, tag);
    }

    public static void checkpointNative(Driver driver, Visual visually, Platform platform, String tag) {
        switchToNativeContext(driver, platform);
        checkpoint(visually, tag);
    }

    public static void checkpointWebView(Driver driver, Visual visually, Platform platform, String tag) {
        switchToWebViewContext(driver, platform);
        checkpoint(visually, tag);
    }

    /** Switch to the webview context; on iOS this is best-effort, on android it must succeed. */
    public static boolean switchToWebViewContext(Driver driver, Platform platform) {
        try {
            driver.setWebViewContext();
            return true;
        } catch (RuntimeException e) {
            if (platform == Platform.android) {
                throw e;
            }
            return false;
        }
    }

    /** Switch back to the native context; on iOS this is best-effort, on android it must succeed. */
    public static void switchToNativeContext(Driver driver, Platform platform) {
        try {
            driver.setNativeAppContext();
        } catch (RuntimeException e) {
            if (platform == Platform.android) {
                throw e;
            }
        }
    }

    /** Tap a top-of-screen CTA and verify the next screen appears (with retries). */
    public static void tapAndWaitForScreen(Driver driver, String buttonAccId, String nextScreenAccId) {
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

    public static void dismissValidationAlert(Driver driver, String cardId, String okId, String backdropId) {
        AppiumDriver appium = (AppiumDriver) driver.getInnerDriver();
        By alertCard = AppiumBy.accessibilityId(cardId);

        Wait.waitTillElementIsPresent(appium, alertCard, 5);

        try {
            driver.waitForClickabilityOf(AppiumBy.accessibilityId(okId), 5).click();
            Wait.waitTillElementDisappears(appium, alertCard);
            return;
        } catch (RuntimeException ignored) {
            // The OK button may not always be hit-testable.
        }

        try {
            driver.waitForClickabilityOf(AppiumBy.accessibilityId(backdropId), 5).click();
            Wait.waitTillElementDisappears(appium, alertCard);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to dismiss guest count validation dialog", e);
        }
    }
}
