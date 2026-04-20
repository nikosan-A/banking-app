package com.gcashapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CashTransferTest
 * Extends BaseTest (Inheritance).
 *
 * Java 8 used:
 *   Polymorphism – BankingOperation[] array loops over CashIn + CashTransfer
 *   Lambda       – assertAll(), assertOperation() with Supplier
 *   Predicate    – VALID_AMOUNT, VALID_NOTE, isSent(), isReceived(), composed
 */
@DisplayName("CashTransfer Tests")
class CashTransferTest extends BaseTest {

    // ─── POLYMORPHISM ─────────────────────────────────────────────────────

    @Test
    @DisplayName("cashTransfer – polymorphic execute() via BankingOperation interface")
    void testCashTransferViaPolymorphicInterface() {
        BankingOperation op = cashTransfer
                .from(1).to(2).amount(500.00).note("Polymorphism test");

        assertTrue(executeOperation(op),   // inherited from BaseTest
                   "CashTransfer.execute() via BankingOperation should succeed");
    }

    @Test
    @DisplayName("Polymorphism – BankingOperation[] handles both CashIn and CashTransfer")
    void testPolymorphicOperationArray() {
        // Array of mixed subtypes — classic polymorphism
        BankingOperation[] ops = {
            cashIn.forUser(1).amount(100.00).description("Op1").from(0),
            cashIn.forUser(2).amount(200.00).description("Op2").from(0),
            cashTransfer.from(1).to(3).amount(50.00).note("Op3")
        };

        for (BankingOperation op : ops) {
            assertTrue(executeOperation(op), op.getOperationName() + " must succeed");
        }

        // 2 cash-ins + 2 transfer records (SENT + RECEIVED) = 4
        assertEquals(4, transactions.viewAll().size(),
                     "Polymorphic ops should log 4 total transaction records");
    }

    // ─── BOTH BALANCES UPDATE ─────────────────────────────────────────────

    @Test
    @DisplayName("cashTransfer – sender balance decreases and recipient increases correctly")
    void testBothBalancesUpdateCorrectly() {
        double senderBefore    = checkBalance.checkBalance(1);   // 5000.00
        double recipientBefore = checkBalance.checkBalance(2);   // 12500.75

        cashTransfer.cashTransfer(1, 2, 1000.00, "Bayad sa utang");

        assertAll("both balances update",
            () -> assertWith(checkBalance.checkBalance(1),
                             equalsAmount(senderBefore - 1000),    "Sender: −PHP 1000"),
            () -> assertWith(checkBalance.checkBalance(2),
                             equalsAmount(recipientBefore + 1000), "Recipient: +PHP 1000")
        );
    }

    @Test
    @DisplayName("cashTransfer – total money across both accounts is conserved")
    void testTotalMoneyConserved() {
        double totalBefore = checkBalance.checkBalance(1) + checkBalance.checkBalance(2);
        cashTransfer.cashTransfer(1, 2, 500.00, "Conservation test");
        double totalAfter  = checkBalance.checkBalance(1) + checkBalance.checkBalance(2);

        assertWith(totalAfter, equalsAmount(totalBefore),
                   "No money should be created or destroyed");
    }

    @Test
    @DisplayName("cashTransfer – chain 1→2 then 2→3 updates all three balances")
    void testChainedTransfers() {
        double b1 = checkBalance.checkBalance(1);
        double b2 = checkBalance.checkBalance(2);
        double b3 = checkBalance.checkBalance(3);

        cashTransfer.cashTransfer(1, 2, 200.00, "Hop1");
        cashTransfer.cashTransfer(2, 3, 100.00, "Hop2");

        assertAll("chained transfers",
            () -> assertWith(checkBalance.checkBalance(1), equalsAmount(b1 - 200),       "Juan  −200"),
            () -> assertWith(checkBalance.checkBalance(2), equalsAmount(b2 + 200 - 100), "Maria +200−100"),
            () -> assertWith(checkBalance.checkBalance(3), equalsAmount(b3 + 100),        "Pedro +100")
        );
    }

    // ─── TRANSACTION LOGGING ──────────────────────────────────────────────

    @Test
    @DisplayName("cashTransfer – logs SENT for sender and RECEIVED for recipient")
    void testBothTransactionRecordsLogged() {
        cashTransfer.cashTransfer(1, 2, 500.00, "Log test");
        List<Transactions.Transaction> all = transactions.viewAll();

        long sent     = countWhere(all, Transactions.byUser(1).and(Transactions.isSent()));
        long received = countWhere(all, Transactions.byUser(2).and(Transactions.isReceived()));

        assertAll("transaction records",
            () -> assertEquals(1L, sent,     "Sender must have 1 SENT record"),
            () -> assertEquals(1L, received, "Recipient must have 1 RECEIVED record")
        );
    }

    // ─── VALIDATION PREDICATES ────────────────────────────────────────────

    @Test
    @DisplayName("CashTransfer predicates – VALID_AMOUNT and VALID_NOTE")
    void testTransferPredicates() {
        Predicate<Double> validAmt  = CashTransfer.VALID_AMOUNT;
        Predicate<String> validNote = CashTransfer.VALID_NOTE;

        assertAll("transfer predicates",
            () -> assertTrue (validAmt.test(1.00),           "PHP 1.00 min"),
            () -> assertTrue (validAmt.test(50000.00),       "PHP 50,000 max"),
            () -> assertFalse(validAmt.test(0.99),           "PHP 0.99 below min"),
            () -> assertFalse(validAmt.test(50000.01),       "PHP 50,000.01 above max"),
            () -> assertFalse(validAmt.test(null),           "null invalid"),
            () -> assertTrue (validNote.test("Short note"),  "short note valid"),
            () -> assertFalse(validNote.test("A".repeat(51)),"51-char note invalid")
        );
    }

    // ─── RESTRICTION SCENARIOS ────────────────────────────────────────────

    @Test
    @DisplayName("cashTransfer – all restrictions reject and balances stay unchanged")
    void testAllRestrictionsWithBalanceIntegrity() {
        double s = checkBalance.checkBalance(1);
        double r = checkBalance.checkBalance(2);

        assertAll("restriction rejections",
            () -> assertOperation(() -> cashTransfer.cashTransfer(1, 1,  100,   "self"),       false, "self-transfer"),
            () -> assertOperation(() -> cashTransfer.cashTransfer(1, 2,  99999, "insuff"),     false, "insufficient"),
            () -> assertOperation(() -> cashTransfer.cashTransfer(1, 2,  60000, "over max"),   false, "over max"),
            () -> assertOperation(() -> cashTransfer.cashTransfer(1, 2,  0.50,  "under min"),  false, "under min"),
            () -> assertOperation(() -> cashTransfer.cashTransfer(1, 99, 100,   "ghost"),      false, "unknown recipient"),
            () -> assertOperation(() -> cashTransfer.cashTransfer(-1, 2, 100,   "bad sender"), false, "invalid sender"),
            () -> assertOperation(() -> cashTransfer.cashTransfer(1, 0,  100,   "zero recip"), false, "recipient id=0")
        );

        assertAll("balances unchanged after all rejections",
            () -> assertWith(checkBalance.checkBalance(1), equalsAmount(s), "Sender unchanged"),
            () -> assertWith(checkBalance.checkBalance(2), equalsAmount(r), "Recipient unchanged")
        );
    }
}
