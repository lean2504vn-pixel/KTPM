package vn.edu.softwaretesting.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Bonus 7.7 - Data-driven mức 2:
 * Bộ thực thi kịch bản kiểm thử đọc toàn bộ URL, locator, action và dữ liệu từ tệp CSV
 * (cột: page, locator, action, value). Đổi sang website khác chỉ cần sửa tệp cấu hình, không sửa Java.
 */
public final class ConfigDrivenFlowRunner {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ConfigDrivenFlowRunner(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    public record FlowStep(String page, String locator, String action, String value) {
    }

    public static List<FlowStep> loadStepsFromResource(String resourcePath) {
        List<FlowStep> steps = new ArrayList<>();
        try (InputStream is = ConfigDrivenFlowRunner.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] parts = line.split(",", 4);
                    String page = parts.length > 0 ? parts[0].trim() : "";
                    String locator = parts.length > 1 ? parts[1].trim() : "";
                    String action = parts.length > 2 ? parts[2].trim() : "";
                    String value = parts.length > 3 ? parts[3].trim() : "";
                    steps.add(new FlowStep(page, locator, action, value));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read flow file: " + resourcePath, e);
        }
        return steps;
    }

    public void executeFlow(String resourcePath) {
        List<FlowStep> steps = loadStepsFromResource(resourcePath);
        for (int i = 0; i < steps.size(); i++) {
            FlowStep step = steps.get(i);
            executeStep(i + 1, step);
        }
    }

    private void executeStep(int stepNumber, FlowStep step) {
        switch (step.action()) {
            case "open" -> driver.get(step.locator());
            case "type" -> {
                WebElement element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(parseLocator(step.locator())));
                element.clear();
                element.sendKeys(step.value());
            }
            case "click" -> wait.until(
                    ExpectedConditions.elementToBeClickable(parseLocator(step.locator()))).click();
            case "assertText" -> {
                String actual = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(parseLocator(step.locator()))).getText();
                assertTrue(actual.contains(step.value()),
                        () -> String.format("Step %d [%s]: expected text '%s' in '%s'",
                                stepNumber, step.page(), step.value(), actual));
            }
            case "assertUrl" -> {
                boolean matched = wait.until(ExpectedConditions.urlContains(step.value()));
                assertTrue(matched,
                        () -> String.format("Step %d [%s]: expected URL to contain '%s', got '%s'",
                                stepNumber, step.page(), step.value(), driver.getCurrentUrl()));
            }
            default -> throw new IllegalArgumentException("Unsupported action: " + step.action());
        }
    }

    private static By parseLocator(String spec) {
        if (spec.startsWith("id:")) {
            return By.id(spec.substring("id:".length()));
        }
        if (spec.startsWith("css:")) {
            return By.cssSelector(spec.substring("css:".length()));
        }
        if (spec.startsWith("xpath:")) {
            return By.xpath(spec.substring("xpath:".length()));
        }
        if (spec.startsWith("name:")) {
            return By.name(spec.substring("name:".length()));
        }
        return By.cssSelector(spec);
    }
}
