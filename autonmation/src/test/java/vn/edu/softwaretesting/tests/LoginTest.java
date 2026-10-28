package vn.edu.softwaretesting.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest extends WebTestBase {

    @ParameterizedTest(name = "{0}: user='{1}', accepted={3}")
    @CsvFileSource(resources = "/login-data.csv", numLinesToSkip = 1)
    @DisplayName("FR-01: Login Data-Driven Testing")
    void testLoginWithCsvData(String testId, String username, String password, boolean accepted,
            String expectedEvidence) {
        LoginPage loginPage = new LoginPage(driver, baseUrl);
        loginPage.open();

        LoginResult result = loginPage.login(username, password);

        assertEquals(accepted, result.accepted(), () -> testId + " acceptance mismatch");

        if (accepted) {
            assertTrue(driver.getCurrentUrl().contains(expectedEvidence),
                    () -> testId + " expected URL to contain: " + expectedEvidence);
        } else {
            String errorMsg = result.message();
            assertTrue(errorMsg.contains(expectedEvidence),
                    () -> testId + " expected error message to contain '" + expectedEvidence + "', but got: "
                            + errorMsg);
        }
    }
}