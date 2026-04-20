package com.gcashapp;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BaseTest – Abstract superclass (Inheritance root for all test classes).
 *
 * ── Java 8 features ───────────────────────────────────────────────────────
 *  Inheritance  : all 5 test classes extend this
 *  Predicate<T> : reusable assertion helpers defined here
 *  Lambda       : Supplier<Boolean> for assertOperation()
 *  Stream       : countWhere(), filterList()
 * ─────────────────────────────────────────────────────────────────────────
 */
public abstract class BaseTest {

    // ── Shared objects – fresh for every test via @BeforeEach ─────────────
    protected UserAuthentication auth;
    protected CheckBalance       checkBalance;
    protected Transactions       transactions;
    protected CashIn             cashIn;
    protected CashTransfer       cashTransfer;

    @BeforeEach
    void baseSetUp() {
        auth         = new UserAuthentication();
        checkBalance = new CheckBalance();
        transactions = new Transactions();
        cashIn       = new CashIn(checkBalance, transactions);
        cashTransfer = new CashTransfer(checkBalance, transactions);
    }

    // ── Predicate factories (inherited by all subclasses) ─────────────────

    /** True when |actual − expected| < 0.001. */
    protected Predicate<Double> equalsAmount(double expected) {
        return actual -> Math.abs(actual - expected) < 0.001;
    }

    /** True when value > 0. */
    protected final Predicate<Double>  isPositive  = v -> v > 0;

    /** True when value == -1.0 (our error sentinel). */
    protected final Predicate<Double>  isErrorResult = v -> Math.abs(v - (-1.0)) < 0.001;

    /** True when id > 0 (valid user/txn ID). */
    protected final Predicate<Integer> isValidId   = id -> id > 0;

    /** True when id == -1 (failure indicator). */
    protected final Predicate<Integer> isFailureId = id -> id == -1;

    // ── Assertion helpers ─────────────────────────────────────────────────

    /**
     * assertWith – fails if predicate.test(value) is false.
     * Replaces verbose assertEquals/assertTrue pairs.
     */
    protected <T> void assertWith(T value, Predicate<T> predicate, String message) {
        assertTrue(predicate.test(value), message + "  (actual: " + value + ")");
    }

    /**
     * assertOperation – runs a lambda and asserts its boolean result.
     * Usage: assertOperation(() -> cashIn.cashIn(1, 200, "desc", 0), true, "msg");
     */
    protected void assertOperation(Supplier<Boolean> operation, boolean expected, String message) {
        assertEquals(expected, operation.get(), message);
    }

    // ── Stream helpers ────────────────────────────────────────────────────

    /** Count elements matching a Predicate (Java 8 stream). */
    protected <T> long countWhere(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).count();
    }

    /** Filter a list and return matches (Java 8 stream). */
    protected <T> List<T> filterList(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    // ── Polymorphism helper ───────────────────────────────────────────────

    /**
     * executeOperation – accepts any BankingOperation polymorphically.
     * Works for both CashIn and CashTransfer through the same method.
     */
    protected boolean executeOperation(BankingOperation op) {
        System.out.println("[TEST] Running: " + op.getOperationName());
        return op.execute();
    }
}
