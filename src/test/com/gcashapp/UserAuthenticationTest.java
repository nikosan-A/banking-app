package com.gcashapp;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests – UserAuthentication
 *
 * Tests: valid login, invalid login (wrong PIN, wrong number),
 *        register (valid & duplicate), changePin, logout.
 */
@DisplayName("UserAuthentication Tests")
class UserAuthenticationTest {

    private UserAuthentication auth;

    // Fresh instance before every test so tests don't share state
    @BeforeEach
    void setUp() {
        auth = new UserAuthentication();
        // Seeded demo users (from constructor):
        //  ID 1 → Juan  | 09171234567 | PIN 123456
        //  ID 2 → Maria | 09189999999 | PIN 654321
    }

    // ─── LOGIN TESTS ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Login – valid credentials returns correct user ID")
    void testValidLogin() {
        int result = auth.login("09171234567", "123456");
        assertEquals(1, result, "Valid login should return user ID 1 (Juan)");
    }

    @Test
    @DisplayName("Login – valid credentials sets current user")
    void testValidLoginSetsSession() {
        auth.login("09171234567", "123456");
        assertNotNull(auth.getCurrentUser(), "Current user should not be null after login");
        assertEquals("Juan dela Cruz", auth.getCurrentUser().getName());
    }

    @Test
    @DisplayName("Login – wrong PIN returns -1")
    void testInvalidLoginWrongPin() {
        int result = auth.login("09171234567", "000000");
        assertEquals(-1, result, "Wrong PIN should return -1");
    }

    @Test
    @DisplayName("Login – wrong mobile number returns -1")
    void testInvalidLoginWrongNumber() {
        int result = auth.login("09000000000", "123456");
        assertEquals(-1, result, "Non-existent number should return -1");
    }

    @Test
    @DisplayName("Login – empty number returns -1")
    void testLoginEmptyNumber() {
        int result = auth.login("", "123456");
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Login – empty PIN returns -1")
    void testLoginEmptyPin() {
        int result = auth.login("09171234567", "");
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Login – null credentials return -1")
    void testLoginNullCredentials() {
        assertEquals(-1, auth.login(null, null));
    }

    // ─── REGISTER TESTS ───────────────────────────────────────────────────

    @Test
    @DisplayName("Register – valid new user returns a positive ID")
    void testValidRegister() {
        int id = auth.register("Test User", "test@email.com", "09301234567", "999999");
        assertTrue(id > 0, "Valid registration should return a positive user ID");
    }

    @Test
    @DisplayName("Register – duplicate email returns -1")
    void testRegisterDuplicateEmail() {
        int result = auth.register("Duplicate", "juan@email.com", "09309999999", "111111");
        assertEquals(-1, result, "Duplicate email should be rejected");
    }

    @Test
    @DisplayName("Register – duplicate number returns -1")
    void testRegisterDuplicateNumber() {
        int result = auth.register("Duplicate", "new@email.com", "09171234567", "111111");
        assertEquals(-1, result, "Duplicate mobile number should be rejected");
    }

    @Test
    @DisplayName("Register – invalid email format returns -1")
    void testRegisterInvalidEmail() {
        int result = auth.register("Test", "notanemail", "09301111111", "123456");
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Register – PIN shorter than 6 digits returns -1")
    void testRegisterShortPin() {
        int result = auth.register("Test", "short@email.com", "09301111112", "123");
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Register – non-numeric PIN returns -1")
    void testRegisterAlphaPin() {
        int result = auth.register("Test", "alpha@email.com", "09301111113", "abc123");
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Register – mobile not starting with 09 returns -1")
    void testRegisterInvalidMobile() {
        int result = auth.register("Test", "mobile@email.com", "08171234567", "123456");
        assertEquals(-1, result);
    }

    // ─── CHANGE PIN TESTS ─────────────────────────────────────────────────

    @Test
    @DisplayName("changePin – valid change succeeds")
    void testChangePinSuccess() {
        auth.login("09171234567", "123456");
        boolean result = auth.changePin("123456", "999999");
        assertTrue(result);
    }

    @Test
    @DisplayName("changePin – new PIN works on next login")
    void testChangePinNewPinWorks() {
        auth.login("09171234567", "123456");
        auth.changePin("123456", "999999");
        auth.logout();
        int id = auth.login("09171234567", "999999");
        assertEquals(1, id, "Should log in successfully with new PIN");
    }

    @Test
    @DisplayName("changePin – wrong old PIN fails")
    void testChangePinWrongOldPin() {
        auth.login("09171234567", "123456");
        boolean result = auth.changePin("000000", "999999");
        assertFalse(result);
    }

    @Test
    @DisplayName("changePin – same PIN as old fails")
    void testChangePinSamePin() {
        auth.login("09171234567", "123456");
        boolean result = auth.changePin("123456", "123456");
        assertFalse(result);
    }

    @Test
    @DisplayName("changePin – without login fails")
    void testChangePinNotLoggedIn() {
        boolean result = auth.changePin("123456", "999999");
        assertFalse(result);
    }

    // ─── LOGOUT TESTS ─────────────────────────────────────────────────────

    @Test
    @DisplayName("logout – clears session")
    void testLogoutClearsSession() {
        auth.login("09171234567", "123456");
        auth.logout();
        assertNull(auth.getCurrentUser(), "Session should be null after logout");
    }
}
