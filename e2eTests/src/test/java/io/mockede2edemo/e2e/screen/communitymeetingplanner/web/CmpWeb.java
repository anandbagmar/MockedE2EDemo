package io.mockede2edemo.e2e.screen.communitymeetingplanner.web;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.znsio.teswiz.runner.Driver;

/**
 * Shared web helpers for the Community Meeting Planner web screens.
 *
 * The webapp exposes the same {@code data-testid} values as the mobile
 * {@code testID} scheme, so every web screen locates elements via
 * {@code [data-testid='...']} CSS selectors driven through the teswiz
 * {@link Driver}.
 */
final class CmpWeb {

    static final int DEFAULT_TIMEOUT = 20;

    private CmpWeb() {
    }

    static By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }

    static WebElement waitVisible(Driver driver, String testId) {
        return waitVisible(driver, testId, DEFAULT_TIMEOUT);
    }

    static WebElement waitVisible(Driver driver, String testId, int timeoutSeconds) {
        return driver.waitTillElementIsVisible(byTestId(testId), timeoutSeconds);
    }

    static void click(Driver driver, String testId) {
        driver.waitForClickabilityOf(byTestId(testId), DEFAULT_TIMEOUT).click();
    }

    static void waitInvisible(Driver driver, String testId) {
        new WebDriverWait(driver.getInnerDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.invisibilityOfElementLocated(byTestId(testId)));
    }
}
