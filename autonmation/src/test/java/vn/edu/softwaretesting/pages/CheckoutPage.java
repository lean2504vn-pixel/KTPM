package vn.edu.softwaretesting.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CheckoutPage {
    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    public CheckoutPage fillInformation(String first, String last, String postal) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME)).clear();
        driver.findElement(FIRST_NAME).sendKeys(first == null ? "" : first);

        driver.findElement(LAST_NAME).clear();
        driver.findElement(LAST_NAME).sendKeys(last == null ? "" : last);

        driver.findElement(POSTAL).clear();
        driver.findElement(POSTAL).sendKeys(postal == null ? "" : postal);

        return this;
    }

    public String continueExpectingError() {
        driver.findElement(CONTINUE).click();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }
}