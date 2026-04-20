package com.gcashapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CheckBalanceTest
 * Extends BaseTest (Inheritance).
 *
 * Java 8 used:
 *   Predicate – equalsAmount(), isPositive, isErrorResult, belongsTo(), composed predicates
 *   Lambda    – assertAll() with lambdas, predicate composition (.and(), .negate(), .or())
 */
@DisplayName("CheckBalance Tests")
class CheckBalanceTest extends BaseTest {

    // ─── BALANCE MATCHES DATABASE ─────────────────────────────────────────

    @Test
    @DisplayName("checkBalance – all seeded users return exact database values")
    void testAllSeedBalancesMatchDatabase() {
        assertAll("seeded balance values match database",
            () -> assertWith(checkBalance.checkBalance(1), equalsAmount(5000.00),   "User 1: PHP 5,000.00"),
            () -> assertWith(checkBalance.checkBalance(2), equalsAmount(12500.75),  "User 2: PHP 12,500.75"),
            () -> assertWith(checkBalance.checkBalance(3), equalsAmount(300.50),    "User 3: PHP 300.50"),
            () -> assertWith(checkBalance.checkBalance(4), equalsAmount(99999.99),  "User 4: PHP 99,999.99")
        );
    }

    @Test
    @DisplayName("checkBalance – all user balances are positive")
    void testAllBalancesArePositive() {
        assertAll("positive balances",
            () -> assertWith(checkBalance.checkBalance(1), isPositive, "User 1 positive"),
            () -> assertWith(checkBalance.checkBalance(2), isPositive, "User 2 positive"),
            () -> assertWith(checkBalance.checkBalance(3), isPositive, "User 3 positive"),
            () -> assertWith(checkBalance.checkBalance(4), isPositive, "User 4 positive")
        );
    }

    // ─── belongsTo PREDICATE ──────────────────────────────────────────────

    @Test
    @DisplayName("belongsTo – static Predicate correctly identifies balance row owners")
    void testBelongsToPredicateFiltersCorrectly() {
        Predicate<CheckBalance.Balance> isUser1 = CheckBalance.belongsTo(1);
        Predicate<CheckBalance.Balance> isUser2 = CheckBalance.belongsTo(2);

        CheckBalance.Balance bal1 = checkBalance.findByUserId(1);
        CheckBalance.Balance bal2 = checkBalance.findByUserId(2);

        assertAll("belongsTo predicate",
            () -> assertTrue (isUser1.test(bal1), "bal1 belongs to User 1"),
            () -> assertFalse(isUser1.test(bal2), "bal2 does NOT belong to User 1"),
            () -> assertTrue (isUser2.test(bal2), "bal2 belongs to User 2"),
            () -> assertFalse(isUser2.test(bal1), "bal1 does NOT belong to User 2")
        );
    }

    // ─── PREDICATE COMPOSITION ────────────────────────────────────────────

    @Test
    @DisplayName("Predicate .and() – User 2 belongs to user AND has high balance")
    void testPredicateCompositionAnd() {
        Predicate<CheckBalance.Balance> isUser2   = CheckBalance.belongsTo(2);
        Predicate<CheckBalance.Balance> highBal   = b -> b.getAmount() > 10000;
        Predicate<CheckBalance.Balance> combined  = isUser2.and(highBal);

        assertTrue(combined.test(checkBalance.findByUserId(2)),
                   "User 2 should have balance > PHP 10,000");
    }

    @Test
    @DisplayName("Predicate .negate() – User 3 does NOT have high balance")
    void testPredicateNegation() {
        Predicate<CheckBalance.Balance> highBal = b -> b.getAmount() > 10000;

        assertTrue(highBal.negate().test(checkBalance.findByUserId(3)),
                   "User 3 (PHP 300.50) should fail the high-balance predicate");
    }

    @Test
    @DisplayName("Predicate .or() – User 1 OR User 2 match their respective predicates")
    void testPredicateOr() {
        Predicate<CheckBalance.Balance> isUser1 = CheckBalance.belongsTo(1);
        Predicate<CheckBalance.Balance> isUser2 = CheckBalance.belongsTo(2);
        Predicate<CheckBalance.Balance> either  = isUser1.or(isUser2);

        assertAll("OR predicate",
            () -> assertTrue (either.test(checkBalance.findByUserId(1)), "User 1 passes OR"),
            () -> assertTrue (either.test(checkBalance.findByUserId(2)), "User 2 passes OR"),
            () -> assertFalse(either.test(checkBalance.findByUserId(3)), "User 3 fails OR")
        );
    }

    // ─── EDGE CASES ───────────────────────────────────────────────────────

    @Test
    @DisplayName("checkBalance – invalid IDs all return -1.0")
    void testInvalidIdsReturnErrorSentinel() {
        assertAll("invalid IDs",
            () -> assertWith(checkBalance.checkBalance(0),   isErrorResult, "ID 0"),
            () -> assertWith(checkBalance.checkBalance(-1),  isErrorResult, "ID -1"),
            () -> assertWith(checkBalance.checkBalance(99),  isErrorResult, "non-existent")
        );
    }

    // ─── BALANCE REFLECTS UPDATES ─────────────────────────────────────────

    @Test
    @DisplayName("checkBalance – reflects setAmount() immediately")
    void testBalanceReflectsSetAmount() {
        checkBalance.findByUserId(1).setAmount(8888.00);
        assertWith(checkBalance.checkBalance(1), equalsAmount(8888.00),
                   "checkBalance must return the updated amount");
    }

    @Test
    @DisplayName("addBalanceRecord – new user starts at PHP 0.00")
    void testNewUserStartsAtZero() {
        checkBalance.addBalanceRecord(10);
        assertWith(checkBalance.checkBalance(10), equalsAmount(0.00),
                   "Newly added balance record must start at PHP 0.00");
    }
}
