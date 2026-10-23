package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CartPage;
import vn.edu.softwaretesting.pages.CheckoutPage;
import vn.edu.softwaretesting.pages.CheckoutPage.OrderSummary;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;

/**
 * FR-03 Thanh toan - phan 1 (Dang Khoa - TV3): luong chinh, Cancel, BVA lam tron thue 8%.
 * Phan 2 (Tran An - TV4): TC-CHK-02..04 va bang quyet dinh so 2, them vao cung lop nay.
 *
 * Ket qua mong doi duoc tinh tu dac ta FR-03 (Tax = 8% Item total, lam tron 2 chu so),
 * KHONG chep tu man hinh.
 */
@Tag("web")
@Tag("FR-03")
@DisplayName("FR-03: Checkout")
class CheckoutTest extends WebTestBase {

    private static final String BACKPACK = "sauce-labs-backpack";            // $29.99
    private static final String BIKE_LIGHT = "sauce-labs-bike-light";        // $9.99
    private static final String BOLT_TSHIRT = "sauce-labs-bolt-t-shirt";     // $15.99
    private static final String FLEECE_JACKET = "sauce-labs-fleece-jacket";  // $49.99
    private static final String ONESIE = "sauce-labs-onesie";                // $7.99
    private static final String RED_TSHIRT = "test.allthethings()-t-shirt-(red)"; // $15.99

    private InventoryPage inventory;

    @BeforeEach
    void loginAsStandardUser() {
        var result = new LoginPage(driver, baseUrl).open()
                .login("standard_user", "secret_sauce");
        assertTrue(result.accepted(), "Tien dieu kien: dang nhap standard_user thanh cong");
        inventory = new InventoryPage(driver);
    }

    @Test
    @DisplayName("TC-CHK-01: main flow - 1 item, Tax 8%, Finish shows Thank you and empties cart")
    void mainFlowOneItem() {
        // Buoc 1: Add to cart Backpack -> badge = 1
        inventory.addToCart(BACKPACK);
        assertEquals(1, inventory.getBadgeCount());

        // Buoc 2: mo gio hang -> cart.html, 1 san pham
        CartPage cart = inventory.openCart();
        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(1, cart.getCartItemCount());

        // Buoc 3: Checkout -> checkout-step-one.html
        CheckoutPage checkout = cart.checkout();
        assertTrue(checkout.currentUrl().contains("checkout-step-one.html"));

        // Buoc 4: nhap thong tin, Continue -> checkout-step-two.html
        checkout.fillInformation("An", "Nguyen", "700000").continueToOverview();
        assertTrue(checkout.currentUrl().contains("checkout-step-two.html"));

        // Buoc 5: 29.99 x 8% = 2.3992 ~ 2.40; 29.99 + 2.40 = 32.39
        OrderSummary summary = checkout.readSummary();
        assertEquals(new BigDecimal("29.99"), summary.itemTotal(), "Item total");
        assertEquals(new BigDecimal("2.40"), summary.tax(), "Tax");
        assertEquals(new BigDecimal("32.39"), summary.total(), "Total");

        // Buoc 6: Finish -> checkout-complete.html, Thank you, khong con badge
        String message = checkout.finishAndGetMessage();
        assertEquals("Thank you for your order!", message);
        assertTrue(checkout.currentUrl().contains("checkout-complete.html"));
        assertFalse(checkout.isCartBadgeDisplayed(), "Gio hang phai rong sau khi Finish");
    }

    @Test
    @DisplayName("TC-CHK-05: Cancel at information step returns to cart.html, item kept")
    void cancelReturnsToCart() {
        inventory.addToCart(BACKPACK);
        CheckoutPage checkout = inventory.openCart().checkout();

        CartPage cart = checkout.cancel();

        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(1, cart.getCartItemCount());
        assertTrue(cart.containsProduct("Sauce Labs Backpack"));
        assertEquals(1, cart.getBadgeCount());
    }

    /**
     * BVA mien 2 - lam tron thue 8%. Moi gia deu co duoi .99 nen phan le cua thue
     * (tinh bang xu) chi phu thuoc so san pham n: 0.92, 0.84, 0.76, 0.68, 0.60, 0.52.
     * Luon >= 0.5 xu -> luon lam tron len; 6 san pham (0.52 xu) la diem sat bien nhat.
     */
    static Stream<Arguments> taxBoundaryCases() {
        return Stream.of(
                // 63.96 x 8% = 5.1168 -> 5.12; 63.96 + 5.12 = 69.08
                Arguments.of("TC-TAX-B1", List.of(BACKPACK, BIKE_LIGHT, BOLT_TSHIRT, ONESIE),
                        "63.96", "5.12", "69.08"),
                // 79.95 x 8% = 6.3960 -> 6.40; 79.95 + 6.40 = 86.35
                Arguments.of("TC-TAX-B2", List.of(BACKPACK, BIKE_LIGHT, BOLT_TSHIRT, ONESIE, RED_TSHIRT),
                        "79.95", "6.40", "86.35"),
                // 129.94 x 8% = 10.3952 -> 10.40; 129.94 + 10.40 = 140.34
                Arguments.of("TC-TAX-B3", List.of(BACKPACK, BIKE_LIGHT, BOLT_TSHIRT, FLEECE_JACKET, ONESIE, RED_TSHIRT),
                        "129.94", "10.40", "140.34"));
    }

    @ParameterizedTest(name = "{0}: {1} -> Item total ${2}, Tax ${3}, Total ${4}")
    @MethodSource("taxBoundaryCases")
    @DisplayName("TC-TAX-B1..B3: BVA lam tron thue 8%")
    void taxIsRoundedToTwoDecimals(String testId, List<String> products,
                                   String itemTotal, String tax, String total) {
        products.forEach(inventory::addToCart);
        assertEquals(products.size(), inventory.getBadgeCount(), testId + " badge");

        OrderSummary summary = inventory.openCart().checkout()
                .fillInformation("An", "Nguyen", "700000")
                .continueToOverview()
                .readSummary();

        assertEquals(new BigDecimal(itemTotal), summary.itemTotal(), testId + " Item total");
        assertEquals(new BigDecimal(tax), summary.tax(), testId + " Tax");
        assertEquals(new BigDecimal(total), summary.total(), testId + " Total");
    }
}
