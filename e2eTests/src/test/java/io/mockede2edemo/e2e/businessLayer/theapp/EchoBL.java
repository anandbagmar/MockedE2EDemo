package io.mockede2edemo.e2e.businessLayer.theapp;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import io.mockede2edemo.e2e.entities.MOCKEDE2EDEMO_TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import io.mockede2edemo.e2e.screen.theapp.AppLaunchScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

public class EchoBL {
    private static final Logger LOGGER = LogManager.getLogger(EchoBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public EchoBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public EchoBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = MOCKEDE2EDEMO_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.getPlatform();
    }

    public EchoBL echoMessage(String message) {
        AppLaunchScreen.get().selectEcho().echoMessage(message);
        return this;
    }
}
