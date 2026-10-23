# Tuấn Em — FR-02 Giỏ hàng | Assignment 1

## Những tệp đã thực hiện

- `src/test/java/vn/edu/softwaretesting/pages/InventoryPage.java`: Add, Remove, badge, mở giỏ, đọc tên/giá ở Inventory, thêm N sản phẩm.
- `src/test/java/vn/edu/softwaretesting/pages/CartPage.java`: đọc danh sách, đếm sản phẩm, Remove, Continue Shopping, Checkout.
- `src/test/java/vn/edu/softwaretesting/tests/CartTest.java`: **9 test cases**: B1..B5 + UC1..UC4 (3 lượt tham số hoá B2/B3/B4).
- `src/test/java/vn/edu/softwaretesting/tests/CartBugInvestigationTest.java`: kiểm tra nghi vấn lỗi với `problem_user`, mặc định SKIPPED.
- `src/test/java/vn/edu/softwaretesting/pages/CheckoutPage.java`: **CẦU NỐI TẠM** cho FR-03; Đăng Khoa/Trần Ân thay bằng bản CheckoutPage đầy đủ khi merge, nhưng giữ constructor `CheckoutPage(WebDriver)`.

## Chạy từ thư mục starter-code

```powershell
mvn -Dtest=CartTest test
mvn -Dtest=CartTest -Dheadless=false test
mvn -Dtest=CartBugInvestigationTest -DrunBugProbe=true -Dheadless=false test
mvn clean test
```

Điều kiện: JDK 17+, Maven 3.9+, Chrome + Internet (trang saucedemo.com). Sau khi chạy, đọc `target/surefire-reports`. Các test cần chạy trên máy có Chrome; mã nguồn chưa thể hiện trạng thái PASS khi chưa có kết quả thực thi thật.

## Trình tự thực thi thủ công NV4

1. Chrome -> https://www.saucedemo.com -> `standard_user` / `secret_sauce`.
2. Chạy lần lượt TC-CART-B1..B5 và UC1..UC4 trong Excel (mỗi lần reset phiên đăng nhập/giỏ), chụp badge, URL và danh sách giỏ.
3. Đăng xuất, thử `problem_user` / `secret_sauce` với Sauce Labs Fleece Jacket (Add rồi Remove). Ghi rõ hiện tượng thực tế; không kết luận lỗi chỉ dựa trên hướng dẫn phân công.
4. Nếu tái hiện lỗi, quay video, ghi ngày giờ/Chrome/OS/bước tái hiện và cập nhật BUG-02 trong sheet Bugs. Nếu không tái hiện, ghi `không tái hiện`.
5. Ghi `PASS/FAIL/BLOCKED` trong sheet Execution **chỉ sau khi thực sự chạy**.

## Quan trọng khi merge nhóm

- **Gia Bảo (FR-04)** cũng cần sửa `InventoryPage.java`. Không dùng cách ghi đè cả tệp của một người; ghép thêm `sortBy`, `names`, `prices` vào lớp hiện có và giữ nguyên hàm FR-02.
- **Đăng Khoa/Trần Ân (FR-03)** thay `CheckoutPage.java` cầu nối tạm bằng phiên bản có đủ logic checkout, giữ `public CheckoutPage(WebDriver driver)`.
- **An (FR-01)** giữ `LoginPage.java`/`LoginTest.java` và CSV hiện có; FR-02 không sửa các tệp đó.
- Chạy lại `mvn clean test` sau merge; không sửa assertion để che lỗi hệ thống. Nếu đã chứng minh lỗi, dùng `@Tag("known-bug")`, mã BUG thật, kèm bằng chứng.

## Quy tắc truy vết

| Requirement | Technique | Test case IDs | Class |
|---|---|---|---|
| FR-02 | BVA | TC-CART-B1..B5 | CartTest |
| FR-02 | UC | TC-CART-UC1..UC4 | CartTest |
| FR-02 | Investigation | TC-CART-PROB1 (chưa xác nhận) | CartBugInvestigationTest |

**Giới hạn BVA:** -1 và 7 không thể sinh qua giao diện SauceDemo: chỉ có 6 sản phẩm, mỗi sản phẩm có duy nhất một nút Add và chuyển sang Remove sau khi thêm. Ghi là *không áp dụng*, không phải `FAIL`.
