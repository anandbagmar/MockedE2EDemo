package io.mockede2edemo.tests;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumDriver;

public class Wait {

    private static final int DEFAULT_TIMEOUT_SECONDS = 15;

    private Wait() {}

    /** Pause for {@code seconds} seconds (use sparingly). */
    public static void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Wait until an element is visible and return it. */
    public static WebElement waitTillElementIsPresent(AppiumDriver driver, By locator) {
        return waitTillElementIsPresent(driver, locator, DEFAULT_TIMEOUT_SECONDS);
    }

    /** Wait until an element is visible and return it (custom timeout). */
    public static WebElement waitTillElementIsPresent(AppiumDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Wait until an element exists in the DOM and return it. */
    public static WebElement waitTillElementExists(AppiumDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /** Wait until an element exists in the DOM and return it (custom timeout). */
    public static WebElement waitTillElementExists(AppiumDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /** Wait until an element is clickable and return it. */
    public static WebElement waitTillElementIsClickable(AppiumDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Wait until an element is clickable and return it (custom timeout). */
    public static WebElement waitTillElementIsClickable(AppiumDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Wait until an element is no longer visible. */
    public static void waitTillElementDisappears(AppiumDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** Wait until a native alert is present and return it. */
    public static Alert waitTillAlertIsPresent(AppiumDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        return wait.until(ExpectedConditions.alertIsPresent());
    }
}
