package com.eot.e2edemo.e2e.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import com.eot.e2edemo.e2e.businessLayer.communitymeetingplanner.CommunityMeetingPlannerBL;
import com.eot.e2edemo.e2e.entities.MOCKEDE2EDEMO_TEST_CONTEXT;

/**
 * Step definitions for the Community Meeting Planner feature.
 *
 * Following the teswiz architecture, the step layer is intentionally thin: it
 * only creates the driver and delegates to the business layer. All UI logic
 * lives in {@link CommunityMeetingPlannerBL} and the screen objects.
 */
public class CommunityMeetingPlannerSteps {
    private static final Logger LOGGER = LogManager.getLogger(CommunityMeetingPlannerSteps.class);
    private final TestExecutionContext context;

    public CommunityMeetingPlannerSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
    }

    @Given("I launch the Community Meeting Planner app")
    public void iLaunchTheCommunityMeetingPlannerApp() {
        Drivers.createDriverFor(MOCKEDE2EDEMO_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new CommunityMeetingPlannerBL(MOCKEDE2EDEMO_TEST_CONTEXT.ME, Runner.getPlatform()).launch();
    }

    @When("I run the Community Meeting Planner workflow")
    public void iRunTheCommunityMeetingPlannerWorkflow() {
        new CommunityMeetingPlannerBL().runWorkflow();
    }

    @Then("I should be back on the home screen")
    public void iShouldBeBackOnTheHomeScreen() {
        new CommunityMeetingPlannerBL().verifyBackOnHome();
    }
}
