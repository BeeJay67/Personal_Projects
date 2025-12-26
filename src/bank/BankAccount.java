// BankAccount.java
package bank;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;

public class BankAccount {

    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private final String username;

    private final String saltB64;      // Base64 salt
    private final String passHashB64;  // Base64 PBKDF2 hash

    private double balance;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String type;
    private final int accountNumber;

    private final List<String> transactionHistory = new ArrayList<>();

    public BankAccount(Scanner scanner, String username, String password) {
        this.username = username;

        this.saltB64 = makeSaltB64();
        this.passHashB64 = pbkdf2HashB64(password.toCharArray(), saltB64);
        balance = 0.0;

        System.out.print("Enter your first name\n> ");
        firstName = scanner.nextLine().trim();

        System.out.print("Enter your last name\n> ");
        lastName = scanner.nextLine().trim();

        System.out.print("Date of Birth\nMonth\n> ");
        short month = readShort(scanner);

        System.out.print("Day\n> ");
        short day = readShort(scanner);

        System.out.print("Year\n> ");
        short year = readShort(scanner);

        dateOfBirth = month + "/" + day + "/" + year;

        System.out.print("1. Checking account\n2. Savings account\n> ");
        short choiceType = readShort(scanner);
        type = (choiceType == 1) ? "Checking" : "Savings";

        accountNumber = Main.getNextAccountNumber();

        transactionHistory.add(now() + " | Account created");
    }
    public BankAccount(String username, String saltB64, String passHashB64,
                       String firstName, String lastName, String dob, String type, double balance) {

        this.username = username;
        this.saltB64 = saltB64;
        this.passHashB64 = passHashB64;

        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dob;
        this.type = type;
        this.balance = balance;
        transactionHistory.add(now() + " | Account loaded");
        this.accountNumber = Main.getNextAccountNumber();
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Cannot deposit non-positive amount.");
            transactionHistory.add(now() + " | FAILED Deposit " + MONEY.format(amount));
            return;
        }
        balance += amount;
        transactionHistory.add(now() + " | Deposited " + MONEY.format(amount));
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Cannot withdraw non-positive amount.");
            transactionHistory.add(now() + " | FAILED Withdrawal " + MONEY.format(amount));
            return;
        }
        if (amount > balance) {
            System.out.println("Cannot withdraw more than balance.");
            transactionHistory.add(now() + " | FAILED Withdrawal " + MONEY.format(amount));
            return;
        }
        balance -= amount;
        transactionHistory.add(now() + " | Withdrew " + MONEY.format(amount));
    }

    public String getBalance() {
        StringBuilder sb = new StringBuilder();
        sb.append("Balance: ").append(MONEY.format(balance)).append("\n");
        sb.append("Transaction History:\n");

        if (transactionHistory.isEmpty()) {
            sb.append("No history\n");
        } else {
            for (String t : transactionHistory) {
                sb.append(t).append("\n");
            }
        }
        return sb.toString();
    }

    public boolean checkPassword(String attempt) {
        String attemptHash = pbkdf2HashB64(attempt.toCharArray(), saltB64);
        // constant-time compare
        return MessageDigest.isEqual(
                Base64.getDecoder().decode(attemptHash),
                Base64.getDecoder().decode(passHashB64)
        );
    }

    public String getUsername() { return username; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getType() { return type; }
    public int getAccountNumber() { return accountNumber; }

    public double getRawBalance() { return balance; }

    public String getSaltB64() { return saltB64; }
    public String getPassHashB64() { return passHashB64; }

    // ---------- helpers ----------
    private static String now() {
        return LocalDateTime.now().format(TIME_FMT);
    }

    private static short readShort(Scanner scanner) {
        while (!scanner.hasNextShort()) {
            scanner.nextLine();
            System.out.print("> ");
        }
        short val = scanner.nextShort();
        scanner.nextLine(); // consume newline
        return val;
    }
    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 120_000; // reasonable
    private static final int KEY_BITS = 256;       // 256-bit hash

    private static String makeSaltB64() {
        byte[] salt = new byte[SALT_BYTES];
        RNG.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String pbkdf2HashB64(char[] password, String saltB64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltB64);
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
