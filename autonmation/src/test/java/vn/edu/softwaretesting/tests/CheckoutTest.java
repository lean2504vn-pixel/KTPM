package vn.edu.softwaretesting.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CheckoutPage;
import vn.edu.softwaretesting.pages.LoginPage;

@DisplayName("FR-03: Checkout Validation")
class CheckoutTest extends WebTestBase {
    private CheckoutPage checkoutPage;

    private void navigateToCheckout(String username) {
        // 1. Đăng nhập
        new LoginPage(driver, baseUrl).open().login(username, "secret_sauce");

        // 2. NHẢY DÙ: Đi thẳng vào form để cắt đứt mọi lỗi chập chờn từ CartPage của đồng đội
        driver.get(baseUrl + "/checkout-step-one.html");
        checkoutPage = new CheckoutPage(driver);
    }

    @ParameterizedTest(name = "{0}: missing field -> {4}")
    @CsvSource({
            "TC-CHK-DT1,   ,   ,       , Error: First Name is required",
            "TC-CHK-DT2,   ,   , 700000, Error: First Name is required",
            "TC-CHK-DT3,   , Le,       , Error: First Name is required",
            "TC-CHK-DT4, An,   ,       , Error: Last Name is required",
            "TC-CHK-DT5, An, Le,       , Error: Postal Code is required",
            "TC-CHK-DT6,   , Le, 700000, Error: First Name is required",
            "TC-CHK-DT7, An,   , 700000, Error: Last Name is required"
    })
    void missingFieldShowsError(String id, String first, String last, String postal, String expected) {
        // Dùng standard_user để test Validation chuẩn
        navigateToCheckout("standard_user");

        String message = checkoutPage
                .fillInformation(first, last, postal)
                .continueExpectingError();

        assertTrue(message.contains(expected), () -> id + " got: " + message);
    }

    @Test
    @Tag("known-bug")
    @DisplayName("TC-CHK-BUG03 [BUG-03]: problem_user cannot fill Last Name in checkout")
    void problemUserCannotFillLastName() {
        // Dùng problem_user để bắt đúng lỗi BUG-03
        navigateToCheckout("problem_user");

        checkoutPage.fillInformation("An", "Le", "700000");

        WebElement lastNameInput = driver.findElement(By.id("last-name"));
        String actualLastName = lastNameInput.getAttribute("value");

        assertEquals("Le", actualLastName,
                "Bug 03: Expected 'Le' to be in Last Name field, but field was empty or overridden.");
    }
}