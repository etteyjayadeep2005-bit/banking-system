package com.bank.bankapp.repository;

import com.bank.bankapp.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
}