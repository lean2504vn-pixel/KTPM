package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

/**
 * TC-NFR-01: đo 5 lần thời gian từ lúc nhấn Login đến khi trang sản phẩm hiển thị,
 * lấy trung vị, so với ngưỡng 3 s.
 *
 * Mỗi lần đo là một lần lặp của @RepeatedTest, nên WebTestBase mở một trình duyệt mới
 * (chưa đăng nhập, không cookie) cho từng lần đo. Không đo 5 lần trong cùng một phiên:
 * xoá cookie hoặc đăng xuất giữa các lần đo với performance_glitch_user làm form đăng
 * nhập bị xoá trắng / click bị bỏ qua, khiến test FAIL vì script thay vì vì BUG-01.
 */
@Tag("web")
@Tag("performance")
@Tag("known-bug") // cả lớp: -Dgroups=known-bug phải chạy cả 5 lần đo lẫn bước kiểm tra trung vị
@DisplayName("NFR-01: Login performance")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceTest extends WebTestBase {

    private static final int RUNS = 5;
    private static final long THRESHOLD_MS = 3000;
    private static final List<Long> SAMPLES = Collections.synchronizedList(new ArrayList<>());

    @Order(1)
    @RepeatedTest(value = RUNS, name = "TC-NFR-01 lan do {currentRepetition}/{totalRepetitions}")
    @DisplayName("TC-NFR-01: do thoi gian dang nhap performance_glitch_user")
    void measureLoginTime(RepetitionInfo repetition) {
        LoginPage loginPage = new LoginPage(driver, baseUrl).open();

        long start = System.nanoTime();
        LoginResult result = loginPage.login("performance_glitch_user", "secret_sauce");
        long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        System.out.printf("[NFR-01] run %d: %d ms, accepted=%s, message=%s%n",
                repetition.getCurrentRepetition(), millis, result.accepted(), result.message());
        assertTrue(result.accepted(),
                () -> "Login was not accepted for performance_glitch_user: " + result.message());
        SAMPLES.add(millis);
    }

    @Order(2)
    @Test
    @DisplayName("TC-NFR-01 [BUG-01]: performance_glitch_user - inventory loads within 3 s (median of 5)")
    void inventoryLoadsWithinThreeSeconds() {
        List<Long> samples = new ArrayList<>(SAMPLES);
        assertEquals(RUNS, samples.size(),
                () -> "Can du " + RUNS + " lan do hop le, chi co " + samples.size());

        Collections.sort(samples);
        long median = samples.get(RUNS / 2);
        System.out.println("[NFR-01] performance_glitch_user samples (ms, sorted) = " + samples
                + ", median = " + median + " ms");

        assertTrue(median <= THRESHOLD_MS,
                () -> "Inventory took " + median + " ms (median of " + RUNS
                        + " runs; samples " + samples + ")");
    }
}
