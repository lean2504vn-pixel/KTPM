package vn.edu.softwaretesting.pages;

import org.openqa.selenium.WebDriver;

/**
 * BRIDGE TẠM CHO TÍCH HỢP: người phụ trách FR-03 thay toàn bộ tệp này bằng
 * CheckoutPage đầy đủ. Giữ public CheckoutPage(WebDriver driver) để CartPage.checkout()
 * tương thích và chạy biên dịch khi mới có FR-02.
 */
public class CheckoutPage {
    protected final WebDriver driver;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
    }
}
