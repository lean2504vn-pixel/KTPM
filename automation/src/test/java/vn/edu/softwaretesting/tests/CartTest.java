package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CartPage;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.InventoryPage.ProductInfo;
import vn.edu.softwaretesting.pages.LoginPage;

@Tag("web")
@Tag("FR-02")
@DisplayName("FR-02: Quan ly gio hang")
class CartTest extends WebTestBase {

    private static final String BACKPACK = "sauce-labs-backpack";
    private static final String BIKE_LIGHT = "sauce-labs-bike-light";
    private InventoryPage inventory;

    @BeforeEach
    void loginWithCleanCart() {
        var result = new LoginPage(driver, baseUrl).open()
                .login("standard_user", "secret_sauce");
        assertTrue(result.accepted(), "Tien dieu kien: dang nhap standard_user thanh cong");
        inventory = new InventoryPage(driver);
    }

    @Test
    @DisplayName("TC-CART-B1: 0 san pham - khong hien badge")
    void emptyCartHasNoBadge() {
        assertFalse(inventory.isBadgeDisplayed());
        CartPage cart = inventory.openCart();
        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(0, cart.getCartItemCount());
    }

    @ParameterizedTest(name = "{0}: badge={1}")
    @CsvSource({
            "TC-CART-B2, 1",
            "TC-CART-B3, 5",
            "TC-CART-B4, 6"
    })
    @DisplayName("TC-CART-B2/B3/B4: cac bien 1, 5, 6")
    void badgeMatchesBoundary(String testId, int expected) {
        if (expected == 5) {
            inventory.addFirstNProducts(6);
          
            inventory.removeFromCart(BACKPACK);
        } else {
            inventory.addFirstNProducts(expected);
        }
        assertEquals(expected, inventory.getBadgeCount(), testId);
        assertEquals(Integer.toString(expected), inventory.getBadgeText(), testId);
        CartPage cart = inventory.openCart();
        assertEquals(expected, cart.getCartItemCount(), testId);
    }

    @Test
    @DisplayName("TC-CART-B5: 1 -> 0 san pham - badge bien mat")
    void removeLastProductHidesBadge() {
        inventory.addToCart(BACKPACK);
        assertEquals(1, inventory.getBadgeCount());
        inventory.removeFromCart(BACKPACK);
        assertFalse(inventory.isBadgeDisplayed());
        assertEquals(0, inventory.openCart().getCartItemCount());
    }

    @Test
    @DisplayName("TC-CART-UC1: them hai san pham, ten va gia trong gio khop Inventory")
    void cartShowsSameProductsAndPrices() {
        ProductInfo backpack = inventory.getProductInfo(BACKPACK);
        ProductInfo light = inventory.getProductInfo(BIKE_LIGHT);
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);

        CartPage cart = inventory.openCart();
        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(2, cart.getCartItemCount());
        List<CartPage.CartItem> items = cart.getCartItems();
        assertTrue(items.stream().anyMatch(i -> i.name().equals(backpack.name())
                && i.price().compareTo(backpack.price()) == 0 && i.quantity() == 1));
        assertTrue(items.stream().anyMatch(i -> i.name().equals(light.name())
                && i.price().compareTo(light.price()) == 0 && i.quantity() == 1));
    }

    @Test
    @DisplayName("TC-CART-UC2: Remove tai Inventory - badge giam tu 2 xuong 1")
    void removeOnInventoryReducesBadge() {
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);
        assertEquals(2, inventory.getBadgeCount());
        inventory.removeFromCart(BACKPACK);
        assertEquals(1, inventory.getBadgeCount());
        CartPage cart = inventory.openCart();
        assertEquals(1, cart.getCartItemCount());
        assertFalse(cart.containsProduct("Sauce Labs Backpack"));
    }

    @Test
    @DisplayName("TC-CART-UC3: Remove trong Cart - san pham bien mat")
    void removeFromCartPageRemovesProduct() {
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);
        CartPage cart = inventory.openCart();
        assertEquals(2, cart.getCartItemCount());
        cart.removeProduct(BACKPACK);
        assertEquals(1, cart.getCartItemCount());
        assertFalse(cart.containsProduct("Sauce Labs Backpack"));
        assertEquals(1, cart.getBadgeCount());
    }

    @Test
    @DisplayName("TC-CART-UC4: Continue Shopping - quay Inventory, giu nguyen gio")
    void continueShoppingPreservesCart() {
        inventory.addToCart(BACKPACK);
        CartPage cart = inventory.openCart();
        assertEquals(1, cart.getCartItemCount());
        InventoryPage returned = cart.continueShopping();
        assertTrue(returned.currentUrl().contains("inventory.html"));
        assertEquals(1, returned.getBadgeCount());
        assertTrue(returned.openCart().containsProduct("Sauce Labs Backpack"));
    }
}