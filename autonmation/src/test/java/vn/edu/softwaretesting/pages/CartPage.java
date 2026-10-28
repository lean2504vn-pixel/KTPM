package vn.edu.softwaretesting.pages;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {
    private static final By CART_CONTENTS = By.id("cart_contents_container");
    private static final By CART_ITEMS = By.cssSelector(".cart_item");
    private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICE = By.cssSelector(".inventory_item_price");
    private static final By ITEM_QUANTITY = By.cssSelector(".cart_quantity");
    private static final By BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CONTINUE_SHOPPING = By.id("continue-shopping");
    private static final By CHECKOUT = By.id("checkout");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_CONTENTS));
    }

    public int getCartItemCount() {
        return driver.findElements(CART_ITEMS).size();
    }

    public List<CartItem> getCartItems() {
        return driver.findElements(CART_ITEMS).stream().map(item -> new CartItem(
                item.findElement(ITEM_NAME).getText(),
                new BigDecimal(item.findElement(ITEM_PRICE).getText().replace("$", "").trim()),
                Integer.parseInt(item.findElement(ITEM_QUANTITY).getText().trim()))).toList();
    }

    public boolean containsProduct(String name) {
        return getCartItems().stream().anyMatch(item -> item.name().equals(name));
    }

    public CartPage removeFirstItem() {
        List<WebElement> items = driver.findElements(CART_ITEMS);
        if (items.isEmpty()) {
            throw new IllegalStateException("Khong co san pham de xoa");
        }
        String buttonId = items.get(0).findElement(By.cssSelector("button[id^='remove-']"))
                .getAttribute("id");
        return removeProduct(buttonId.substring("remove-".length()));
    }

    public CartPage removeProduct(String slug) {
        int before = getCartItemCount();
        if (before == 0) {
            throw new IllegalStateException("Gio hang dang trong");
        }
        By removeButton = By.id("remove-" + slug);
        wait.until(ExpectedConditions.elementToBeClickable(removeButton)).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(CART_ITEMS, before - 1));
        return this;
    }

    public int getBadgeCount() {
        List<WebElement> badges = driver.findElements(BADGE);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    public boolean isBadgeDisplayed() {
        return driver.findElements(BADGE).stream().anyMatch(WebElement::isDisplayed);
    }

    public InventoryPage continueShopping() {
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_SHOPPING)).click();
        return new InventoryPage(driver);
    }

    public CheckoutPage checkout() {
        wait.until(ExpectedConditions.elementToBeClickable(CHECKOUT)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        return new CheckoutPage(driver);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public record CartItem(String name, BigDecimal price, int quantity) {
    }
}
