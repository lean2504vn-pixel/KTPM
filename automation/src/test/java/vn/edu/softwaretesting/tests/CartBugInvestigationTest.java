package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;

@Tag("web")
@Tag(" known-bug")
@EnabledIfSystemProperty(named = "runBugProbe", matches = "true")
class CartBugInvestigationTest extends WebTestBase {

    @Test
    @DisplayName("TC-CART-PROB1 [BUG-02]: kiem tra them Fleece Jacket bang problem_user")
    void addingFleeceJacketMustWorkForProblemUser() {
        var result = new LoginPage(driver, baseUrl).open()
                .login("problem_user", "secret_sauce");
        assertTrue(result.accepted());
        InventoryPage inventory = new InventoryPage(driver);
        inventory.addToCart("sauce-labs-fleece-jacket");
        assertEquals(1, inventory.getBadgeCount(), "Sau khi Add badge phai la 1");
        assertTrue(inventory.openCart().containsProduct("Sauce Labs Fleece Jacket"));
    }
}