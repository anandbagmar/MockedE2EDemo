package io.mockede2edemo.e2e.screen.communitymeetingplanner;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;

import io.mockede2edemo.e2e.screen.communitymeetingplanner.android.NativeHybridScreenAndroid;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.ios.NativeHybridScreenIOS;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.web.NativeHybridScreenWeb;

public abstract class NativeHybridScreen {
    private static final String SCREEN_NAME = NativeHybridScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static NativeHybridScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new NativeHybridScreenWeb(driver, visually);
            case android:
                return new NativeHybridScreenAndroid(driver, visually);
            case iOS:
                return new NativeHybridScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /** Verify the native hybrid step is loaded. */
    public abstract NativeHybridScreen waitForScreen();

    /** Continue to the guest lookup step. */
    public abstract GuestLookupScreen continueToGuestLookup();
}
