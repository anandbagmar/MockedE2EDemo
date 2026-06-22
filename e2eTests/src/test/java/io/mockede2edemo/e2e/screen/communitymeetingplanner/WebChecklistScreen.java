package io.mockede2edemo.e2e.screen.communitymeetingplanner;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.android.WebChecklistScreenAndroid;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.ios.WebChecklistScreenIOS;
import io.mockede2edemo.e2e.screen.communitymeetingplanner.web.WebChecklistScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class WebChecklistScreen {
    private static final String SCREEN_NAME = WebChecklistScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static WebChecklistScreen get() {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);

        switch (platform) {
            case web:
                return new WebChecklistScreenWeb(driver, visually);
            case android:
                return new WebChecklistScreenAndroid(driver, visually);
            case iOS:
                return new WebChecklistScreenIOS(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + platform);
    }

    /**
     * Verify the checklist screen (and, on mobile, its inline webview) is ready.
     */
    public abstract WebChecklistScreen waitForScreen();

    /** Confirm the checklist so the workflow can complete. */
    public abstract WebChecklistScreen markChecklistReady();

    /** Complete the workflow and land on the summary screen. */
    public abstract SummaryScreen completeWorkflow();
}
