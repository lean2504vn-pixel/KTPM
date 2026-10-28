package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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

    private static final int PRODUCT_COUNT = 6;

    private InventoryPage inventoryPage;

    @BeforeEach
    void loginAsStandardUser() {
        LoginResult result = new LoginPage(driver, baseUrl)
                .open()
                .login("standard_user", "secret_sauce");
        assertTrue(result.accepted(),
                () -> "Precondition failed: " + result.message());
        inventoryPage = new InventoryPage(driver);
    }

    @ParameterizedTest(name = "{0}: sort by name, option={1}")
    @CsvSource({
            "TC-SORT-01, az, true",
            "TC-SORT-02, za, false"
    })
    void sortByName(String id, String option, boolean ascending) {
        List<String> actual = inventoryPage.sortBy(option).getProductNames();
        List<String> expected = new ArrayList<>(actual);
        Comparator<String> order = ascending
                ? Comparator.naturalOrder() : Comparator.reverseOrder();
        expected.sort(order);

        assertEquals(PRODUCT_COUNT, actual.size(),
                () -> id + ": unexpected number of products");
        assertEquals(expected, actual,
                () -> id + ": names are not sorted for option " + option);
    }

    @ParameterizedTest(name = "{0}: sort by price, option={1}")
    @CsvSource({
            "TC-SORT-03, lohi, true",
            "TC-SORT-04, hilo, false"
    })
    void sortByPrice(String id, String option, boolean ascending) {
        List<BigDecimal> actual = inventoryPage.sortBy(option).getProductPrices();
        List<BigDecimal> expected = new ArrayList<>(actual);
        Comparator<BigDecimal> order = ascending
                ? Comparator.naturalOrder() : Comparator.reverseOrder();
        expected.sort(order);

        assertEquals(PRODUCT_COUNT, actual.size(),
                () -> id + ": unexpected number of products");
        assertEquals(expected, actual,
                () -> id + ": prices are not sorted for option " + option);
    }

    // Lỗi đã biết BUG-04: problem_user sort không hoạt động
    @Test
    @Tag("known-bug")
    @DisplayName("TC-SORT-05 [BUG-04]: problem_user - Price (low to high) is not applied")
    void problemUserSortByPriceIsNotApplied() {
        driver.manage().deleteAllCookies(); // đăng xuất phiên standard_user
        LoginResult result = new LoginPage(driver, baseUrl)
                .open()
                .login("problem_user", "secret_sauce");
        assertTrue(result.accepted(),
                () -> "Precondition failed for problem_user: " + result.message());

        List<BigDecimal> actual = new InventoryPage(driver).sortBy("lohi").getProductPrices();
        List<BigDecimal> expected = new ArrayList<>(actual);
        expected.sort(Comparator.naturalOrder());

        assertEquals(expected, actual,
                "problem_user: prices are not in ascending order after sorting");
    }
}