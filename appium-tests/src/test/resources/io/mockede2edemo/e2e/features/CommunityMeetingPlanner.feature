@mockede2edemo
Feature: Scenarios for "Community Meeting Planner" app

  # IS_VISUAL=true CONFIG=./configs/mockede2edemo/mockede2edemo_local_android_config.properties PLATFORM=android ./gradlew runTeswiz
  # CONFIG=./configs/mockede2edemo/mockede2edemo_local_ios_config.properties PLATFORM=iOS ./gradlew runTeswiz
  @communityMeetingPlanner @android @iOS
  Scenario: Verify_Community_Meeting_Planner_workflow
    Given I launch the Community Meeting Planner app
    When I run the Community Meeting Planner workflow
    Then I should be back on the home screen
