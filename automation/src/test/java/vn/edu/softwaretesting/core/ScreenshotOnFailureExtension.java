package vn.edu.softwaretesting.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Bonus 7.7: JUnit 5 extension (TestWatcher & AfterTestExecutionCallback)
 * tự động chụp ảnh màn hình khi test FAIL trước khi trình duyệt đóng ở @AfterEach.
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback, TestWatcher {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final Supplier<WebDriver> driverSupplier;

    public ScreenshotOnFailureExtension(Supplier<WebDriver> driverSupplier) {
        this.driverSupplier = driverSupplier;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        WebDriver driver = driverSupplier.get();
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return;
        }
        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);

            String displayName = context.getDisplayName()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            if (displayName.length() > 60) {
                displayName = displayName.substring(0, 60);
            }
            String timestamp = LocalDateTime.now().format(TS_FORMAT);
            Path destination = dir.resolve(displayName + "_" + timestamp + ".png");

            Path tempFile = screenshotDriver.getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[ScreenshotOnFailureExtension] Saved failure screenshot: " + destination.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.err.println("[ScreenshotOnFailureExtension] Could not capture screenshot: " + e.getMessage());
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("[TestWatcher] Test FAILED: " + context.getDisplayName()
                + " -> Reason: " + (cause != null ? cause.getMessage() : "unknown"));
    }
}
