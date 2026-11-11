package vn.edu.softwaretesting.pages;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object dùng chung cho FR-02 (giỏ hàng) và các bước điều tra BUG
 * tại Inventory/Detail/Cart với problem_user.
 * Chỉ thao tác và đọc UI, không chứa assertion JUnit.
 *
 * Lưu ý: không đợi Cart trong constructor vì test BUG cần khởi tạo
 * từ Inventory. Các phương thức dành cho Cart tự đợi cart_contents_container.
 */
public class CartPage {
    // Cart
    private static final By CART_CONTENTS = By.id("cart_contents_container");
    private static final By CART_ITEMS = By.cssSelector(".cart_item");
    private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICE = By.cssSelector(".inventory_item_price");
    private static final By ITEM_QUANTITY = By.cssSelector(".cart_quantity");
    private static final By BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
    private static final By CONTINUE_SHOPPING = By.id("continue-shopping");
    private static final By CHECKOUT = By.id("checkout");

    // Inventory
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By INVENTORY_ITEMS = By.cssSelector(".inventory_item");
    private static final By INVENTORY_NAME = By.cssSelector(".inventory_item_name");
    private static final By INVENTORY_IMAGE = By.cssSelector(".inventory_item_img img");

    // Detail
    private static final By DETAIL_CONTAINER = By.cssSelector(".inventory_details_container");
    private static final By DETAIL_NAME = By.cssSelector(".inventory_details_name");
    private static final By DETAIL_PRICE = By.cssSelector(".inventory_details_price");
    private static final By DETAIL_IMAGE = By.cssSelector(".inventory_details_img");
    private static final By DETAIL_ADD = By.cssSelector(
            ".inventory_details_container button[id='add-to-cart'], "
                    + ".inventory_details_container button[id^='add-to-cart-'], "
                    + ".inventory_details_container button[data-test='add-to-cart']");
    private static final By DETAIL_REMOVE = By.cssSelector(
            ".inventory_details_container button[id='remove'], "
                    + ".inventory_details_container button[id^='remove-'], "
                    + ".inventory_details_container button[data-test='remove']");
    private static final By BACK_TO_PRODUCTS = By.id("back-to-products");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CartPage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver");
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    // ===== Cart: giữ giao diện API cho standard_user / FR-03 =====

    public CartPage waitForCart() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_CONTENTS));
        return this;
    }

    public int getCartItemCount() {
        waitForCart();
        return driver.findElements(CART_ITEMS).size();
    }

    public List<CartItem> getCartItems() {
        waitForCart();
        return driver.findElements(CART_ITEMS).stream().map(item -> new CartItem(
                item.findElement(ITEM_NAME).getText(),
                new BigDecimal(item.findElement(ITEM_PRICE).getText().replace("$", "").trim()),
                Integer.parseInt(item.findElement(ITEM_QUANTITY).getText().trim())
        )).toList();
    }

    public boolean containsProduct(String name) {
        waitForCart();
        return driver.findElements(CART_ITEMS).stream()
                .anyMatch(item -> Objects.equals(item.findElement(ITEM_NAME).getText(), name));
    }

    public CartPage removeFirstItem() {
        waitForCart();
        List<WebElement> items = driver.findElements(CART_ITEMS);
        if (items.isEmpty()) {
            throw new IllegalStateException("Khong co san pham de xoa");
        }
        String buttonId = items.get(0)
                .findElement(By.cssSelector("button[id^='remove-']")).getAttribute("id");
        return removeProduct(buttonId.substring("remove-".length()));
    }

    /** Xóa theo slug, ví dụ sauce-labs-backpack. */
    public CartPage removeProduct(String slug) {
        waitForCart();
        int before = getCartItemCount();
        if (before == 0) throw new IllegalStateException("Gio hang dang trong");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("remove-" + slug))).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(CART_ITEMS, before - 1));
        return this;
    }

    public int getBadgeCount() {
        List<WebElement> badges = driver.findElements(BADGE);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText().trim());
    }

    public boolean isBadgeDisplayed() {
        return driver.findElements(BADGE).stream().anyMatch(WebElement::isDisplayed);
    }

    public InventoryPage continueShopping() {
        waitForCart();
        wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_SHOPPING)).click();
        return new InventoryPage(driver);
    }

    /** Dùng chung với CheckoutPage/FR-03. */
    public CheckoutPage checkout() {
        waitForCart();
        wait.until(ExpectedConditions.elementToBeClickable(CHECKOUT)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        return new CheckoutPage(driver);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    // ===== Inventory/Detail: chuyển từ ProblemProductPage vào đây =====

    public CartPage waitForInventory() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        return this;
    }

    public List<ProductTile> productTiles() {
        waitForInventory();
        List<ProductTile> tiles = new ArrayList<>();
        for (WebElement item : driver.findElements(INVENTORY_ITEMS)) {
            String name = item.findElement(INVENTORY_NAME).getText();
            String imageSrc = item.findElement(INVENTORY_IMAGE).getAttribute("src");
            tiles.add(new ProductTile(name, imageSrc));
        }
        return tiles;
    }

    private WebElement inventoryItemByName(String displayedName) {
        waitForInventory();
        return driver.findElements(INVENTORY_ITEMS).stream()
                .filter(item -> Objects.equals(item.findElement(INVENTORY_NAME).getText(), displayedName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khong co san pham tai Inventory: " + displayedName));
    }

    /** Mở Detail bằng cách click vào tên sản phẩm, giữ đúng luồng người dùng. */
    public CartPage openDetail(String displayedName) {
        inventoryItemByName(displayedName).findElement(INVENTORY_NAME).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_CONTAINER));
        return this;
    }

    /** Đi thẳng tới ID để cô lập lỗi Detail khỏi lỗi link sản phẩm (khi cần). */
    public CartPage openDetailById(int productId) {
        driver.get(siteBaseUrl() + "/inventory-item.html?id=" + productId);
        wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_CONTAINER));
        return this;
    }

    public DetailInfo detailInfo() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_CONTAINER));
        return new DetailInfo(
                wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_NAME)).getText(),
                wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_PRICE)).getText(),
                wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_IMAGE)).getAttribute("src"),
                driver.getCurrentUrl());
    }

    public CartPage backToProducts() {
        wait.until(ExpectedConditions.elementToBeClickable(BACK_TO_PRODUCTS)).click();
        return waitForInventory();
    }

    /**
     * Điều hướng trực tiếp về Inventory để test BUG-10 không phụ thuộc
     * vào chức năng Back to Products (có thể lỗi riêng ở problem_user).
     * Vẫn giữ nguyên session của WebDriver.
     */
    public CartPage openInventoryDirectly() {
        driver.get(siteBaseUrl() + "/inventory.html");
        return waitForInventory();
    }

    public String buttonStateInDetail() {
        if (!driver.findElements(DETAIL_REMOVE).isEmpty()) return "REMOVE";
        if (!driver.findElements(DETAIL_ADD).isEmpty()) return "ADD";
        return "MISSING";
    }

    public void clickDetailAdd() {
        wait.until(ExpectedConditions.elementToBeClickable(DETAIL_ADD)).click();
    }

    public void clickDetailRemove() {
        wait.until(ExpectedConditions.elementToBeClickable(DETAIL_REMOVE)).click();
    }

    public void clickInventoryAdd(String displayedName) {
        WebElement item = inventoryItemByName(displayedName);
        wait.until(ExpectedConditions.elementToBeClickable(
                item.findElement(By.cssSelector("button[id^='add-to-cart-']")))).click();
    }

    public String buttonStateAtInventory(String displayedName) {
        WebElement item = inventoryItemByName(displayedName);
        List<WebElement> buttons = item.findElements(
                By.cssSelector("button[id^='add-to-cart-'], button[id^='remove-']"));
        if (buttons.isEmpty()) return "MISSING";
        return buttons.get(0).getAttribute("id").startsWith("remove-") ? "REMOVE" : "ADD";
    }

    // ===== Tương thích API cũ của ProblemProductPage =====

    public int badgeCount() {
        return getBadgeCount();
    }

    /** Mở Cart bằng biểu tượng giỏ hàng. */
    public CartPage openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(CART_LINK)).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        return waitForCart();
    }

    /**
     * Mở Cart trực tiếp cho test chẩn đoán; không xác nhận icon/link Cart hoạt động.
     */
    public CartPage openCartDirectly() {
        driver.get(siteBaseUrl() + "/cart.html");
        wait.until(ExpectedConditions.urlContains("cart.html"));
        return waitForCart();
    }

    public int cartItemCount() {
        return getCartItemCount();
    }

    public boolean cartContains(String name) {
        return containsProduct(name);
    }

    /** Xóa trong Cart theo tên hiển thị, dùng cho BUG-10. */
    public void clickCartRemove(String name) {
        waitForCart();
        WebElement item = driver.findElements(CART_ITEMS).stream()
                .filter(e -> Objects.equals(e.findElement(ITEM_NAME).getText(), name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cart khong chua: " + name));
        String removeId = item.findElement(By.cssSelector("button[id^='remove-']"))
                .getAttribute("id");
        removeProduct(removeId.substring("remove-".length()));
    }

    private String siteBaseUrl() {
        URI uri = URI.create(driver.getCurrentUrl());
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    public record CartItem(String name, BigDecimal price, int quantity) { }
    public record ProductTile(String name, String imageSrc) { }
    public record DetailInfo(String name, String price, String imageSrc, String url) { }
}
