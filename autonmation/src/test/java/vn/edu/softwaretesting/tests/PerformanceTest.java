package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

@Tag("web")
@Tag("performance")
@DisplayName("NFR-01: Login performance")
class PerformanceTest extends WebTestBase {

    private static final String PASSWORD = "secret_sauce";
    private static final int RUNS = 5;
    private static final long LIMIT_MILLIS = 3000; // KHÔNG nới ngưỡng này

    private LoginPage loginPage;

    @BeforeEach
    void createLoginPage() {
        loginPage = new LoginPage(driver, baseUrl);
    }

    /**
     * Đo RUNS lần "nhấn Login -> trang sản phẩm hiển thị" và trả về trung vị.
     * Mỗi lần đo bắt đầu từ trang đăng nhập sạch (xóa cookie phiên cũ).
     * Lưu ý: LoginPage.login() chờ bằng WebDriverWait (polling 500 ms) nên
     * số đo có sai số tối đa khoảng 0,5 s; với ngưỡng 3 s vẫn đủ phân biệt.
     */
    private long medianLoginMillis(String username) {
        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) {
            if (i > 0) {
                driver.manage().deleteAllCookies(); // reset phiên, không phải thao tác UI
            }
            loginPage.open();

            long start = System.nanoTime();
            LoginResult result = loginPage.login(username, PASSWORD);
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();

            assertTrue(result.accepted(),
                    "Login failed for " + username + ": " + result.message());
            samples.add(millis);
        }
        Collections.sort(samples);
        System.out.println("[NFR-01] " + username + " samples (ms, sorted) = " + samples);
        return samples.get(RUNS / 2); // phần tử thứ 3 trong 5 giá trị đã sắp xếp
    }

    @Test
    @Tag("known-bug")
    @DisplayName("TC-NFR-01 [BUG-01]: performance_glitch_user - inventory loads within 3 s (median of 5)")
    void glitchUserInventoryLoadsWithinThreeSeconds() {
        long median = medianLoginMillis("performance_glitch_user");

        assertTrue(median <= LIMIT_MILLIS,
                () -> "Median of " + RUNS + " runs was " + median
                        + " ms, limit is " + LIMIT_MILLIS + " ms");
    }

    @Test
    @DisplayName("TC-NFR-01: standard_user - inventory loads within 3 s (median of 5)")
    void standardUserInventoryLoadsWithinThreeSeconds() {
        long median = medianLoginMillis("standard_user");

        assertTrue(median <= LIMIT_MILLIS,
                () -> "Median of " + RUNS + " runs was " + median
                        + " ms, limit is " + LIMIT_MILLIS + " ms");
    }
}
