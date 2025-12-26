Bank Account Management System (Java)

A console-based banking application written in Java that simulates account creation, authentication, and basic financial transactions. The project focuses on clean object-oriented design, secure password handling, and persistent data storage.

Overview

This program allows users to create accounts, log in securely, deposit and withdraw funds

Features

Create and manage multiple bank accounts
Secure login using hashed passwords
Deposit and withdraw funds
Transaction history tracking
Persistent data storage using file I/O

Security

Passwords are never stored in plain text. Each account uses a unique salt and is hashed using PBKDF2 with HMAC SHA-256. Password comparisons use constant-time checks to reduce timing attack risks.

Concepts Used

Object-oriented programming
Constructor overloading
File input/output
Password hashing and validation
Collections and data management
Error handling

Technologies

Java
IntelliJ IDEA
Standard Java libraries
