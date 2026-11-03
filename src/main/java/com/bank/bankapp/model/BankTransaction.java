package com.bank.bankapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private String type; // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    private BigDecimal amount;
    private LocalDateTime createdAt = LocalDateTime.now();

    public BankTransaction() {}

    public BankTransaction(String accountNumber, String type, BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}