package io.mockede2edemo.e2e.screen.communitymeetingplanner;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.android.GuestLookupScreenAndroid;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.web.GuestLookupScreenWeb;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.ios.GuestLookupScreenIOS;

public abstract class GuestLookupScreen {
    private static final String SCREEN_NAME = GuestLookupScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static GuestLookupScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new GuestLookupScreenWeb(driver, visually);
            case android:
                return new GuestLookupScreenAndroid(driver, visually);
            case iOS:
                return new GuestLookupScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /** Verify the guest lookup screen is loaded and ready. */
    public abstract GuestLookupScreen waitForScreen();

    /** Enter an out-of-range count and trigger the validation alert. */
    public abstract GuestLookupScreen fetchProfilesExpectingValidationError(int count);

    /** Dismiss the validation alert dialog. */
    public abstract GuestLookupScreen dismissValidationAlert();

    /** Enter a valid count and load the guest profiles. */
    public abstract GuestLookupScreen loadProfiles(int count);

    /** Move forward to the web checklist screen. */
    public abstract WebChecklistScreen openChecklist();
}
