package com.bank.bankapp.service;

import com.bank.bankapp.exception.BadRequestException;
import com.bank.bankapp.exception.NotFoundException;
import com.bank.bankapp.model.Account;
import com.bank.bankapp.model.BankTransaction;
import com.bank.bankapp.repository.AccountRepository;
import com.bank.bankapp.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final BankTransactionRepository transactions;

    public AccountService(AccountRepository accounts, BankTransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public Account create(String holderName, BigDecimal initialDeposit) {
        return create(holderName, initialDeposit, null);
    }

    @Transactional
    public Account create(String holderName, BigDecimal initialDeposit, String ownerUsername) {
        Account a = new Account();
        a.setHolderName(holderName);
        a.setOwnerUsername(ownerUsername);
        a.setAccountNumber("SB" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L));
        a.setBalance(initialDeposit);
        Account saved = accounts.save(a);
        if (initialDeposit.signum() > 0) {
            transactions.save(new BankTransaction(saved.getAccountNumber(), "DEPOSIT", initialDeposit));
        }
        return saved;
    }

    public Account get(String accountNumber) {
        return accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountNumber));
    }

    public List<Account> listByOwner(String ownerUsername) {
        return accounts.findByOwnerUsernameOrderByIdAsc(ownerUsername);
    }

    public List<Account> listAll() {
        return accounts.findAll();
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account a = get(accountNumber);
        a.setBalance(a.getBalance().add(amount));
        transactions.save(new BankTransaction(accountNumber, "DEPOSIT", amount));
        return accounts.save(a);
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account a = get(accountNumber);
        if (a.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }
        a.setBalance(a.getBalance().subtract(amount));
        transactions.save(new BankTransaction(accountNumber, "WITHDRAW", amount));
        return accounts.save(a);
    }

    // Transfer: remaina okati fail aithe, rendu rollback avutayi (@Transactional valla)
    @Transactional
    public void transfer(String from, String to, BigDecimal amount) {
        if (from.equals(to)) {
            throw new BadRequestException("Cannot transfer to the same account");
        }
        Account source = get(from);
        Account target = get(to);
        if (source.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }
        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));
        accounts.save(source);
        accounts.save(target);
        transactions.save(new BankTransaction(from, "TRANSFER_OUT", amount));
        transactions.save(new BankTransaction(to, "TRANSFER_IN", amount));
    }

    public List<BankTransaction> history(String accountNumber) {
        get(accountNumber); // account unda leda check
        return transactions.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }
}