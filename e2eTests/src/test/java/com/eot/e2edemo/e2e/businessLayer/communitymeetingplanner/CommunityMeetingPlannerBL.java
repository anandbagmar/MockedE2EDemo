package com.eot.e2edemo.e2e.businessLayer.communitymeetingplanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Runner;

import com.eot.e2edemo.e2e.entities.MOCKEDE2EDEMO_TEST_CONTEXT;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.HomeScreen;
import com.eot.e2edemo.e2e.screen.communitymeetingplanner.SummaryScreen;

/**
 * Business layer for the Community Meeting Planner workflow.
 *
 * Orchestrates the single, platform-agnostic journey and performs assertions.
 * Thanks to web feature parity (the webapp now renders the native journey and
 * native hybrid steps as standard web components), the workflow is a single
 * linear chain across android, iOS, and web - no platform branching:
 *
 * Home -> Planner -> NativeJourney -> NativeHybrid -> GuestLookup
 * -> WebChecklist -> Summary -> Home
 */
public class CommunityMeetingPlannerBL {
    private static final Logger LOGGER = LogManager.getLogger(CommunityMeetingPlannerBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    private static final String ATTENDEE_NAME = "Test Automation";
    private static final String UNIQUE_ID_PATTERN = "CMP-[A-Z0-9]+";
    private static final int INVALID_GUEST_COUNT = 20;
    private static final int VALID_GUEST_COUNT = 10;

    public CommunityMeetingPlannerBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public CommunityMeetingPlannerBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = MOCKEDE2EDEMO_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.getPlatform();
    }

    public CommunityMeetingPlannerBL launch() {
        LOGGER.info("Launching Community Meeting Planner on " + currentPlatform);
        HomeScreen.get().waitForScreen();
        return this;
    }

    public CommunityMeetingPlannerBL runWorkflow() {
        LOGGER.info("Running Community Meeting Planner workflow on " + currentPlatform);

        SummaryScreen summary = HomeScreen.get()
                .enterName(ATTENDEE_NAME)
                .startPlanner(isAlternateFlow())
                .proceedToNativeJourney()
                .continueToHybrid()
                .continueToGuestLookup()
                .fetchProfilesExpectingValidationError(INVALID_GUEST_COUNT)
                .dismissValidationAlert()
                .loadProfiles(VALID_GUEST_COUNT)
                .openChecklist()
                .markChecklistReady()
                .completeWorkflow();

        verifySummary(summary);
        summary.restart();
        return this;
    }

    public CommunityMeetingPlannerBL verifyBackOnHome() {
        // restart() already waits for (and, on mobile, checkpoints) the home
        // screen, so here we only assert it is displayed.
        boolean atHome = HomeScreen.get().isAtHome();
        softly.assertThat(atHome).as("Should be back on the home screen").isTrue();
        return this;
    }

    /**
     * The web summary surfaces the entered name and a generated unique id; the
     * mobile summary does not expose these as readable text, so those screens
     * return empty strings and the assertions are skipped.
     */
    private void verifySummary(SummaryScreen summary) {
        String thankYou = summary.getThankYouText();
        if (thankYou != null && !thankYou.isBlank()) {
            softly.assertThat(thankYou)
                    .as("Thank-you message should contain the entered name")
                    .contains(ATTENDEE_NAME);
        }
        String uniqueId = summary.getUniqueId();
        if (uniqueId != null && !uniqueId.isBlank()) {
            LOGGER.info("Summary shows uniqueId=" + uniqueId);
            softly.assertThat(uniqueId)
                    .as("Unique id should match CMP-XXXX format")
                    .matches(UNIQUE_ID_PATTERN);
        }
    }

    private boolean isAlternateFlow() {
        return "true".equalsIgnoreCase(System.getenv("USE_ALTERNATE_FLOW"))
                || "true".equalsIgnoreCase(System.getProperty("USE_ALTERNATE_FLOW"));
    }
}
