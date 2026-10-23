package vn.edu.softwaretesting.pages;

import java.math.BigDecimal;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object cho quy trinh thanh toan (FR-03):
 * checkout-step-one.html -> checkout-step-two.html -> checkout-complete.html.
 *
 * Phan luong chinh, Cancel va doc tien/thue: Dang Khoa (TV3).
 * Phan bat loi validate (ERROR, continueExpectingError): cua Tran An (TV4), gop vao day
 * de khi merge chi can giu ban nay.
 * Giu nguyen constructor public CheckoutPage(WebDriver) de CartPage.checkout() tuong thich.
 */
public class CheckoutPage {

    // Buoc 1: nhap thong tin
    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By CANCEL = By.id("cancel");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    // Buoc 2: tong quan don hang
    private static final By SUBTOTAL = By.cssSelector(".summary_subtotal_label");
    private static final By TAX = By.cssSelector(".summary_tax_label");
    private static final By TOTAL = By.cssSelector(".summary_total_label");
    private static final By FINISH = By.id("finish");

    // Buoc 3: hoan tat
    private static final By COMPLETE_HEADER = By.cssSelector(".complete-header");

    private static final By BADGE = By.cssSelector(".shopping_cart_badge");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    /** Nhap First Name, Last Name, Postal Code (chuoi rong "" = de trong). */
    public CheckoutPage fillInformation(String first, String last, String postal) {
        type(FIRST_NAME, first);
        type(LAST_NAME, last);
        type(POSTAL, postal);
        return this;
    }

    /** Nhan Continue va cho trang tong quan checkout-step-two.html hien ra. */
    public CheckoutPage continueToOverview() {
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL));
        return this;
    }

    /** (TV4) Nhan Continue khi thieu thong tin va tra ve noi dung thong bao loi. */
    public String continueExpectingError() {
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE)).click();
        return readText(ERROR);
    }

    /** Giu tuong thich voi Listing 2 cua de bai: tra ve chuoi "Total: $xx.xx". */
    public String continueToOverviewAndReadTotal() {
        return continueToOverview().readText(TOTAL);
    }

    /** Doc Item total, Tax, Total tren trang tong quan. */
    public OrderSummary readSummary() {
        return new OrderSummary(
                parseAmount(readText(SUBTOTAL)),
                parseAmount(readText(TAX)),
                parseAmount(readText(TOTAL)));
    }

    /** Nhan Finish va tra ve thong bao hoan tat (vd "Thank you for your order!"). */
    public String finishAndGetMessage() {
        wait.until(ExpectedConditions.elementToBeClickable(FINISH)).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        return readText(COMPLETE_HEADER);
    }

    /** Nhan Cancel o buoc nhap thong tin, quay ve cart.html. */
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
        field.sendKeys(value == null ? "" : value); // o CSV trong -> null (TV4)
    }

    private String readText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    /** "Item total: $29.99" / "Tax: $2.40" / "Total: $32.39" -> 29.99 / 2.40 / 32.39 */
    static BigDecimal parseAmount(String label) {
        return new BigDecimal(label.substring(label.indexOf('$') + 1).trim());
    }

    public record OrderSummary(BigDecimal itemTotal, BigDecimal tax, BigDecimal total) { }
}
