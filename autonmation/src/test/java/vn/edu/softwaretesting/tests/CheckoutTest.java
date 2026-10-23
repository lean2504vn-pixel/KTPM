package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CartPage;
import vn.edu.softwaretesting.pages.CheckoutPage;
import vn.edu.softwaretesting.pages.CheckoutPage.OrderSummary;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;

@Tag("web")
@Tag("FR-03")
@DisplayName("FR-03: Checkout")
class CheckoutTest extends WebTestBase {
    private static final String BACKPACK = "sauce-labs-backpack";
    private static final String BIKE_LIGHT = "sauce-labs-bike-light";
    private static final String BOLT_TSHIRT = "sauce-labs-bolt-t-shirt";
    private static final String FLEECE_JACKET = "sauce-labs-fleece-jacket";
    private static final String ONESIE = "sauce-labs-onesie";
    private static final String RED_TSHIRT = "test.allthethings()-t-shirt-(red)";

    private InventoryPage inventory;

    @BeforeEach
    void loginAsStandardUser() {
        restartChromeWithoutPasswordPopup();
        var result = new LoginPage(driver, baseUrl).open()
                .login("standard_user", "secret_sauce");
        assertTrue(result.accepted(), "Tien dieu kien: dang nhap standard_user thanh cong");
        inventory = new InventoryPage(driver);
    }

    private void restartChromeWithoutPasswordPopup() {
        if (!(driver instanceof ChromeDriver)) {
            return;
        }
        driver.quit();
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-search-engine-choice-screen",
                "--disable-features=PasswordLeakDetection");
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().window().setSize(new Dimension(1440, 900));
    }

    @Test
    @DisplayName("TC-CHK-01: main flow - 1 item, Tax 8%, Finish shows Thank you and empties cart")
    void mainFlowOneItem() {
        inventory.addToCart(BACKPACK);
        assertEquals(1, inventory.getBadgeCount());

        CartPage cart = inventory.openCart();
        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(1, cart.getCartItemCount());

        CheckoutPage checkout = cart.checkout();
        assertTrue(checkout.currentUrl().contains("checkout-step-one.html"));

        checkout.fillInformation("An", "Nguyen", "700000").continueToOverview();
        assertTrue(checkout.currentUrl().contains("checkout-step-two.html"));

        OrderSummary summary = checkout.readSummary();
        assertEquals(new BigDecimal("29.99"), summary.itemTotal(), "Item total");
        assertEquals(new BigDecimal("2.40"), summary.tax(), "Tax");
        assertEquals(new BigDecimal("32.39"), summary.total(), "Total");

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

    static Stream<Arguments> taxBoundaryCases() {
        return Stream.of(

                Arguments.of("TC-TAX-B1", List.of(BACKPACK, BIKE_LIGHT, BOLT_TSHIRT, ONESIE),
                        "63.96", "5.12", "69.08"),

                Arguments.of("TC-TAX-B2", List.of(BACKPACK, BIKE_LIGHT, BOLT_TSHIRT, ONESIE, RED_TSHIRT),
                        "79.95", "6.40", "86.35"),

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
