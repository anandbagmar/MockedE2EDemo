package io.specmatic.tests;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsStatus;
import com.applitools.eyes.appium.Eyes;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.specmatic.utils.Wait;

/**
 * BaseTest – shared Appium + Applitools setup/teardown for all platform tests.
 *
 * Key configuration (set via environment variables or override static fields):
 *   APPLITOOLS_API_KEY  – required
 *   IS_EYES_ENABLED     – set to "false" to disable visual checks (default: true)
 *   IS_NML              – set to "true" to use NML-instrumented app (default: false)
 */
public abstract class BaseTest {

    // ── Configuration ──────────────────────────────────────────────────────

    /** Set to true to enable Applitools visual checkpoints. */
    protected static final boolean IS_EYES_ENABLED =
            !"false".equalsIgnoreCase(System.getenv("IS_EYES_ENABLED"))
                    && !"false".equalsIgnoreCase(System.getProperty("IS_EYES_ENABLED"));

    /**
     * Set to true to load the NML-instrumented app from builds/.
     * When false the standard (non-NML) app from builds/ is used.
     */
    protected static final boolean IS_NML =
            "true".equalsIgnoreCase(System.getenv("IS_NML"))
                    || "true".equalsIgnoreCase(System.getProperty("IS_NML"));

    protected static final String APPLITOOLS_API_KEY =
            System.getenv("APPLITOOLS_API_KEY") != null
                    ? System.getenv("APPLITOOLS_API_KEY")
                    : System.getProperty("APPLITOOLS_API_KEY", "");

    // Builds root (relative to project root / appium-tests module)
    protected static final String BUILDS_ROOT =
            new File(System.getProperty("user.dir")).getParentFile().getAbsolutePath()
                    + File.separator + "builds";

    private static final String APP_BASE_NAME = "App Automation Playground";

    // ── Appium server ──────────────────────────────────────────────────────

    protected static String APPIUM_SERVER_URL = "http://localhost:4723/wd/hub/";
    private static AppiumDriverLocalService localAppiumServer;

    // ── Applitools batch ───────────────────────────────────────────────────

    protected static BatchInfo batch;
    private static final long EPOCH = new Date().toInstant().getEpochSecond();

    // ── Per-test state ─────────────────────────────────────────────────────

    protected AppiumDriver driver;
    protected Eyes eyes;

    // ══════════════════════════════════════════════════════════════════════
    // Suite hooks
    // ══════════════════════════════════════════════════════════════════════

    @BeforeSuite
    public static void beforeSuite() {
        startAppiumServer();

        String batchName = "CommunityMeetingPlanner"
                + " NML=" + IS_NML
                + " EYES=" + IS_EYES_ENABLED;
        batch = new BatchInfo(batchName);
        batch.setId(String.valueOf(EPOCH));
        batch.addProperty("REPOSITORY_NAME",
                new File(System.getProperty("user.dir")).getName());

        System.out.printf("[BaseTest] Batch: %s  id=%s%n", batch.getName(), batch.getId());
    }

    @AfterSuite
    public static void afterSuite() {
        if (batch != null) batch.setCompleted(true);
        if (localAppiumServer != null) {
            localAppiumServer.stop();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Method hooks
    // ══════════════════════════════════════════════════════════════════════

    @AfterMethod
    public void afterEach(Method testInfo) {
        boolean passed = true;
        if (IS_EYES_ENABLED && eyes != null) {
            TestResults results = eyes.close(false);
            System.out.printf("[Eyes] %s → %s%n", results.getName(), results.getStatus());
            if (results.getStatus() == TestResultsStatus.Failed
                    || results.getStatus() == TestResultsStatus.Unresolved) {
                passed = false;
            }
        }
        if (driver != null) driver.quit();
        Assert.assertTrue(passed, "Visual differences detected by Applitools Eyes.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private static void startAppiumServer() {
        // Appium is installed locally in the project root's node_modules/ (not globally).
        File projectRoot = new File(System.getProperty("user.dir")).getParentFile();
        File appiumMainScript = new File(projectRoot, "node_modules/appium/build/lib/main.js");

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .usingAnyFreePort()
                .withArgument(GeneralServerFlag.ALLOW_INSECURE, "adb_shell")
                .withArgument(GeneralServerFlag.RELAXED_SECURITY);

        if (appiumMainScript.exists()) {
            builder.withAppiumJS(appiumMainScript);
            System.out.printf("[BaseTest] Using local Appium: %s%n", appiumMainScript.getAbsolutePath());
        } else {
            System.out.println("[BaseTest] Local Appium not found — falling back to global installation");
        }

        String logDir = System.getenv("LOG_DIR");
        if (logDir != null) {
            builder.withLogFile(new File(logDir + "/appium_logs.txt"));
        }

        localAppiumServer = AppiumDriverLocalService.buildService(builder);
        localAppiumServer.start();
        APPIUM_SERVER_URL = localAppiumServer.getUrl().toString();
        System.out.printf("[BaseTest] Appium server started: %s%n", APPIUM_SERVER_URL);
    }

    /** Resolve the app path from the latest platform build folder based on platform and NML flag. */
    protected String resolveAppPath(String platform) {
        // platform: "android" → .apk / "ios" → .app.zip
        String suffix = IS_NML ? "-nml" : "";
        String filename;
        String buildsDirProperty;
        String defaultPlatformDir;
        if ("android".equals(platform)) {
            filename = APP_BASE_NAME + "-debug" + suffix + ".apk";
            buildsDirProperty = "builds.android.dir";
            defaultPlatformDir = BUILDS_ROOT + File.separator + "latest-android";
        } else {
            filename = APP_BASE_NAME + "-debug" + suffix + ".app.zip";
            buildsDirProperty = "builds.ios.dir";
            defaultPlatformDir = BUILDS_ROOT + File.separator + "latest-ios";
        }
        String buildsDir = System.getProperty(
                buildsDirProperty,
                System.getProperty("builds.dir", defaultPlatformDir)
        );
        File appFile = new File(buildsDir, filename);
        if (!appFile.exists()) {
            throw new IllegalStateException(
                    "App not found in builds/: " + appFile.getAbsolutePath()
                            + "\nRun scripts/build-android-apks.sh or scripts/build-ios-app.sh first.");
        }
        return appFile.getAbsolutePath();
    }

    /** Configure Applitools Eyes for the current test. */
    protected void configureEyes(String appName, Method testInfo) {
        eyes = new Eyes();
        eyes.setLogHandler(new StdoutLogHandler(false));
        eyes.setBatch(batch);
        eyes.setApiKey(APPLITOOLS_API_KEY);
        eyes.setServerUrl("https://eyes.applitools.com");
        eyes.setMatchLevel(MatchLevel.STRICT);
        eyes.setIsDisabled(!IS_EYES_ENABLED);
        eyes.setIgnoreCaret(true);
        eyes.setIgnoreDisplacements(true);
        eyes.setSaveNewTests(true);
        eyes.addProperty("IS_NML", String.valueOf(IS_NML));
        eyes.open(driver, appName, testInfo.getName());
    }

    /** Convenience: take a named Eyes checkpoint. */
    protected void checkpoint(String tag) {
        Wait.waitFor(2);
        eyes.checkWindow(tag);
    }
}
