package io.specmatic.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.os.ExecutableFinder;
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
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.AndroidMultiDeviceTarget;
import com.applitools.eyes.visualgrid.model.IosMultiDeviceTarget;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.specmatic.utils.Wait;

/**
 * BaseTest – shared Appium + Applitools setup/teardown for all platform tests.
 *
 * Key configuration (set via environment variables or override static fields):
 * APPLITOOLS_API_KEY – required IS_EYES_ENABLED – set to "false" to disable
 * visual checks (default: true) IS_NML – set to "true" to use NML-instrumented
 * app (default: false)
 */
public abstract class BaseTest {

    // ── Configuration ──────────────────────────────────────────────────────
    /**
     * Set to true to enable Applitools visual checkpoints.
     */
    protected static final boolean IS_EYES_ENABLED
            = !"false".equalsIgnoreCase(System.getenv("IS_EYES_ENABLED"))
            && !"false".equalsIgnoreCase(System.getProperty("IS_EYES_ENABLED"));

    /**
     * Set to true to load the NML-instrumented app from builds/. When false the
     * standard (non-NML) app from builds/ is used.
     */
    protected static final boolean IS_NML
            = "true".equalsIgnoreCase(System.getenv("IS_NML"))
            || "true".equalsIgnoreCase(System.getProperty("IS_NML"));

    protected static final String APPLITOOLS_API_KEY
            = System.getenv("APPLITOOLS_API_KEY") != null
            ? System.getenv("APPLITOOLS_API_KEY")
            : System.getProperty("APPLITOOLS_API_KEY", "");

    // Builds root (relative to project root / appium-tests module)
    protected static final String BUILDS_ROOT
            = projectRoot().getAbsolutePath()
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
        if (batch != null) {
            batch.setCompleted(true);
        }
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
        if (driver != null) {
            driver.quit();
        }
        Assert.assertTrue(passed, "Visual differences detected by Applitools Eyes.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════
    private static void startAppiumServer() {
        File projectRoot = projectRoot();
        File appiumMainScript = new File(projectRoot, "node_modules/appium/build/lib/main.js");
        File nodeExecutable = findNodeExecutable();

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .usingAnyFreePort()
                .withArgument(GeneralServerFlag.ALLOW_INSECURE, "adb_shell")
                .withArgument(GeneralServerFlag.RELAXED_SECURITY);

        if (appiumMainScript.exists()) {
            System.setProperty(AppiumServiceBuilder.APPIUM_PATH, appiumMainScript.getAbsolutePath());
            builder.withAppiumJS(appiumMainScript);
            System.out.printf("[BaseTest] Using local Appium: %s%n", appiumMainScript.getAbsolutePath());
        } else {
            System.out.println("[BaseTest] Local Appium not found — falling back to global installation");
        }

        if (nodeExecutable != null) {
            System.setProperty("NODE_BINARY_PATH", nodeExecutable.getAbsolutePath());
            builder.usingDriverExecutable(nodeExecutable);
            System.out.printf("[BaseTest] Using node: %s%n", nodeExecutable.getAbsolutePath());
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

    private static File projectRoot() {
        File current = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (current != null) {
            File packageJson = new File(current, "package.json");
            File appiumScript = new File(current, "node_modules/appium/build/lib/main.js");
            if (packageJson.exists() || appiumScript.exists()) {
                return current;
            }
            current = current.getParentFile();
        }
        return new File(System.getProperty("user.dir")).getAbsoluteFile();
    }

    private static File findNodeExecutable() {
        String[] candidates = {
                System.getenv("NODE_BINARY_PATH"),
                System.getProperty("NODE_BINARY_PATH"),
                "/opt/homebrew/bin/node",
                "/usr/local/bin/node"
        };

        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            File node = new File(candidate);
            if (node.exists()) {
                return node;
            }
        }
        return null;
    }

    protected void uninstallAndroidPackage(String packageName) {
        try {
            File adb = resolveAdbExecutable();
            if (adb == null) {
                System.out.printf("[BaseTest] adb not found; skipping uninstall of %s%n", packageName);
                return;
            }

            ProcessBuilder builder = new ProcessBuilder(adb.getAbsolutePath(), "uninstall", packageName);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            System.out.printf("[BaseTest] adb uninstall %s -> exit=%d%n", packageName, exitCode);
            if (!output.isBlank()) {
                System.out.println("[BaseTest] adb output: " + output.trim());
            }
        } catch (IOException e) {
            System.out.printf("[BaseTest] Unable to uninstall %s before test: %s%n", packageName, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("[BaseTest] Interrupted while uninstalling %s%n", packageName);
        }
    }

    private File resolveAdbExecutable() {
        String[] candidates = {
                System.getenv("ANDROID_ADB"),
                System.getProperty("ANDROID_ADB"),
                System.getenv("ANDROID_HOME"),
                System.getenv("ANDROID_SDK_ROOT"),
                "/Users/anand.bagmar/Library/Android/sdk/platform-tools/adb"
        };

        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            File adb = new File(candidate);
            if (adb.isDirectory()) {
                adb = new File(adb, "platform-tools/adb");
            }
            if (adb.exists()) {
                return adb;
            }
        }

        String adbOnPath = new ExecutableFinder().find("adb");
        return adbOnPath != null ? new File(adbOnPath) : null;
    }

    /**
     * Resolve the app path from the latest platform build folder based on
     * platform and NML flag.
     */
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

    /**
     * Configure Applitools Eyes for the current test.
     */
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
        if (appName.toLowerCase().contains("android")) {
            eyes.addProperty("PLATFORM", "Android");
            Configuration config = eyes.getConfiguration();
            config.addMultiDeviceTarget(
                    AndroidMultiDeviceTarget.Galaxy_S25(),
                    AndroidMultiDeviceTarget.Galaxy_S25_Ultra(),
                    AndroidMultiDeviceTarget.Pixel_9()
            );
            eyes.setConfiguration(config);
        } else if (appName.toLowerCase().contains("ios")) {
            eyes.addProperty("PLATFORM", "iOS");
            Configuration config = eyes.getConfiguration();
            config.addMultiDeviceTarget(
                    IosMultiDeviceTarget.iPhone_14(),
                    IosMultiDeviceTarget.iPhone_14_Pro_Max()
            );
            eyes.setConfiguration(config);
        }
        eyes.open(driver, appName, testInfo.getName());
    }

    /**
     * Convenience: take a named Eyes checkpoint.
     */
    protected void checkpoint(String tag) {
        Wait.waitFor(2);
        eyes.checkWindow(tag);
    }
}
