package com.gcashapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserAuthenticationTest
 * Extends BaseTest (Inheritance).
 *
 * Java 8 used:
 *   Lambda   – assertAll(), assertOperation() with Supplier lambdas
 *   Predicate – isValidId, isFailureId, VALID_EMAIL, VALID_PIN, VALID_NUMBER, VALID_NAME
 */
@DisplayName("UserAuthentication Tests")
class UserAuthenticationTest extends BaseTest {

    // ─── VALID LOGIN ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Login – valid credentials return correct user ID")
    void testValidLoginReturnsId() {
        int result = auth.login("09171234567", "123456");

        assertAll("valid login checks",
            () -> assertWith(result, isValidId,      "Should return a positive ID"),
            () -> assertEquals(1, result,             "Juan's ID is 1"),
            () -> assertNotNull(auth.getCurrentUser(),"Session must be set"),
            () -> assertEquals("Juan dela Cruz",
                               auth.getCurrentUser().getName(), "Name must match")
        );
    }

    @Test
    @DisplayName("Login – Maria's valid credentials return ID 2")
    void testValidLoginMaria() {
        int result = auth.login("09189999999", "654321");
        assertAll("Maria valid login",
            () -> assertWith(result, isValidId, "Maria's ID should be valid (>0)"),
            () -> assertEquals(2, result,        "Maria's ID is 2")
        );
    }

    // ─── INVALID LOGIN ────────────────────────────────────────────────────

    @Test
    @DisplayName("Login – wrong PIN returns -1 and leaves no session")
    void testInvalidLoginWrongPin() {
        int result = auth.login("09171234567", "000000");
        assertAll("wrong PIN",
            () -> assertWith(result, isFailureId,       "Wrong PIN → -1"),
            () -> assertNull(auth.getCurrentUser(),     "No session after failed login")
        );
    }

    @Test
    @DisplayName("Login – wrong mobile number returns -1")
    void testInvalidLoginWrongNumber() {
        assertWith(auth.login("09000000000", "123456"), isFailureId,
                   "Non-existent number must return -1");
    }

    @Test
    @DisplayName("Login – null and empty credentials all return -1")
    void testLoginEdgeCases() {
        assertAll("edge case credentials",
            () -> assertWith(auth.login(null, null),          isFailureId, "null/null"),
            () -> assertWith(auth.login(null, "123456"),      isFailureId, "null number"),
            () -> assertWith(auth.login("09171234567", null), isFailureId, "null PIN"),
            () -> assertWith(auth.login("", "123456"),        isFailureId, "empty number"),
            () -> assertWith(auth.login("09171234567", ""),   isFailureId, "empty PIN")
        );
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Register – valid user returns positive ID")
    void testValidRegister() {
        // Lambda: assertOperation wraps the call
        assertOperation(
            () -> auth.register("New User", "new@email.com", "09301234567", "999999") > 0,
            true, "Valid registration should return a positive ID"
        );
    }

    @Test
    @DisplayName("Register – static Predicates correctly validate each field")
    void testStaticValidationPredicates() {
        // Directly test the exposed Java 8 Predicates
        Predicate<String> email  = UserAuthentication.VALID_EMAIL;
        Predicate<String> pin    = UserAuthentication.VALID_PIN;
        Predicate<String> number = UserAuthentication.VALID_NUMBER;
        Predicate<String> name   = UserAuthentication.VALID_NAME;

        assertAll("static predicate validation",
            () -> assertTrue(email.test("good@email.com"),        "valid email"),
            () -> assertFalse(email.test("notanemail"),           "invalid email"),
            () -> assertTrue(pin.test("123456"),                  "valid 6-digit PIN"),
            () -> assertFalse(pin.test("12345"),                  "5-digit PIN fails"),
            () -> assertFalse(pin.test("abcdef"),                 "alpha PIN fails"),
            () -> assertTrue(number.test("09171234567"),          "valid PH number"),
            () -> assertFalse(number.test("08171234567"),         "wrong prefix fails"),
            () -> assertFalse(number.test("0917123456"),          "10-digit fails"),
            () -> assertTrue(name.test("Juan"),                   "non-empty name"),
            () -> assertFalse(name.test(""),                      "empty name fails"),
            () -> assertFalse(name.test(null),                    "null name fails")
        );
    }

    @Test
    @DisplayName("Register – duplicates are rejected")
    void testRegisterDuplicates() {
        assertAll("duplicate rejections",
            () -> assertWith(
                    auth.register("Other", "juan@email.com", "09309999999", "111111"),
                    isFailureId, "duplicate email"),
            () -> assertWith(
                    auth.register("Other", "other@email.com", "09171234567", "111111"),
                    isFailureId, "duplicate number")
        );
    }

    // ─── CHANGE PIN ───────────────────────────────────────────────────────

    @Test
    @DisplayName("changePin – new PIN works on subsequent login")
    void testChangePinThenReLogin() {
        auth.login("09171234567", "123456");
        assertTrue(auth.changePin("123456", "999999"), "PIN change should succeed");
        auth.logout();

        // Lambda: verify new PIN works
        assertOperation(() -> auth.login("09171234567", "999999") > 0,
                        true, "New PIN must allow login");
    }

    @Test
    @DisplayName("changePin – invalid scenarios all fail")
    void testChangePinInvalidScenarios() {
        auth.login("09171234567", "123456");
        assertAll("changePin failures",
            () -> assertFalse(auth.changePin("WRONG", "999999"),   "wrong old PIN"),
            () -> assertFalse(auth.changePin("123456", "123456"),  "same PIN rejected"),
            () -> assertFalse(auth.changePin("123456", "12345"),   "5-digit new PIN")
        );
    }

    @Test
    @DisplayName("changePin – fails when not logged in")
    void testChangePinNoSession() {
        assertFalse(auth.changePin("123456", "999999"),
                    "changePin must fail without an active session");
    }

    // ─── LOGOUT ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout – clears session to null")
    void testLogoutClearsSession() {
        auth.login("09171234567", "123456");
        assertNotNull(auth.getCurrentUser(), "Session exists before logout");
        auth.logout();
        assertNull(auth.getCurrentUser(), "Session must be null after logout");
    }
}
