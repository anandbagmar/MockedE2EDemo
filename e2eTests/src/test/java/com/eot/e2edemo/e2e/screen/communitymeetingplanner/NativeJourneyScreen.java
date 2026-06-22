package com.eot.e2edemo.e2e.screen.communitymeetingplanner;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.android.NativeJourneyScreenAndroid;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.ios.NativeJourneyScreenIOS;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.web.NativeJourneyScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NativeJourneyScreen {
    private static final String SCREEN_NAME = NativeJourneyScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static NativeJourneyScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new NativeJourneyScreenWeb(driver, visually);
            case android:
                return new NativeJourneyScreenAndroid(driver, visually);
            case iOS:
                return new NativeJourneyScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /** Verify the native journey detail step is loaded. */
    public abstract NativeJourneyScreen waitForScreen();

    /** Continue to the native hybrid step. */
    public abstract NativeHybridScreen continueToHybrid();
}
