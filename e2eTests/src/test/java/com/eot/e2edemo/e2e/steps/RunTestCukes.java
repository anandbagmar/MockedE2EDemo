package com.eot.e2edemo.e2e.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;

import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.visualgrid.model.AndroidMultiDeviceTarget;
import com.applitools.eyes.visualgrid.model.IosMultiDeviceTarget;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.APPLITOOLS;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.steps.Hooks;
import com.znsio.teswiz.tools.JsonPrettyPrinter;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.testng.AbstractTestNGCucumberTests;

public class RunTestCukes
        extends AbstractTestNGCucumberTests {

    private static final Logger LOGGER = LogManager.getLogger(RunTestCukes.class.getName());
    private final TestExecutionContext context;

    public RunTestCukes() {
        long threadId = Thread.currentThread().getId();
        LOGGER.info("RunTestCukes: Constructor: ThreadId: " + threadId);
        context = SessionContext.getTestExecutionContext(threadId);
        System.setProperty(TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT, "@android,@web,@iOS,@api,@cli,@pdf");
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        LOGGER.info(
                String.format("RunTestCukes: ThreadId: %d: in overridden scenarios%n", Thread.currentThread().getId()));
        Object[][] scenarios = super.scenarios();
        LOGGER.info(scenarios);
        return scenarios;
    }

    @Before
    public void beforeTestScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info(
                "RunTestCukes: ThreadId : '%d' :: beforeTestScenario: '%s'".formatted(threadId, scenario.getName()));
        new Hooks().beforeScenario(scenario);
        addApplitoolsUFGConfigurationToContext();
        addApplitoolsNMLConfigurationToContext();
    }

    @After
    public void afterTestScenario(Scenario scenario) {
        long threadId = Thread.currentThread().getId();
        LOGGER.info("RunTestCukes: ThreadId : '%d' :: afterTestScenario: '%s'".formatted(threadId, scenario.getName()));
        new Hooks().afterScenario(scenario);
    }

    private void addApplitoolsUFGConfigurationToContext() {
        Configuration ufgConfig = new Configuration();
        ufgConfig.addBrowser(1280, 900, BrowserType.CHROME);
        ufgConfig.addBrowser(1280, 900, BrowserType.FIREFOX);
        ufgConfig.addBrowser(1280, 900, BrowserType.EDGE_CHROMIUM);
        ufgConfig.addBrowser(1280, 900, BrowserType.SAFARI);
        ufgConfig.addBrowser(1280, 900, BrowserType.CHROME_ONE_VERSION_BACK);
        ufgConfig.addBrowser(1280, 900, BrowserType.FIREFOX_ONE_VERSION_BACK);
        ufgConfig.addBrowser(1280, 900, BrowserType.EDGE_CHROMIUM_ONE_VERSION_BACK);
        ufgConfig.addBrowser(1280, 900, BrowserType.SAFARI_ONE_VERSION_BACK);
        LOGGER.info("Use the following Browsers and devices in UFG config: "
                + JsonPrettyPrinter.prettyPrint(ufgConfig.getBrowsersInfo()));
        context.addTestState(APPLITOOLS.UFG_CONFIG, ufgConfig);
    }

    private void addApplitoolsNMLConfigurationToContext() {
        Platform currentPlatform = Runner.getPlatform();

        if (null == currentPlatform) {
            LOGGER.info("Skipping NML config for platform: " + currentPlatform);
        } else {
            switch (currentPlatform) {
                case iOS:
                    IosMultiDeviceTarget[] iosTargets = new IosMultiDeviceTarget[]{
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
                        IosMultiDeviceTarget.iPhone_11_Pro_Max()
                    };
                    LOGGER.info(
                            "Use the following devices in NML config: " + JsonPrettyPrinter.prettyPrint(iosTargets));
                    context.addTestState(APPLITOOLS.NML_CONFIG, iosTargets);
                    break;
                case android:
                    AndroidMultiDeviceTarget[] androidTargets = new AndroidMultiDeviceTarget[]{
                        AndroidMultiDeviceTarget.Galaxy_S25_Ultra(),
                        AndroidMultiDeviceTarget.Pixel_9()
                    };
                    LOGGER.info("Use the following devices in NML config: "
                            + JsonPrettyPrinter.prettyPrint(androidTargets));
                    context.addTestState(APPLITOOLS.NML_CONFIG, androidTargets);
                    break;
                default:
                    LOGGER.info("Skipping NML config for platform: " + currentPlatform);
                    break;
            }
        }
    }
}
