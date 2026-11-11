package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CartPage;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.InventoryPage.ProductInfo;
import vn.edu.softwaretesting.pages.LoginPage;

@Tag("web")
@Tag("FR-02")
@DisplayName("FR-02: Quan ly gio hang")
@ExtendWith(CartTest.FailureScreenshot.class)
class CartTest extends WebTestBase {

    private static final String BACKPACK = "sauce-labs-backpack";
    private static final String BIKE_LIGHT = "sauce-labs-bike-light";
    private InventoryPage inventory;

    private static final String FLEECE = "Sauce Labs Fleece Jacket";
    private static final String RED_SHIRT = "Test.allTheThings() T-Shirt (Red)";
    private static final String BOLT_T_SHIRT = "Sauce Labs Bolt T-Shirt";
    private static final String ONESIE = "Sauce Labs Onesie";
    private static final String BACKPACK_NAME = "Sauce Labs Backpack";
    private CartPage page;

    @BeforeEach
    void loginForTest(TestInfo testInfo) {
        if (testInfo.getTags().contains("known-bug")) {
            var result = new LoginPage(driver, baseUrl).open()
                    .login("problem_user", "secret_sauce");
            assertTrue(result.accepted(), "Tien dieu kien: dang nhap problem_user thanh cong");
            page = new CartPage(driver).waitForInventory();
        } else {
            var result = new LoginPage(driver, baseUrl).open()
                    .login("standard_user", "secret_sauce");
            assertTrue(result.accepted(), "Tien dieu kien: dang nhap standard_user thanh cong");
            inventory = new InventoryPage(driver);
        }
    }

    @Test
    @DisplayName("TC-CART-B1: 0 san pham - khong hien badge")
    void emptyCartHasNoBadge() {
        assertFalse(inventory.isBadgeDisplayed());
        CartPage cart = inventory.openCart();
        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(0, cart.getCartItemCount());
    }

    @ParameterizedTest(name = "{0}: badge={1}")
    @CsvSource({
            "TC-CART-B2, 1",
            "TC-CART-B3, 5",
            "TC-CART-B4, 6"
    })
    @DisplayName("TC-CART-B2/B3/B4: cac bien 1, 5, 6")
    void badgeMatchesBoundary(String testId, int expected) {
        if (expected == 5) {
            inventory.addFirstNProducts(6);
            // Theo đề: để đạt 5 phải thêm đủ 6 rồi Remove 1.
            inventory.removeFromCart(BACKPACK);
        } else {
            inventory.addFirstNProducts(expected);
        }
        assertEquals(expected, inventory.getBadgeCount(), testId);
        assertEquals(Integer.toString(expected), inventory.getBadgeText(), testId);
        CartPage cart = inventory.openCart();
        assertEquals(expected, cart.getCartItemCount(), testId);
    }

    @Test
    @DisplayName("TC-CART-B5: 1 -> 0 san pham - badge bien mat")
    void removeLastProductHidesBadge() {
        inventory.addToCart(BACKPACK);
        assertEquals(1, inventory.getBadgeCount());
        inventory.removeFromCart(BACKPACK);
        assertFalse(inventory.isBadgeDisplayed());
        assertEquals(0, inventory.openCart().getCartItemCount());
    }

    @Test
    @DisplayName("TC-CART-UC1: them hai san pham, ten va gia trong gio khop Inventory")
    void cartShowsSameProductsAndPrices() {
        ProductInfo backpack = inventory.getProductInfo(BACKPACK);
        ProductInfo light = inventory.getProductInfo(BIKE_LIGHT);
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);

        CartPage cart = inventory.openCart();
        assertTrue(cart.currentUrl().contains("cart.html"));
        assertEquals(2, cart.getCartItemCount());
        List<CartPage.CartItem> items = cart.getCartItems();
        assertTrue(items.stream().anyMatch(i -> i.name().equals(backpack.name())
                && i.price().compareTo(backpack.price()) == 0 && i.quantity() == 1));
        assertTrue(items.stream().anyMatch(i -> i.name().equals(light.name())
                && i.price().compareTo(light.price()) == 0 && i.quantity() == 1));
    }

    @Test
    @DisplayName("TC-CART-UC2: Remove tai Inventory - badge giam tu 2 xuong 1")
    void removeOnInventoryReducesBadge() {
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);
        assertEquals(2, inventory.getBadgeCount());
        inventory.removeFromCart(BACKPACK);
        assertEquals(1, inventory.getBadgeCount());
        CartPage cart = inventory.openCart();
        assertEquals(1, cart.getCartItemCount());
        assertFalse(cart.containsProduct("Sauce Labs Backpack"));
    }

    @Test
    @DisplayName("TC-CART-UC3: Remove trong Cart - san pham bien mat")
    void removeFromCartPageRemovesProduct() {
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);
        CartPage cart = inventory.openCart();
        assertEquals(2, cart.getCartItemCount());
        cart.removeProduct(BACKPACK);
        assertEquals(1, cart.getCartItemCount());
        assertFalse(cart.containsProduct("Sauce Labs Backpack"));
        assertEquals(1, cart.getBadgeCount());
    }

    @Test
    @DisplayName("TC-CART-UC4: Continue Shopping - quay Inventory, giu nguyen gio")
    void continueShoppingPreservesCart() {
        inventory.addToCart(BACKPACK);
        CartPage cart = inventory.openCart();
        assertEquals(1, cart.getCartItemCount());
        InventoryPage returned = cart.continueShopping();
        assertTrue(returned.currentUrl().contains("inventory.html"));
        assertEquals(1, returned.getBadgeCount());
        assertTrue(returned.openCart().containsProduct("Sauce Labs Backpack"));
    }

    // ===== TEST CHO problem_user: phat hien BUG =====


    // Dùng riêng cho extension để không phải sửa lớp WebTestBase.
    WebDriver evidenceDriver() {
        return driver;
    }

    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-02] Add Fleece Jacket tai Inventory phai thanh cong")
    void bug02_addFleeceAtInventoryMustWork() {
        int before = page.badgeCount();
        assertEquals("ADD", page.buttonStateAtInventory(FLEECE),
                "Tien dieu kien: Fleece Jacket chua co trong gio hang");
        page.clickInventoryAdd(FLEECE);
        String state = page.buttonStateAtInventory(FLEECE);
        int after = page.badgeCount();
        page.openCart();
        boolean inCart = page.cartContains(FLEECE);
        assertAll("[BUG-02] Add tai Inventory",
                () -> assertEquals("REMOVE", state),
                () -> assertEquals(before + 1, after),
                () -> assertTrue(inCart));
    }

    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-06] Badge, Inventory va Cart phai dong bo sau khi mo Fleece Jacket")
    void bug06_fleeceDetailAndCartMustAgree() {
        // Mo bang link tren Inventory (giong thao tac thu cong, co the dan den id=6).
        // KHONG assert ten trang chi tiet o day: loi duong dan/ITEM NOT FOUND la BUG-09.
        int badgeBefore = page.badgeCount();
        String inventoryButtonBefore = page.buttonStateAtInventory(FLEECE);
        page.openDetail(FLEECE);
        var detail = page.detailInfo();
        String detailButtonBefore = page.buttonStateInDetail();

        // Thu Add neu website co nut Add. Ke ca neu Add khong hoat dong,
        // van thu thap trang thai thuc te tren ca ba man hinh.
        if ("ADD".equals(detailButtonBefore)) {
            page.clickDetailAdd();
        }
        int badgeAfterAdd = page.badgeCount();
        String detailButtonAfter = page.buttonStateInDetail();

        page.backToProducts();
        int badgeAtInventory = page.badgeCount();
        String inventoryButtonAfter = page.buttonStateAtInventory(FLEECE);

        // Mo Cart truc tiep de khong nham voi loi dieu huong icon gio hang.
        page.openCartDirectly();
        int badgeAtCart = page.badgeCount();
        int cartItems = page.cartItemCount();
        boolean fleeceInCart = page.cartContains(FLEECE);

        String snapshot = String.join(System.lineSeparator(),
                "[BUG-06] KIEM TRA DONG BO GIO HANG - problem_user",
                "San pham click tai Inventory: " + FLEECE,
                "URL chi tiet: " + detail.url(),
                "Ten chi tiet thuc te: " + detail.name(),
                "Badge truoc Add: " + badgeBefore,
                "Nut Inventory truoc Add: " + inventoryButtonBefore,
                "Nut Detail truoc Add: " + detailButtonBefore,
                "Badge sau Add o Detail: " + badgeAfterAdd,
                "Nut Detail sau Add: " + detailButtonAfter,
                "Badge khi quay lai Inventory: " + badgeAtInventory,
                "Nut Fleece Jacket tai Inventory: " + inventoryButtonAfter,
                "Badge tren cart.html: " + badgeAtCart,
                "So dong san pham thuc te trong Cart: " + cartItems,
                "Fleece Jacket co trong Cart: " + fleeceInCart,
                "Neu badge=1 va so dong Cart=0: loi dong bo da duoc tai hien.");
        System.out.println(snapshot);
        try {
            Path evidence = Path.of("evidence");
            Files.createDirectories(evidence);
            Files.writeString(evidence.resolve("BUG-06_cart_state.txt"), snapshot);
        } catch (Exception ex) {
            System.err.println("Khong ghi duoc nhat ky BUG-06: " + ex.getMessage());
        }

        // Chi kiem tra tinh NHAT QUAN. Neu Add khong lam gi (badge=0, Cart=0),
        // day thuoc BUG-02, KHONG duoc ket luan da tai hien BUG-06.
        assertAll("[BUG-06] Badge, nut va danh sach Cart phai dong bo",
                () -> assertEquals("ADD", detailButtonBefore,
                        "Tien dieu kien: trang chi tiet can co nut Add de thu"),
                () -> assertEquals(badgeAfterAdd, badgeAtInventory,
                        "Badge thay doi bat thuong khi quay lai Inventory"),
                () -> assertEquals(badgeAfterAdd, badgeAtCart,
                        "Badge khong thong nhat giua Detail va Cart"),
                () -> assertEquals(cartItems, badgeAtCart,
                        "Badge phai bang so san pham thuc te (vd 1 badge / 0 item la BUG-06)"),
                () -> assertEquals(fleeceInCart ? "REMOVE" : "ADD", inventoryButtonAfter,
                        "Nut Fleece Jacket tai Inventory phai phan anh Cart"),
                () -> {
                    if (badgeAfterAdd > badgeBefore) {
                        assertTrue(fleeceInCart,
                                "Badge da tang sau khi bam Add Fleece, nhung Cart khong co Fleece Jacket");
                    }
                },
                () -> {
                    if ("REMOVE".equals(detailButtonAfter)) {
                        assertTrue(fleeceInCart,
                                "Detail hien Remove nhung Cart khong co Fleece Jacket");
                    }
                });
    }

    // BUG-10: Khi Add tai Detail, problem_user co the them SAI san pham vao Cart.
    // Hai test dung chung logic nhung tach rieng de co report va screenshot cho tung truong hop.
    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-10][TH1] Add Bolt T-Shirt tai Detail khong duoc them Onesie")
    void bug10_boltTShirtDetailMustAddBoltTShirt() {
        verifyBug10DetailAddsCorrectProduct(BOLT_T_SHIRT, ONESIE, "TH1_Bolt_T_Shirt");
    }

    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-10][TH2] Add T-Shirt Red tai Detail khong duoc them Backpack")
    void bug10_redShirtDetailMustAddRedShirt() {
        verifyBug10DetailAddsCorrectProduct(RED_SHIRT, BACKPACK_NAME, "TH2_Red_T_Shirt");
    }

    /**
     * Di theo dung thao tac thu cong: click TEN san pham tren Inventory -> Detail -> Add.
     * Khong mo URL theo id vi nhu vay co the bo qua loi xay ra khi nguoi dung click ten.
     * Chi assert sau khi da doc trang thai Inventory va Cart de ghi nhan du bang chung.
     */
    private void verifyBug10DetailAddsCorrectProduct(
            String chosenProduct, String unexpectedlyAddedProduct, String caseId) {
        int badgeBefore = page.badgeCount();
        String chosenBefore = page.buttonStateAtInventory(chosenProduct);
        String otherBefore = page.buttonStateAtInventory(unexpectedlyAddedProduct);

        // Tien dieu kien: ca hai san pham chua co trong gio; du lieu can trong.
        assertAll("[BUG-10] Tien dieu kien " + caseId,
                () -> assertEquals(0, badgeBefore, "Badge ban dau phai = 0"),
                () -> assertEquals("ADD", chosenBefore, "San pham duoc chon phai chua trong Cart"),
                () -> assertEquals("ADD", otherBefore, "San pham khac phai chua trong Cart"));

        page.openDetail(chosenProduct);
        String detailName = page.detailInfo().name();
        String detailButtonBefore = page.buttonStateInDetail();
        assertEquals("ADD", detailButtonBefore, "Trang Detail phai co nut Add to cart truoc thao tac");
        page.clickDetailAdd();

        int badgeAfter = page.badgeCount();
        String detailButtonAfter = page.buttonStateInDetail();

        // BUG-10 kiem tra san pham duoc them, khong kiem tra nut Back to Products.
        // Ve Inventory bang URL truc tiep de tranh Timeout neu dieu huong Back bi loi.
        // Van giu nguyen session/Cart cua WebDriver hien tai.
        page.openInventoryDirectly();
        String chosenAfter = page.buttonStateAtInventory(chosenProduct);
        String otherAfter = page.buttonStateAtInventory(unexpectedlyAddedProduct);

        page.openCartDirectly();
        int cartItemCount = page.cartItemCount();
        boolean chosenInCart = page.cartContains(chosenProduct);
        boolean otherInCart = page.cartContains(unexpectedlyAddedProduct);

        String snapshot = String.join(System.lineSeparator(),
                "[BUG-10] " + caseId + " - Add sai san pham tu Detail",
                "San pham click tai Inventory: " + chosenProduct,
                "Ten hien thi tai Detail: " + detailName,
                "San pham khac can doi chieu: " + unexpectedlyAddedProduct,
                "Badge truoc/sau Add: " + badgeBefore + " / " + badgeAfter,
                "Nut Detail truoc/sau Add: " + detailButtonBefore + " / " + detailButtonAfter,
                "Nut Inventory san pham da chon: " + chosenBefore + " -> " + chosenAfter,
                "Nut Inventory san pham khac: " + otherBefore + " -> " + otherAfter,
                "So san pham thuc te trong Cart: " + cartItemCount,
                "Cart co san pham da chon: " + chosenInCart,
                "Cart co san pham khac: " + otherInCart);
        System.out.println(snapshot);

        try {
            Path folder = Path.of("evidence");
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("BUG-10_" + caseId + ".txt"), snapshot);
        } catch (Exception e) {
            System.err.println("Khong ghi duoc evidence BUG-10: " + e.getMessage());
        }

        // Expected: chi san pham da chon duoc them; san pham khac khong bi tac dong.
        // Khi bug xuat hien, assertAll giup bao cao dong thoi ca trang thai nut va Cart.
        assertAll("[BUG-10] " + caseId + ": Add tai Detail phai dung san pham",
                () -> assertEquals(badgeBefore + 1, badgeAfter,
                        "Badge phai tang dung 1 sau khi Add"),
                () -> assertEquals("REMOVE", detailButtonAfter,
                        "Nut tren Detail phai chuyen thanh Remove"),
                () -> assertEquals("REMOVE", chosenAfter,
                        "Inventory phai hien Remove cho san pham da chon: " + chosenProduct),
                () -> assertEquals("ADD", otherAfter,
                        "Inventory KHONG duoc hien Remove cho san pham khac: " + unexpectedlyAddedProduct),
                () -> assertEquals(1, cartItemCount,
                        "Cart phai co dung 1 san pham"),
                () -> assertTrue(chosenInCart,
                        "Cart phai chua san pham da chon: " + chosenProduct),
                () -> assertFalse(otherInCart,
                        "Cart KHONG duoc chua san pham khac: " + unexpectedlyAddedProduct));
    }

    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-07] Ten san pham o Inventory va trang chi tiet phai khop")
    void bug07_productNamesMustMatchDetails() {
        List<CartPage.ProductTile> tiles = page.productTiles();
        assertEquals(6, tiles.size(), "Inventory phai co 6 san pham");
        for (var tile : tiles) {
            page.openDetail(tile.name());
            var detail = page.detailInfo();
            assertEquals(tile.name(), detail.name(),
                    "Ten khong khop khi mo chi tiet: " + tile.name() + "; URL=" + detail.url());
            page.backToProducts();
        }
    }

    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-08] Anh 6 san pham tai Inventory phai phan biet va dung san pham")
    void bug08_inventoryImagesShouldNotAllBeSame() {
        var tiles = page.productTiles();
        assertEquals(6, tiles.size());
        long distinct = tiles.stream().map(CartPage.ProductTile::imageSrc)
                .filter(url -> url != null && !url.isBlank()).distinct().count();
        assertEquals(6L, distinct,
                "Sau san pham dang co " + distinct + " anh phan biet (mong doi 6); co the dang cung anh cho");
    }

    @Test
    @Tag("known-bug")
    @DisplayName("[BUG-09] Chi tiet Fleece Jacket phai hien dung ten va gia $49.99")
    void bug09_fleeceDetailMustNotBeItemNotFound() {
        page.openDetail(FLEECE);
        var detail = page.detailInfo();
        assertAll("[BUG-09] Thong tin trang chi tiet Fleece Jacket",
                () -> assertEquals(FLEECE, detail.name(), "Trang chi tiet khong duoc hien ITEM NOT FOUND"),
                () -> assertEquals("$49.99", detail.price(), "Gia phai dung theo Inventory"),
                () -> assertFalse(detail.name().toUpperCase().contains("NOT FOUND")));
    }

    /** Chụp ảnh cả Assertion FAIL và Selenium ERROR trước @AfterEach quit driver. */
    public static class FailureScreenshot implements AfterTestExecutionCallback {
        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            if (context.getExecutionException().isEmpty()) return;
            if (!context.getTags().contains("known-bug")) return;
            var instance = context.getRequiredTestInstance();
            if (!(instance instanceof CartTest test)) return;
            if (!(test.evidenceDriver() instanceof TakesScreenshot screenshot)) return;

            String method = context.getRequiredTestMethod().getName();
            String id = method.startsWith("bug02") ? "BUG-02"
                    : method.startsWith("bug06") ? "BUG-06"
                    : method.startsWith("bug10") ? "BUG-10"
                    : method.startsWith("bug07") ? "BUG-07"
                    : method.startsWith("bug08") ? "BUG-08" : "BUG-09";
            Path folder = Path.of("evidence");
            Files.createDirectories(folder);
            Path output = folder.resolve(id + "_" + method + ".png");
            try {
                Files.write(output, screenshot.getScreenshotAs(OutputType.BYTES));
                System.out.println("Evidence saved: " + output.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Screenshot could not be saved: " + e.getMessage());
            }
        }
    }
}
