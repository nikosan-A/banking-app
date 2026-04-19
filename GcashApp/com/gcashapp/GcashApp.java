package com.gcashapp;

import java.util.Scanner;

/**
 * GcashApp – Main Class
 *
 * Combines all banking objects into a single CLI application.
 *
 * Program Flow:
 *   1. Login or Register
 *   2. Main menu: Check Balance | Cash-In | Transfer | View Transactions | Change PIN
 *   3. After each transaction → ask for another
 *   4. Logout
 */
public class GcashApp {

    // ── Shared objects (all classes use these same instances) ─────────────
    private static UserAuthentication auth         = new UserAuthentication();
    private static CheckBalance       checkBalance = new CheckBalance();
    private static Transactions       transactions = new Transactions();
    private static CashIn             cashIn       = new CashIn(checkBalance, transactions);
    private static CashTransfer       cashTransfer = new CashTransfer(checkBalance, transactions);

    private static Scanner sc = new Scanner(System.in);

    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();

        boolean running = true;
        while (running) {
            int choice = showStartMenu();
            switch (choice) {
                case 1: handleLogin();    break;
                case 2: handleRegister(); break;
                case 3: running = false;
                        System.out.println("\n  Thank you for using GcashApp. Goodbye!\n");
                        break;
                default: System.out.println("  [!] Invalid option. Try again.");
            }
        }
        sc.close();
    }

    // ─────────────────────────────────────────────────────────────────────
    // START MENU
    // ─────────────────────────────────────────────────────────────────────
    private static int showStartMenu() {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║         GCASH APP MENU           ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  [1] Login                       ║");
        System.out.println("║  [2] Register                    ║");
        System.out.println("║  [3] Exit                        ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("  Enter choice: ");
        return readInt();
    }

    // ─────────────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────────────
    private static void handleRegister() {
        System.out.println("\n─── REGISTER ───────────────────────────────");
        System.out.print("  Full Name     : "); String name   = sc.nextLine().trim();
        System.out.print("  Email         : "); String email  = sc.nextLine().trim();
        System.out.print("  Mobile (09xx) : "); String number = sc.nextLine().trim();
        System.out.print("  PIN (6 digits): "); String pin    = sc.nextLine().trim();

        int newId = auth.register(name, email, number, pin);
        if (newId != -1) {
            // Create a balance record for the new user starting at PHP 0
            checkBalance.addBalanceRecord(newId);
            System.out.println("  Account ready! You may now log in.\n");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LOGIN → MAIN BANKING MENU
    // ─────────────────────────────────────────────────────────────────────
    private static void handleLogin() {
        System.out.println("\n─── LOGIN ──────────────────────────────────");
        System.out.print("  Mobile Number : "); String number = sc.nextLine().trim();
        System.out.print("  PIN           : "); String pin    = sc.nextLine().trim();

        int userId = auth.login(number, pin);
        if (userId == -1) { System.out.println(); return; }

        UserAuthentication.User user = auth.getCurrentUser();

        // ── Main banking loop ─────────────────────────────────────────────
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            int choice = showBankingMenu(user.getName());
            System.out.println();

            switch (choice) {
                case 1: // Check Balance
                    System.out.println("─── CHECK BALANCE ──────────────────────────");
                    checkBalance.checkBalance(userId);
                    break;

                case 2: // Cash-In
                    System.out.println("─── CASH-IN ────────────────────────────────");
                    System.out.print("  Amount (PHP)  : "); double ciAmt = readDouble();
                    System.out.print("  Description   : "); String ciDesc = sc.nextLine().trim();
                    cashIn.cashIn(userId, ciAmt, ciDesc, 0);
                    break;

                case 3: // Transfer
                    System.out.println("─── CASH TRANSFER ──────────────────────────");
                    System.out.print("  Recipient ID  : "); int recId = readInt();
                    System.out.print("  Amount (PHP)  : "); double ctAmt = readDouble();
                    System.out.print("  Note          : "); String note = sc.nextLine().trim();
                    cashTransfer.cashTransfer(userId, recId, ctAmt, note);
                    break;

                case 4: // View My Transactions
                    System.out.println("─── MY TRANSACTIONS ────────────────────────");
                    transactions.viewUserAll(userId);
                    break;

                case 5: // View All Transactions (admin view)
                    System.out.println("─── ALL TRANSACTIONS ───────────────────────");
                    transactions.viewAll();
                    break;

                case 6: // View Single Transaction
                    System.out.println("─── VIEW TRANSACTION ───────────────────────");
                    System.out.print("  Transaction ID: "); int txnId = readInt();
                    transactions.viewTransaction(txnId);
                    break;

                case 7: // Change PIN
                    System.out.println("─── CHANGE PIN ─────────────────────────────");
                    System.out.print("  Old PIN       : "); String oldPin = sc.nextLine().trim();
                    System.out.print("  New PIN       : "); String newPin = sc.nextLine().trim();
                    auth.changePin(oldPin, newPin);
                    break;

                case 8: // Logout
                    auth.logout();
                    loggedIn = false;
                    System.out.println();
                    continue;

                default:
                    System.out.println("  [!] Invalid option. Try again.");
                    continue;
            }

            // ── Ask for another transaction ───────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────
    // BANKING MENU
    // ─────────────────────────────────────────────────────────────────────
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
        System.out.print("  Enter choice: ");
        return readInt();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────
    private static int readInt() {
        try {
            int val = Integer.parseInt(sc.nextLine().trim());
            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double readDouble() {
        try {
            double val = Double.parseDouble(sc.nextLine().trim());
            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ██████╗  ██████╗ █████╗ ███████╗██╗  ██╗");
        System.out.println(" ██╔════╝ ██╔════╝██╔══██╗██╔════╝██║  ██║");
        System.out.println(" ██║  ███╗██║     ███████║███████╗███████║");
        System.out.println(" ██║   ██║██║     ██╔══██║╚════██║██╔══██║");
        System.out.println(" ╚██████╔╝╚██████╗██║  ██║███████║██║  ██║");
        System.out.println("  ╚═════╝  ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝");
        System.out.println("       Online Banking App  |  GcashApp");
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  Demo accounts: 09171234567 PIN 123456");
        System.out.println("                 09189999999 PIN 654321");
        System.out.println("  ─────────────────────────────────────────\n");
    }
}
