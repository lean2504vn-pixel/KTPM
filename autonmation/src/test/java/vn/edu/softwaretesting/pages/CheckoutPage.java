package vn.edu.softwaretesting.pages;

import java.math.BigDecimal;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CheckoutPage {
    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By CANCEL = By.id("cancel");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    private static final By SUBTOTAL = By.cssSelector(".summary_subtotal_label");
    private static final By TAX = By.cssSelector(".summary_tax_label");
    private static final By TOTAL = By.cssSelector(".summary_total_label");
    private static final By FINISH = By.id("finish");

    private static final By COMPLETE_HEADER = By.cssSelector(".complete-header");

    private static final By BADGE = By.cssSelector(".shopping_cart_badge");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    public CheckoutPage fillInformation(String first, String last, String postal) {
        type(FIRST_NAME, first);
        type(LAST_NAME, last);
        type(POSTAL, postal);
        return this;
    }

    public CheckoutPage continueToOverview() {
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL));
        return this;
    }

    public String continueExpectingError() {
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE)).click();
        return readText(ERROR);
    }

    public String continueToOverviewAndReadTotal() {
        return continueToOverview().readText(TOTAL);
    }

    public OrderSummary readSummary() {
        return new OrderSummary(
                parseAmount(readText(SUBTOTAL)),
                parseAmount(readText(TAX)),
                parseAmount(readText(TOTAL)));
    }

    public String finishAndGetMessage() {
        wait.until(ExpectedConditions.elementToBeClickable(FINISH)).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        return readText(COMPLETE_HEADER);
    }

    public CartPage cancel() {
        wait.until(ExpectedConditions.elementToBeClickable(CANCEL)).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        return new CartPage(driver);
    }

    public boolean isCartBadgeDisplayed() {
        return driver.findElements(BADGE).stream().anyMatch(WebElement::isDisplayed);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private void type(By locator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value == null ? "" : value);
    }

    private String readText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    static BigDecimal parseAmount(String label) {
        return new BigDecimal(label.substring(label.indexOf('$') + 1).trim());
    }

    public record OrderSummary(BigDecimal itemTotal, BigDecimal tax, BigDecimal total) { }
}
