package com.gcashapp;

import java.util.Scanner;

/**
 * GcashApp – Main CLI Entry Point
 *
 * Wires all classes together and provides a terminal-based
 * interactive banking experience.
 *
 * Run: mvn package && java -jar target/GcashApp-1.0.jar
 *   or: mvn exec:java -Dexec.mainClass="com.gcashapp.GcashApp"
 *
 * Demo credentials:
 *   09171234567 / PIN 123456  (Juan)
 *   09189999999 / PIN 654321  (Maria)
 */
public class GcashApp {

    // ── Shared singletons ─────────────────────────────────────────────────
    private static final UserAuthentication auth         = new UserAuthentication();
    private static final CheckBalance       checkBalance = new CheckBalance();
    private static final Transactions       transactions = new Transactions();
    private static final CashIn             cashIn       = new CashIn(checkBalance, transactions);
    private static final CashTransfer       cashTransfer = new CashTransfer(checkBalance, transactions);

    private static final Scanner sc = new Scanner(System.in);

    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            int choice = showStartMenu();
            switch (choice) {
                case 1: handleLogin();    break;
                case 2: handleRegister(); break;
                case 3:
                    running = false;
                    System.out.println("\n  Thank you for using GcashApp. Goodbye!\n");
                    break;
                default: System.out.println("  [!] Invalid choice. Try again.\n");
            }
        }
        sc.close();
    }

    // ── START MENU ────────────────────────────────────────────────────────
    private static int showStartMenu() {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║          GCASH APP MENU          ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  [1] Login                       ║");
        System.out.println("║  [2] Register                    ║");
        System.out.println("║  [3] Exit                        ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("  Choice: ");
        return readInt();
    }

    // ── REGISTER ─────────────────────────────────────────────────────────
    private static void handleRegister() {
        System.out.println("\n─── REGISTER ───────────────────────────────");
        System.out.print("  Full Name     : "); String name   = sc.nextLine().trim();
        System.out.print("  Email         : "); String email  = sc.nextLine().trim();
        System.out.print("  Mobile (09xx) : "); String number = sc.nextLine().trim();
        System.out.print("  PIN (6 digits): "); String pin    = sc.nextLine().trim();

        int newId = auth.register(name, email, number, pin);
        if (newId != -1) {
            checkBalance.addBalanceRecord(newId);
            System.out.println("  Account created! You may now log in.\n");
        }
    }

    // ── LOGIN → BANKING MENU ──────────────────────────────────────────────
    private static void handleLogin() {
        System.out.println("\n─── LOGIN ──────────────────────────────────");
        System.out.print("  Mobile Number : "); String number = sc.nextLine().trim();
        System.out.print("  PIN           : "); String pin    = sc.nextLine().trim();

        int userId = auth.login(number, pin);
        if (userId == -1) { System.out.println(); return; }

        UserAuthentication.User user = auth.getCurrentUser();
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            int choice = showBankingMenu(user.getName());
            System.out.println();

            switch (choice) {
                case 1:
                    System.out.println("─── CHECK BALANCE ──────────────────────────");
                    checkBalance.checkBalance(userId);
                    break;

                case 2:
                    System.out.println("─── CASH-IN ────────────────────────────────");
                    System.out.print("  Amount (PHP)  : "); double ciAmt  = readDouble();
                    System.out.print("  Description   : "); String ciDesc = sc.nextLine().trim();
                    cashIn.cashIn(userId, ciAmt, ciDesc, 0);
                    break;

                case 3:
                    System.out.println("─── CASH TRANSFER ──────────────────────────");
                    System.out.print("  Recipient ID  : "); int    recId = readInt();
                    System.out.print("  Amount (PHP)  : "); double ctAmt = readDouble();
                    System.out.print("  Note          : "); String note  = sc.nextLine().trim();
                    cashTransfer.cashTransfer(userId, recId, ctAmt, note);
                    break;

                case 4:
                    System.out.println("─── MY TRANSACTIONS ────────────────────────");
                    transactions.viewUserAll(userId);
                    break;

                case 5:
                    System.out.println("─── ALL TRANSACTIONS ───────────────────────");
                    transactions.viewAll();
                    break;

                case 6:
                    System.out.println("─── VIEW TRANSACTION BY ID ─────────────────");
                    System.out.print("  Transaction ID: "); int txnId = readInt();
                    transactions.viewTransaction(txnId);
                    break;

                case 7:
                    System.out.println("─── CHANGE PIN ─────────────────────────────");
                    System.out.print("  Old PIN       : "); String oldPin = sc.nextLine().trim();
                    System.out.print("  New PIN       : "); String newPin = sc.nextLine().trim();
                    auth.changePin(oldPin, newPin);
                    break;

                case 8:
                    auth.logout();
                    loggedIn = false;
                    System.out.println();
                    continue;

                default:
                    System.out.println("  [!] Invalid choice.\n");
                    continue;
            }

            if (loggedIn) {
                System.out.print("\n  Do another transaction? (y/n): ");
                String again = sc.nextLine().trim();
                if (!again.equalsIgnoreCase("y")) {
                    auth.logout();
                    loggedIn = false;
                }
            }
        }
    }

    // ── BANKING MENU ──────────────────────────────────────────────────────
    private static int showBankingMenu(String name) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.printf ("║  Hello, %-25s║%n", name + "!");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  [1] Check Balance               ║");
        System.out.println("║  [2] Cash-In                     ║");
        System.out.println("║  [3] Transfer                    ║");
        System.out.println("║  [4] My Transactions             ║");
        System.out.println("║  [5] All Transactions            ║");
        System.out.println("║  [6] View Transaction by ID      ║");
        System.out.println("║  [7] Change PIN                  ║");
        System.out.println("║  [8] Logout                      ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("  Choice: ");
        return readInt();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static int readInt() {
        try   { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private static double readDouble() {
        try   { return Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════╗");
        System.out.println("  ║     GcashApp – Online Banking CLI      ║");
        System.out.println("  ╠════════════════════════════════════════╣");
        System.out.println("  ║  Demo: 09171234567  PIN 123456  (Juan) ║");
        System.out.println("  ║         09189999999  PIN 654321 (Maria)║");
        System.out.println("  ╚════════════════════════════════════════╝");
        System.out.println();
    }
}
