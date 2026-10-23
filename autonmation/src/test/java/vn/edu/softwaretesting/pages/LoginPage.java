package vn.edu.softwaretesting.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class LoginPage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By INVENTORY = By.id("inventory_container");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public LoginPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    public LoginPage open() {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
        return this;
    }

    public LoginResult login(String username, String password) {
        driver.findElement(USERNAME).sendKeys(username);
        driver.findElement(PASSWORD).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(INVENTORY),
                    ExpectedConditions.visibilityOfElementLocated(ERROR)));
        } catch (TimeoutException exception) {
            return new LoginResult(false, driver.getCurrentUrl(),
                    "No success or error state appeared within the timeout");
        }

        boolean accepted = !driver.findElements(INVENTORY).isEmpty();
        String message = accepted
                ? "Login accepted"
                : driver.findElement(ERROR).getText();
        return new LoginResult(accepted, driver.getCurrentUrl(), message);
    }

    public record LoginResult(boolean accepted, String currentUrl,
                              String message) {
    }
}
