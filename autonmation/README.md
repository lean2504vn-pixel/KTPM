# Assignment 1 — JUnit 5/Selenium starter

Đây là mã minh họa đi kèm đề bài, không phải lời giải hoàn chỉnh. Sinh viên phải
thay Page Object, dữ liệu và test theo đúng hệ thống đã đăng ký.

## Yêu cầu

- JDK 17 trở lên
- Maven 3.9 trở lên
- Chrome hoặc Firefox

Selenium Manager sẽ tự tìm hoặc tải driver tương thích trong lần chạy đầu.

## Chạy test

```bash
mvn clean test
```

Chạy có giao diện trên Chrome:

```bash
mvn test -Dheadless=false -Dbrowser=chrome
```

Chạy Firefox với một hệ thống khác:

```bash
mvn test \
  -Dbrowser=firefox \
  -DbaseUrl=http://localhost:8080 \
  -Dheadless=true
```

Kết quả XML và log được tạo trong `target/surefire-reports/`.

## Những điểm mẫu đang minh họa

- JUnit Jupiter lifecycle (`@BeforeEach`, `@AfterEach`)
- `@ParameterizedTest` và dữ liệu ngoài bằng `@CsvFileSource`
- Page Object tách locator/thao tác khỏi assertion
- explicit wait thay cho `Thread.sleep`
- cấu hình URL, trình duyệt và chế độ headless qua system property

Tài khoản trong CSV là dữ liệu kiểm thử công khai của
[SauceDemo](https://www.saucedemo.com/). Không dùng thông tin đăng nhập thật
trong mã nguồn hoặc tệp dữ liệu.
