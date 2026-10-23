package vn.edu.softwaretesting.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/*
 * LƯU Ý KHI GỘP (merge) VỚI TV2:
 * File này chỉ có khung (driver, wait, constructor) + PHẦN SORT của TV5.
 * Khi gộp: giữ nguyên khung và các hàm giỏ hàng của TV2, chỉ copy khối
 * "===== TV5: PHẦN SORT =====" (3 locator + 3 hàm) và các import còn thiếu
 * (Select, WebElement, List).
 */
public final class InventoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    // ===== TV5: PHẦN SORT (FR-04) =====================================

    private static final By SORT_DROPDOWN =
            By.cssSelector("[data-test='product-sort-container']");
    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICES = By.cssSelector(".inventory_item_price");

    /** optionValue: "az" | "za" | "lohi" | "hilo". */
    public InventoryPage sortBy(String optionValue) {
        WebElement dropdown = wait.until(
                ExpectedConditions.visibilityOfElementLocated(SORT_DROPDOWN));
        new Select(dropdown).selectByValue(optionValue);
        return this;
    }

    public List<String> getProductNames() {
        return wait.until(ExpectedConditions
                        .visibilityOfAllElementsLocatedBy(ITEM_NAMES))
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public List<Double> getProductPrices() {
        return wait.until(ExpectedConditions
                        .visibilityOfAllElementsLocatedBy(ITEM_PRICES))
                .stream()
                .map(e -> Double.parseDouble(
                        e.getText().replace("$", "").trim()))
                .toList();
    }

    // ===== HẾT PHẦN SORT ==============================================
}
