package io.specmatic.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.openqa.selenium.os.ExecutableFinder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.applitools.ICheckSettings;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsStatus;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import com.applitools.eyes.config.Configuration;
import com.applitools.eyes.visualgrid.model.AndroidMultiDeviceTarget;
import com.applitools.eyes.visualgrid.model.IosMultiDeviceTarget;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.mockede2edemo.tests.Wait;

/**
 * BaseTest – shared Appium + Applitools setup/teardown for all platform tests.
 *
 * Key configuration (set via environment variables or override static fields):
 * APPLITOOLS_API_KEY – required IS_EYES_ENABLED – set to "false" to disable
 * visual checks (default: true) IS_NML – set to "true" to use NML-instrumented
 * app (default: false) IS_NML_R – Android-only, set to "true" to use the
 * Applitools "-r" APK variant (default: false)
 */
public abstract class BaseTest {

    // ── Configuration ──────────────────────────────────────────────────────
    /**
     * Set to true to enable Applitools visual checkpoints.
     */
    protected static final boolean IS_EYES_ENABLED = !"false".equalsIgnoreCase(System.getenv("IS_EYES_ENABLED"))
            && !"false".equalsIgnoreCase(System.getProperty("IS_EYES_ENABLED"));

    /**
     * Set to true to load the NML-instrumented app from builds/. When false the
     * standard (non-NML) app from builds/ is used.
     */
    protected static final boolean IS_NML = "true".equalsIgnoreCase(System.getenv("IS_NML"))
            || "true".equalsIgnoreCase(System.getProperty("IS_NML"));

    /**
     * Android-only flag to load the NML "-r" APK variant from builds/.
     */
    protected static final boolean IS_NML_R = "true".equalsIgnoreCase(System.getenv("IS_NML_R"))
            || "true".equalsIgnoreCase(System.getProperty("IS_NML_R"));

    protected static final String APPLITOOLS_API_KEY = System.getenv("APPLITOOLS_API_KEY") != null
            ? System.getenv("APPLITOOLS_API_KEY")
            : System.getProperty("APPLITOOLS_API_KEY", "");

    // Builds root (relative to project root / e2eTests module)
    protected static final String BUILDS_ROOT = projectRoot().getAbsolutePath()
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

        String platformLabel = resolveBatchPlatform();
        String batchName = "CommunityMeetingPlanner"
                + " - " + platformLabel;
        if (IS_NML) {
            batchName += " - NML";
        }
        batch = new BatchInfo(batchName);
        batch.setId(String.valueOf(EPOCH));
        batch.addProperty("REPOSITORY_NAME",
                new File(System.getProperty("user.dir")).getName());
        batch.addProperty("PLATFORM", platformLabel);
        batch.addProperty("IS_NML", String.valueOf(IS_NML));
        batch.addProperty("IS_NML_R", String.valueOf(IS_NML_R));

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
        File appiumLogFile = resolveAppiumLogFile(projectRoot);
        resetLogFile(appiumLogFile);

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .usingAnyFreePort()
                .withArgument(GeneralServerFlag.ALLOW_INSECURE, "adb_shell")
                .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                .withLogFile(appiumLogFile);

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

        System.out.printf("[BaseTest] Appium log file: %s%n", appiumLogFile.getAbsolutePath());

        localAppiumServer = AppiumDriverLocalService.buildService(builder);
        localAppiumServer.start();
        APPIUM_SERVER_URL = localAppiumServer.getUrl().toString();
        System.out.printf("[BaseTest] Appium server started: %s%n", APPIUM_SERVER_URL);
        try {
            Files.writeString(
                    appiumLogFile.toPath(),
                    String.format("Appium server started at %s%n", APPIUM_SERVER_URL),
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.printf("[BaseTest] Unable to append Appium startup line to %s: %s%n",
                    appiumLogFile.getAbsolutePath(), e.getMessage());
        }
    }

    protected void logStep(String platform, String step) {
        System.out.printf("%n%n[%s] STEP: %s%n%n%n", platform, step);
    }

    private static String resolveBatchPlatform() {
        String configuredPlatform = System.getProperty("TEST_PLATFORM");
        if (configuredPlatform == null || configuredPlatform.isBlank()) {
            configuredPlatform = System.getenv("TEST_PLATFORM");
        }
        if (configuredPlatform != null && !configuredPlatform.isBlank()) {
            return normalizePlatformLabel(configuredPlatform);
        }
        return "Unknown";
    }

    private static String normalizePlatformLabel(String platform) {
        String trimmed = platform.trim();
        if (trimmed.equalsIgnoreCase("android")) {
            return "Android";
        }
        if (trimmed.equalsIgnoreCase("ios") || trimmed.equalsIgnoreCase("iOS")) {
            return "iOS";
        }
        if (trimmed.equalsIgnoreCase("android+ios") || trimmed.equalsIgnoreCase("ios+android")) {
            return "Android+iOS";
        }
        return trimmed;
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

    private static File resolveAppiumLogFile(File projectRoot) {
        File reportsDir = new File(projectRoot, "reports/appium");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        String platform = System.getProperty("TEST_PLATFORM");
        if (platform == null || platform.isBlank()) {
            platform = System.getenv("TEST_PLATFORM");
        }
        String suffix = "unknown";
        if (platform != null && !platform.isBlank()) {
            suffix = platform.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }
        return new File(reportsDir, "appium-server-" + suffix + ".log");
    }

    private static void resetLogFile(File appiumLogFile) {
        try {
            Path logPath = appiumLogFile.toPath();
            Files.createDirectories(logPath.getParent());
            Files.deleteIfExists(logPath);
            Files.createFile(logPath);
            Files.writeString(
                    logPath,
                    String.format("Appium log initialized at %s%n", new Date()),
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.printf("[BaseTest] Unable to reset Appium log file %s: %s%n",
                    appiumLogFile.getAbsolutePath(), e.getMessage());
        }
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

    protected void uninstallIosSimulatorApp(String bundleId) {
        try {
            ProcessBuilder builder = new ProcessBuilder("xcrun", "simctl", "uninstall", "booted", bundleId);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            System.out.printf("[BaseTest] simctl uninstall %s -> exit=%d%n", bundleId, exitCode);
            if (!output.isBlank()) {
                System.out.println("[BaseTest] simctl output: " + output.trim());
            }
        } catch (IOException e) {
            System.out.printf("[BaseTest] Unable to uninstall %s before test: %s%n", bundleId, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("[BaseTest] Interrupted while uninstalling %s%n", bundleId);
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
        boolean useNmlBuild = IS_EYES_ENABLED && IS_NML;
        String suffix;
        String filename;
        String buildsDirProperty;
        String defaultPlatformDir;
        if ("android".equals(platform)) {
            suffix = useNmlBuild ? (IS_NML_R ? "-nml-r" : "-nml") : "";
            filename = APP_BASE_NAME + "-debug" + suffix + ".apk";
            buildsDirProperty = "builds.android.dir";
            defaultPlatformDir = BUILDS_ROOT + File.separator + "latest-android";
        } else {
            suffix = useNmlBuild ? "-nml" : "";
            filename = APP_BASE_NAME + "-debug" + suffix + ".app.zip";
            buildsDirProperty = "builds.ios.dir";
            defaultPlatformDir = BUILDS_ROOT + File.separator + "latest-ios";
        }
        String buildsDir = System.getProperty(
                buildsDirProperty,
                System.getProperty("builds.dir", defaultPlatformDir));
        File appFile = new File(buildsDir, filename);
        if (!appFile.exists()) {
            throw new IllegalStateException(
                    "App not found in builds/: " + appFile.getAbsolutePath()
                            + "\nRun scripts/build-android-apks.sh or scripts/build-ios-app.sh first.");
        }
        try {
            String canonicalPath = appFile.getCanonicalPath();
            System.out.printf("[BaseTest] Resolved app path: %s%n", canonicalPath);
            return canonicalPath;
        } catch (IOException e) {
            String absolutePath = appFile.getAbsolutePath();
            System.out.printf("[BaseTest] Resolved app path (fallback): %s%n", absolutePath);
            return absolutePath;
        }
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
        eyes.setForceFullPageScreenshot(true);
        eyes.addProperty("IS_NML", String.valueOf(IS_NML));
        eyes.addProperty("IS_NML_R", String.valueOf(IS_NML_R));
        if (appName.toLowerCase().contains("android")) {
            eyes.addProperty("PLATFORM", "Android");
            if (IS_NML) {
                Configuration config = eyes.getConfiguration();
                config.addMultiDeviceTarget(
                        // AndroidMultiDeviceTarget.Galaxy_S25(),
                        AndroidMultiDeviceTarget.Galaxy_S25_Ultra(),
                        AndroidMultiDeviceTarget.Pixel_9());
                eyes.setConfiguration(config);
            }
        } else if (appName.toLowerCase().contains("ios")) {
            eyes.addProperty("PLATFORM", "iOS");
            if (IS_NML) {
                Configuration config = eyes.getConfiguration();
                config.addMultiDeviceTarget(
                        IosMultiDeviceTarget.iPhone_14(),
                        IosMultiDeviceTarget.iPhone_14_Plus(),
                        IosMultiDeviceTarget.iPhone_14_Pro(),
                        IosMultiDeviceTarget.iPhone_14_Pro_Max(),
                        IosMultiDeviceTarget.iPhone_13(),
                        IosMultiDeviceTarget.iPhone_13_mini(),
                        IosMultiDeviceTarget.iPhone_13_Pro(),
                        IosMultiDeviceTarget.iPhone_13_Pro_Max(),
                        IosMultiDeviceTarget.iPhone_12(),
                        IosMultiDeviceTarget.iPhone_12_mini(),
                        IosMultiDeviceTarget.iPhone_12_Pro(),
                        IosMultiDeviceTarget.iPhone_12_Pro_Max(),
                        IosMultiDeviceTarget.iPhone_11(),
                        IosMultiDeviceTarget.iPhone_11_Pro(),
                        IosMultiDeviceTarget.iPhone_11_Pro_Max());
                eyes.setConfiguration(config);
            }
        }
        eyes.open(driver, appName, testInfo.getName());
    }

    /**
     * Convenience: take a named Eyes checkpoint.
     */
    protected void checkpoint(String tag) {
        Wait.waitFor(3);
        logStep("Applitools", "Checkpoint: " + tag);
        eyes.checkWindow(tag);
    }

    protected void checkpointWithMultipleMatchLevels(String tag, ICheckSettings settings) {
        Wait.waitFor(3);
        logStep("Applitools", "Checkpoint: " + tag + " (multiple match levels)");
        eyes.check(tag, settings);
    }

    /**
     * Convenience: take a named Eyes checkpoint with a temporary match level.
     * This is useful when a subsection contains intentionally variable content.
     */
    protected void checkpointWithMatchLevel(String tag, MatchLevel matchLevel) {
        Wait.waitFor(3);
        logStep("Applitools", "Checkpoint: " + tag + " (match level: " + matchLevel + ")");
        eyes.check(tag, Target.window().matchLevel(matchLevel));
    }
}
