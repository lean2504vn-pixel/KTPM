package vn.edu.softwaretesting.pages;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

/** Page Object cho cart.html. */
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
                Integer.parseInt(item.findElement(ITEM_QUANTITY).getText().trim())
        )).toList();
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

    /** Dùng chung cho thành viên làm FR-03. */
    public CheckoutPage checkout() {
        wait.until(ExpectedConditions.elementToBeClickable(CHECKOUT)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        return new CheckoutPage(driver);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public record CartItem(String name, BigDecimal price, int quantity) { }

    // Page Object cho cac ca kiem thu loi problem_user (giu nguyen logic cu).
    /**
     * Page Object dành cho các kiểm thử điều tra hiển thị và trạng thái giỏ hàng.
     * Chỉ đọc/thao tác giao diện, không chứa assertion JUnit.
     */
    public static final class ProblemProductPage {
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
         * Navigate to an exact known product ID to isolate cart behavior from
         * broken product links (covered separately by BUG-07 / BUG-09).
         * On SauceDemo sample data: Fleece Jacket id=5; T-Shirt (Red) id=3.
         * This is a TEST ISOLATION route, not a fix to the application.
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
         * Navigate directly to Cart for DIAGNOSTIC tests of state synchronization.
         * This intentionally bypasses the cart icon/link because the problem_user
         * navigation can fail after returning from the detail page.
         * IMPORTANT: this does NOT verify that the cart icon works.
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
}
