@mockede2edemo
Feature: Scenarios for "Community Meeting Planner" app

  # CONFIG=./configs/mockede2edemo/mockede2edemo_local_android_config.properties PLATFORM=android TAG=@app ./gradlew runTeswiz
  # CONFIG=./configs/mockede2edemo/mockede2edemo_local_ios_config.properties PLATFORM=iOS TAG=@app ./gradlew runTeswiz
  # CONFIG=./configs/mockede2edemo/mockede2edemo_local_web_config.properties PLATFORM=web TAG=@web ./gradlew runTeswiz
  @communityMeetingPlanner @android @iOS @web @app
  Scenario: Verify_Community_Meeting_Planner_workflow
    Given I launch the Community Meeting Planner app
    When I run the Community Meeting Planner workflow
    Then I should be back on the home screen

  # IS_VISUAL=true CONFIG=./configs/mockede2edemo/mockede2edemo_local_android_config.properties PLATFORM=android TAG=@figma ./gradlew runTeswiz
  # CONFIG=./configs/mockede2edemo/mockede2edemo_local_ios_config.properties PLATFORM=iOS TAG=@figma ./gradlew runTeswiz
  @communityMeetingPlanner @android @figma
  Scenario: Verify Community Meeting Planner workflow against Figma design
    Given I have my Figma design with app name "App Automation Playground", test name "Community Meeting Planner-5-11" and baseline name "Community Meeting Planner-5-11_427" available in Applitools
    When I launch the Community Meeting Planner app
    And I run the Community Meeting Planner workflow
    Then I should be back on the home screen
