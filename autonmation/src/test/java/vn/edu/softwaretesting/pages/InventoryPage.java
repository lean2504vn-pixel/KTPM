package vn.edu.softwaretesting.pages;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object trang Inventory. Chỉ đóng gói các thao tác UI, không chứa assertion.
 * Gia Bảo có thể thêm các hàm sortBy/names/prices vào chính lớp này khi merge.
 */
public class InventoryPage {
    private static final By INVENTORY = By.id("inventory_container");
    private static final By ADD_BUTTONS = By.cssSelector("button[id^='add-to-cart-']");
    private static final By BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
    private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICE = By.cssSelector(".inventory_item_price");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY));
    }

    public InventoryPage addToCart(String productSlug) {
        By add = By.id("add-to-cart-" + productSlug);
        wait.until(ExpectedConditions.elementToBeClickable(add)).click();
        // Xác nhận trạng thái Add -> Remove thay vì chờ một khoảng thời gian cố định.
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("remove-" + productSlug)));
        return this;
    }

    public InventoryPage removeFromCart(String productSlug) {
        By remove = By.id("remove-" + productSlug);
        wait.until(ExpectedConditions.elementToBeClickable(remove)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-" + productSlug)));
        return this;
    }

    /**
     * Chỉ sử dụng với trạng thái giỏ trống trong kiểm thử BVA.
     * Tìm lại button sau mỗi lần click nhằm tránh StaleElementReferenceException.
     */
    public InventoryPage addFirstNProducts(int n) {
        if (n < 0 || n > 6) {
            throw new IllegalArgumentException("So san pham phai nam trong [0, 6]: " + n);
        }
        wait.until(ExpectedConditions.numberOfElementsToBe(ADD_BUTTONS, 6));
        for (int i = 0; i < n; i++) {
            List<WebElement> buttons = driver.findElements(ADD_BUTTONS);
            String id = buttons.get(0).getAttribute("id");
            String slug = id.substring("add-to-cart-".length());
            addToCart(slug);
            int remaining = 6 - i - 1;
            wait.until(ExpectedConditions.numberOfElementsToBe(ADD_BUTTONS, remaining));
        }
        return this;
    }

    public boolean isBadgeDisplayed() {
        return driver.findElements(BADGE).stream().anyMatch(WebElement::isDisplayed);
    }

    public String getBadgeText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(BADGE)).getText();
    }

    public int getBadgeCount() {
        return isBadgeDisplayed() ? Integer.parseInt(getBadgeText()) : 0;
    }

    public ProductInfo getProductInfo(String slug) {
        By button = By.cssSelector("button[id='add-to-cart-" + slug + "'],button[id='remove-" + slug + "']");
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(button));
        WebElement item = element.findElement(By.xpath("./ancestor::*[contains(concat(' ', normalize-space(@class), ' '), ' inventory_item ')]"));
        String name = item.findElement(ITEM_NAME).getText();
        BigDecimal price = parsePrice(item.findElement(ITEM_PRICE).getText());
        return new ProductInfo(slug, name, price);
    }

    public CartPage openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(CART_LINK)).click();
        return new CartPage(driver);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private static BigDecimal parsePrice(String text) {
        return new BigDecimal(text.replace("$", "").trim());
    }

    public record ProductInfo(String slug, String name, BigDecimal price) { }
}
