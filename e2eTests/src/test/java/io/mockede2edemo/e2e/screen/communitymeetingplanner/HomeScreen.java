package io.mockede2edemo.e2e.screen.communitymeetingplanner;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.android.HomeScreenAndroid;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.ios.HomeScreenIOS;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.web.HomeScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class HomeScreen {
    private static final String SCREEN_NAME = HomeScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static HomeScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new HomeScreenWeb(driver, visually);
            case android:
                return new HomeScreenAndroid(driver, visually);
            case iOS:
                return new HomeScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /** Wait until the home screen is loaded and ready for interaction. */
    public abstract HomeScreen waitForScreen();

    /** Enter the attendee name (web only; a no-op on platforms without the field). */
    public abstract HomeScreen enterName(String name);

    /** Open the planner for the chosen flow variant and land on the Planner screen. */
    public abstract PlannerScreen startPlanner(boolean alternateFlow);

    /** True when the home screen is currently displayed. */
    public abstract boolean isAtHome();
}
