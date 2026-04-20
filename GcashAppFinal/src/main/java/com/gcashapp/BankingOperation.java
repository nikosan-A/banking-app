package com.gcashapp;

/**
 * BankingOperation – Polymorphism interface.
 *
 * CashIn and CashTransfer implement this so tests can
 * treat both operations through a single type.
 */
public interface BankingOperation {
    /** Execute the operation. Returns true on success. */
    boolean execute();

    /** Human-readable operation name. */
    String getOperationName();
}
