package vn.edu.softwaretesting.core;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public abstract class WebTestBase {

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void openBrowser() {
        baseUrl = System.getProperty("baseUrl", "https://www.saucedemo.com");
        String browser = System.getProperty("browser", "chrome")
                .toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", "true"));

        driver = switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + browser);
        };

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(1440, 900));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    private ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-search-engine-choice-screen");
        return options;
    }

    private FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }
}
