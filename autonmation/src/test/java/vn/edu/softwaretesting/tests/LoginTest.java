package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

@Tag("web")
@DisplayName("FR-LOGIN: Authentication")
class LoginTest extends WebTestBase {

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver, baseUrl).open();
    }

    @ParameterizedTest(name = "TC-LOGIN-{index}: user={0}, accepted={2}")
    @CsvFileSource(resources = "/login-data.csv", numLinesToSkip = 1)
    void loginShouldMatchExpectedOutcome(
            String username,
            String password,
            boolean expectedAccepted,
            String expectedEvidence) {

        LoginResult actual = loginPage.login(username, password);

        assertEquals(expectedAccepted, actual.accepted(),
                () -> "Unexpected result for user: " + username);
        assertTrue(
                actual.currentUrl().contains(expectedEvidence)
                        || actual.message().contains(expectedEvidence),
                () -> "Expected evidence '" + expectedEvidence
                        + "', but URL/message was: "
                        + actual.currentUrl() + " / " + actual.message());
    }
}
