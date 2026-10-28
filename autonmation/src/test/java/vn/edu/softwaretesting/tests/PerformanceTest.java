package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static final int RUNS = 5;

    @Test
    @Tag("known-bug")
    @DisplayName("TC-NFR-01 [BUG-01]: performance_glitch_user - inventory loads within 3 s (median of 5)")
    void inventoryLoadsWithinThreeSeconds() {
        LoginPage loginPage = new LoginPage(driver, baseUrl);
        List<Long> samples = new ArrayList<>();
        boolean allAccepted = true;

        for (int i = 0; i < RUNS; i++) {
            if (i > 0) {
                driver.manage().deleteAllCookies(); // mỗi lần đo bắt đầu từ trạng thái chưa đăng nhập
            }
            loginPage.open();

            long start = System.nanoTime();
            LoginResult result = loginPage.login("performance_glitch_user", "secret_sauce");
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();

            samples.add(millis);
            allAccepted &= result.accepted();
        }

        Collections.sort(samples);
        long median = samples.get(RUNS / 2);
        System.out.println("[NFR-01] performance_glitch_user samples (ms, sorted) = " + samples);

        assertTrue(allAccepted, "Login was not accepted for performance_glitch_user");
        assertTrue(median <= 3000,
                () -> "Inventory took " + median + " ms (median of " + RUNS
                        + " runs; samples " + samples + ")");
    }
}