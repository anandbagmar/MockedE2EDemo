package io.mockede2edemo.e2e.screen.communitymeetingplanner;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.android.SummaryScreenAndroid;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.ios.SummaryScreenIOS;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.web.SummaryScreenWeb;

public abstract class SummaryScreen {
    private static final String SCREEN_NAME = SummaryScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static SummaryScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new SummaryScreenWeb(driver, visually);
            case android:
                return new SummaryScreenAndroid(driver, visually);
            case iOS:
                return new SummaryScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /** Verify the summary screen is loaded. */
    public abstract SummaryScreen waitForScreen();

    /** The thank-you message (web only; empty string where not surfaced). */
    public abstract String getThankYouText();

    /** The generated unique id (web only; empty string where not surfaced). */
    public abstract String getUniqueId();

    /** Restart the flow and return to the home screen. */
    public abstract HomeScreen restart();
}
