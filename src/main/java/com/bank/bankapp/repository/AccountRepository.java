package com.bank.bankapp.repository;

import com.bank.bankapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByOwnerUsernameOrderByIdAsc(String ownerUsername);
}