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

    /** Kết quả đo: các mẫu thời gian (ms, đã sắp xếp) và việc mọi lần đăng nhập có được chấp nhận hay không. */
    private record Measurement(List<Long> samples, boolean allAccepted) { }

    private Measurement measure(String username) {
        List<Long> samples = new ArrayList<>();
        boolean allAccepted = true;
        for (int i = 0; i < RUNS; i++) {
            if (i > 0) {
                driver.manage().deleteAllCookies(); // reset phiên trước mỗi lần đo
            }
            loginPage.open();

            long start = System.nanoTime();
            LoginResult result = loginPage.login(username, PASSWORD);
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();

            samples.add(millis);
            allAccepted &= result.accepted();
        }
        Collections.sort(samples);
        System.out.println("[NFR-01] " + username + " samples (ms, sorted) = " + samples);
        return new Measurement(samples, allAccepted);
    }

    private void assertWithinLimit(String username, Measurement m) {
        long median = m.samples().get(RUNS / 2);
        // Ghi số đo trước khi assert, để báo cáo luôn có số thật.
        assertTrue(median <= LIMIT_MILLIS,
                () -> "Median of " + RUNS + " runs = " + median + " ms, limit "
                        + LIMIT_MILLIS + " ms; samples " + m.samples());
        assertTrue(m.allAccepted(),
                () -> "Login was not accepted for " + username);
    }

    @Test
    @Tag("known-bug")
    @DisplayName("TC-NFR-01 [BUG-01]: performance_glitch_user - inventory loads within 3 s (median of 5)")
    void glitchUserInventoryLoadsWithinThreeSeconds() {
        assertWithinLimit("performance_glitch_user", measure("performance_glitch_user"));
    }

    @Test
    @DisplayName("TC-NFR-02: standard_user - inventory loads within 3 s (median of 5)")
    void standardUserInventoryLoadsWithinThreeSeconds() {
        assertWithinLimit("standard_user", measure("standard_user"));
    }
}