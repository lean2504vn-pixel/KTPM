package vn.edu.softwaretesting.pages;

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
 * Page Object dành cho các kiểm thử điều tra hiển thị và trạng thái giỏ hàng.
 * Chỉ đọc/thao tác giao diện, không chứa assertion JUnit.
 */
public final class ProblemProductPage {
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By INVENTORY_ITEMS = By.cssSelector(".inventory_item");
    private static final By INVENTORY_NAME = By.cssSelector(".inventory_item_name");
    private static final By INVENTORY_IMAGE = By.cssSelector(".inventory_item_img img");
    private static final By DETAIL_CONTAINER = By.cssSelector(".inventory_details_container");
    private static final By DETAIL_NAME = By.cssSelector(".inventory_details_name");
    private static final By DETAIL_PRICE = By.cssSelector(".inventory_details_price");
    private static final By DETAIL_IMAGE = By.cssSelector(".inventory_details_img");
    // Detail page buttons often have IDs exactly "add-to-cart" / "remove".
    // Inventory buttons have product-specific IDs such as "add-to-cart-sauce-labs-backpack".
    private static final By DETAIL_ADD = By.cssSelector(
            ".inventory_details_container button[id='add-to-cart'], "
          + ".inventory_details_container button[id^='add-to-cart-'], "
          + ".inventory_details_container button[data-test='add-to-cart']");
    private static final By DETAIL_REMOVE = By.cssSelector(
            ".inventory_details_container button[id='remove'], "
          + ".inventory_details_container button[id^='remove-'], "
          + ".inventory_details_container button[data-test='remove']");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
    private static final By BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CART_ITEMS = By.cssSelector(".cart_item");
    private static final By CART_ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By BACK_TO_PRODUCTS = By.id("back-to-products");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ProblemProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    public ProblemProductPage waitForInventory() {
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

    public ProblemProductPage openDetail(String displayedName) {
        waitForInventory();
        WebElement item = driver.findElements(INVENTORY_ITEMS).stream()
                .filter(e -> Objects.equals(e.findElement(INVENTORY_NAME).getText(), displayedName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong co item tai Inventory: " + displayedName));
        item.findElement(INVENTORY_NAME).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_CONTAINER));
        return this;
    }

    /**

     * Điều hướng đến một ID sản phẩm cụ thể đã biết để cô lập hành vi giỏ hàng khỏi

     * các liên kết sản phẩm bị lỗi (được đề cập riêng trong BUG-07 / BUG-09).

     * Trên dữ liệu mẫu SauceDemo: Fleece Jacket id=5; T-Shirt (Red) id=3.

     * Đây là một phương pháp KIỂM TRA CÔ LẬP, không phải là bản sửa lỗi cho ứng dụng.

     */
    public ProblemProductPage openDetailById(int productId) {
        URI current = URI.create(driver.getCurrentUrl());
        String base = current.getScheme() + "://" + current.getAuthority();
        driver.get(base + "/inventory-item.html?id=" + productId);
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

    public ProblemProductPage backToProducts() {
        wait.until(ExpectedConditions.elementToBeClickable(BACK_TO_PRODUCTS)).click();
        return waitForInventory();
    }

    public String buttonStateInDetail() {
        if (!driver.findElements(DETAIL_REMOVE).isEmpty()) return "REMOVE";
        if (!driver.findElements(DETAIL_ADD).isEmpty()) return "ADD";
        return "MISSING";
    }

    /** Nut Remove tai trang chi tiet; khong kiem tra assertion trong Page Object. */
    public void clickDetailRemove() {
        wait.until(ExpectedConditions.elementToBeClickable(DETAIL_REMOVE)).click();
    }

    public void clickDetailAdd() {
        wait.until(ExpectedConditions.elementToBeClickable(DETAIL_ADD)).click();
    }

    public void clickInventoryAdd(String displayedName) {
        waitForInventory();
        WebElement item = driver.findElements(INVENTORY_ITEMS).stream()
                .filter(e -> Objects.equals(e.findElement(INVENTORY_NAME).getText(), displayedName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong co item tai Inventory: " + displayedName));
        wait.until(ExpectedConditions.elementToBeClickable(
                item.findElement(By.cssSelector("button[id^='add-to-cart-']")))).click();
    }

    public String buttonStateAtInventory(String displayedName) {
        waitForInventory();
        WebElement item = driver.findElements(INVENTORY_ITEMS).stream()
                .filter(e -> Objects.equals(e.findElement(INVENTORY_NAME).getText(), displayedName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong co item tai Inventory: " + displayedName));
        List<WebElement> buttons = item.findElements(By.cssSelector("button[id^='add-to-cart-'], button[id^='remove-']"));
        if (buttons.isEmpty()) return "MISSING";
        return buttons.get(0).getAttribute("id").startsWith("remove-") ? "REMOVE" : "ADD";
    }

    public int badgeCount() {
        List<WebElement> badges = driver.findElements(BADGE);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText().trim());
    }

    public ProblemProductPage openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(CART_LINK)).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_contents_container")));
        return this;
    }

    /**

     * Điều hướng trực tiếp đến Giỏ hàng để thực hiện các bài kiểm tra CHẨN ĐOÁN về đồng bộ hóa trạng thái.

     * Thao tác này cố ý bỏ qua biểu tượng/liên kết giỏ hàng vì vấn đề người dùng gặp phải (problem_user)

     * việc điều hướng có thể thất bại sau khi quay lại từ trang chi tiết sản phẩm.

     * QUAN TRỌNG: Thao tác này KHÔNG xác minh rằng biểu tượng giỏ hàng hoạt động.

     */

    public ProblemProductPage openCartDirectly() {
        URI current = URI.create(driver.getCurrentUrl());
        String base = current.getScheme() + "://" + current.getAuthority();
        driver.get(base + "/cart.html");
        wait.until(ExpectedConditions.urlContains("cart.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_contents_container")));
        return this;
    }

    /** So dong san pham that su dang hien trong cart.html (khong doc badge). */
    public int cartItemCount() {
        return driver.findElements(CART_ITEMS).size();
    }

    public boolean cartContains(String name) {
        return driver.findElements(CART_ITEMS).stream()
                .map(e -> e.findElement(CART_ITEM_NAME).getText())
                .anyMatch(name::equals);
    }

    /** Xoa san pham theo ten tai Cart, danh cho buoc kiem tra ket thuc BUG-10. */
    public void clickCartRemove(String name) {
        WebElement item = driver.findElements(CART_ITEMS).stream()
                .filter(e -> Objects.equals(e.findElement(CART_ITEM_NAME).getText(), name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cart khong chua: " + name));
        wait.until(ExpectedConditions.elementToBeClickable(
                item.findElement(By.cssSelector("button[id^='remove-']")))).click();
    }

    public record ProductTile(String name, String imageSrc) { }
    public record DetailInfo(String name, String price, String imageSrc, String url) { }
}
