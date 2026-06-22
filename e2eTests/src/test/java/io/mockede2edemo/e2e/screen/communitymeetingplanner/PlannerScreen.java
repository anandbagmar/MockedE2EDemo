package io.mockede2edemo.e2e.screen.communitymeetingplanner;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.android.PlannerScreenAndroid;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.ios.PlannerScreenIOS;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.web.PlannerScreenWeb;

public abstract class PlannerScreen {
    private static final String SCREEN_NAME = PlannerScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static PlannerScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new PlannerScreenWeb(driver, visually);
            case android:
                return new PlannerScreenAndroid(driver, visually);
            case iOS:
                return new PlannerScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /** Verify the planner screen is loaded (intro + next CTA). */
    public abstract PlannerScreen waitForScreen();

    /** Move forward to the native journey detail step. */
    public abstract NativeJourneyScreen proceedToNativeJourney();
}
