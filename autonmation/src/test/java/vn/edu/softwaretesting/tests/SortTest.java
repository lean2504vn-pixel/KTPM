package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

@Tag("web")
@DisplayName("FR-04: Product sorting")
class SortTest extends WebTestBase {

    private static final String PASSWORD = "secret_sauce";
    private static final int PRODUCT_COUNT = 6;

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver, baseUrl).open();
    }

    private InventoryPage openInventoryAs(String username) {
        LoginResult result = loginPage.login(username, PASSWORD);
        assertTrue(result.accepted(),
                () -> "Precondition failed, cannot log in as " + username
                        + ": " + result.message());
        return new InventoryPage(driver);
    }

    // TC-SORT-01, TC-SORT-02: Name (A to Z) / Name (Z to A)
    @ParameterizedTest(name = "{0}: sort by name, option={1}")
    @CsvSource({
            "TC-SORT-01, az, true",
            "TC-SORT-02, za, false"
    })
    void sortByName(String id, String option, boolean ascending) {
        InventoryPage inventory = openInventoryAs("standard_user");

        List<String> actual = inventory.sortBy(option).getProductNames();
        List<String> expected = new ArrayList<>(actual);
        Comparator<String> order = ascending
                ? Comparator.naturalOrder() : Comparator.reverseOrder();
        expected.sort(order);

        assertEquals(PRODUCT_COUNT, actual.size(),
                () -> id + ": unexpected number of products");
        assertEquals(expected, actual,
                () -> id + ": names are not sorted for option " + option);
    }

    // TC-SORT-03, TC-SORT-04: Price (low to high) / Price (high to low)
    @ParameterizedTest(name = "{0}: sort by price, option={1}")
    @CsvSource({
            "TC-SORT-03, lohi, true",
            "TC-SORT-04, hilo, false"
    })
    void sortByPrice(String id, String option, boolean ascending) {
        InventoryPage inventory = openInventoryAs("standard_user");

        List<Double> actual = inventory.sortBy(option).getProductPrices();
        List<Double> expected = new ArrayList<>(actual);
        Comparator<Double> order = ascending
                ? Comparator.naturalOrder() : Comparator.reverseOrder();
        expected.sort(order);

        assertEquals(PRODUCT_COUNT, actual.size(),
                () -> id + ": unexpected number of products");
        assertEquals(expected, actual,
                () -> id + ": prices are not sorted for option " + option);
    }

    // Lỗi hệ thống đã biết: problem_user chọn sắp xếp nhưng danh sách không đổi.
    // Test này PHẢI FAIL cho đến khi hệ thống được sửa. Không sửa assertion.
    @Test
    @Tag("known-bug")
    @DisplayName("TC-SORT-05 [BUG-04]: problem_user - Price (low to high) is not applied")
    void problemUserSortByPriceIsNotApplied() {
        InventoryPage inventory = openInventoryAs("problem_user");

        List<Double> actual = inventory.sortBy("lohi").getProductPrices();
        List<Double> expected = new ArrayList<>(actual);
        expected.sort(Comparator.naturalOrder());

        assertEquals(expected, actual,
                "problem_user: prices are not in ascending order after sorting");
    }
}
