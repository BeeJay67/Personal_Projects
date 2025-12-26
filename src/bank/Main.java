// Main.java
package bank;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final List<BankAccount> accounts = new ArrayList<>();
    private static int lastAccountNumber = 100000;

    static void main() {
        boolean running = true;
        loadAccounts();
        while (running) {
            System.out.println("\n=== BANK START ===");
            System.out.println("1. Create Account");
            System.out.println("2. Sign In");
            System.out.println("3. Exit");
            System.out.print("> ");

            int choice = readInt();

            switch (choice) {
                case 1 -> createAccountFlow();
                case 2 -> signInFlow();
                case 3 -> {
                    saveAccounts(); running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }

        System.out.println("Goodbye.");
    }

    // Called by BankAccount constructor to assign unique account numbers
    public static int getNextAccountNumber() {
        lastAccountNumber++;
        return lastAccountNumber;
    }

    private static void createAccountFlow() {
        System.out.println("\n=== CREATE ACCOUNT ===");

        // Make sure username isn't taken
        String username;
        while (true) {
            System.out.print("Create a username\n> ");
            username = readLine();
            if (username.isBlank()) {
                System.out.println("Username cannot be blank.");
                continue;
            }
            if (findByUsername(username) != null) {
                System.out.println("That username is already taken.");
                continue;
            }
            break;
        }

        String password;
        while (true) {
            System.out.print("Create a password\n> ");
            password = readLine();
            if (password.isBlank()) {
                System.out.println("Password cannot be blank.");
                continue;
            }
            break;
        }

        // This continues with your existing prompts (name, DOB, type...)
        BankAccount newAcc = new BankAccount(scanner, username, password);
        accounts.add(newAcc);

        System.out.println("\nAccount created!");
        System.out.println("Account Number: " + newAcc.getAccountNumber());

        // After creating, you can drop them right into their account menu
        accountMenu(newAcc);
    }

    private static void signInFlow() {
        System.out.println("\n=== SIGN IN ===");

        System.out.print("Username\n> ");
        String username = readLine();

        System.out.print("Password\n> ");
        String password = readLine();

        BankAccount acc = findByUsername(username);
        if (acc == null || !acc.checkPassword(password)) {
            System.out.println("Invalid username or password.");
            return; // back to start screen
        }

        System.out.println("Signed in.");
        accountMenu(acc);
    }

    private static void accountMenu(BankAccount account) {
        boolean inAccount = true;

        while (inAccount) {
            System.out.println("\n=== ACCOUNT MENU ===");
            System.out.println("User: " + account.getUsername());
            System.out.println("Account #: " + account.getAccountNumber());
            System.out.println("Type: " + account.getType());
            System.out.println("Name: " + account.getFullName());
            System.out.println("DOB: " + account.getDateOfBirth());

            System.out.println("\n1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. View Balance + History");
            System.out.println("4. Sign Out (Back to Start)");
            System.out.print("> ");

            int choice = readInt();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter amount to deposit\n> ");
                    double amount = readDouble();
                    account.deposit(amount);
                }
                case 2 -> {
                    System.out.print("Enter amount to withdraw\n> ");
                    double amount = readDouble();
                    account.withdraw(amount);
                }
                case 3 -> System.out.println(account.getBalance());
                case 4 -> inAccount = false; // goes back to start screen
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static BankAccount findByUsername(String username) {
        for (BankAccount acc : accounts) {
            if (acc.getUsername().equals(username)) {
                return acc;
            }
        }
        return null;
    }

    private static void saveAccounts() {
        try (FileWriter writer = new FileWriter("accounts.txt")) {
            for( BankAccount acc : accounts) {
                writer.write(
                        acc.getUsername() + "|" +
                                acc.getSaltB64() + "|" +
                                acc.getPassHashB64() + "|" +
                                acc.getFirstName() + "|" +
                                acc.getLastName() + "|" +
                                acc.getDateOfBirth() + "|" +
                                acc.getType() + "|" +
                                acc.getRawBalance() + "\n"
                );
            }
        } catch (IOException e) {
            System.out.println("Error saving accounts.");
        }
    }
    private static void loadAccounts() {

        File file = new File("accounts.txt");
        if (!file.exists()) {
            System.out.println("No save file found.");
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            int loaded = 0;

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length != 8) {
                    System.out.println("Skipping bad line (wrong field count): " + line);
                    continue;
                }

                double bal;
                try {
                    bal = Double.parseDouble(parts[7].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Skipping bad line (bad balance): " + parts[7]);
                    continue;
                }

                BankAccount acc = new BankAccount(
                        parts[0], // username
                        parts[1], // saltB64
                        parts[2], // passHashB64
                        parts[3], // firstName
                        parts[4], // lastName
                        parts[5], // dob
                        parts[6], // type
                        bal       // balance
                );

                accounts.add(acc);
                loaded++;
            }

            System.out.println("Loaded " + loaded + " accounts.");

        } catch (FileNotFoundException e) {
            System.out.println("Could not read accounts.txt");
        }
    }

    private static String readLine() {
        String line = scanner.nextLine();
        while (line.isEmpty()) {
            line = scanner.nextLine();
        }
        return line.trim();
    }

    private static int readInt() {
        while (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.print("> ");
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }

    private static double readDouble() {
        while (!scanner.hasNextDouble()) {
            scanner.nextLine();
            System.out.print("> ");
        }
        double val = scanner.nextDouble();
        scanner.nextLine();
        return val;
    }
}
