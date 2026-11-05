package com.bank.bankapp.service;

import com.bank.bankapp.exception.BadRequestException;
import com.bank.bankapp.exception.NotFoundException;
import com.bank.bankapp.model.Account;
import com.bank.bankapp.model.BankTransaction;
import com.bank.bankapp.repository.AccountRepository;
import com.bank.bankapp.repository.BankTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accounts;

    @Mock
    private BankTransactionRepository transactions;

    @InjectMocks
    private AccountService service;

    private Account account(String number, String balance) {
        Account a = new Account();
        a.setAccountNumber(number);
        a.setHolderName("Test");
        a.setBalance(new BigDecimal(balance));
        return a;
    }

    @Test
    void createAccountStartsWithInitialDepositAndRecordsTransaction() {
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account created = service.create("Ravi", new BigDecimal("5000"));

        assertThat(created.getAccountNumber()).startsWith("SB");
        assertThat(created.getHolderName()).isEqualTo("Ravi");
        assertThat(created.getBalance()).isEqualByComparingTo("5000");
        verify(transactions).save(any(BankTransaction.class));
    }

    @Test
    void createAccountWithZeroDepositRecordsNoTransaction() {
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create("Sita", BigDecimal.ZERO);

        verify(transactions, never()).save(any(BankTransaction.class));
    }

    @Test
    void getUnknownAccountThrowsNotFound() {
        when(accounts.findByAccountNumber("SB404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("SB404"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("SB404");
    }

    @Test
    void depositIncreasesBalance() {
        Account a = account("SB1", "1000");
        when(accounts.findByAccountNumber("SB1")).thenReturn(Optional.of(a));
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.deposit("SB1", new BigDecimal("500"));

        assertThat(result.getBalance()).isEqualByComparingTo("1500");
        verify(transactions).save(any(BankTransaction.class));
    }

    @Test
    void withdrawReducesBalance() {
        Account a = account("SB1", "1000");
        when(accounts.findByAccountNumber("SB1")).thenReturn(Optional.of(a));
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.withdraw("SB1", new BigDecimal("400"));

        assertThat(result.getBalance()).isEqualByComparingTo("600");
        verify(transactions).save(any(BankTransaction.class));
    }

    @Test
    void withdrawMoreThanBalanceThrowsAndSavesNothing() {
        Account a = account("SB1", "100");
        when(accounts.findByAccountNumber("SB1")).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.withdraw("SB1", new BigDecimal("500")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient balance");

        assertThat(a.getBalance()).isEqualByComparingTo("100");
        verify(accounts, never()).save(any(Account.class));
        verify(transactions, never()).save(any(BankTransaction.class));
    }

    @Test
    void transferMovesMoneyBetweenAccounts() {
        Account from = account("SB1", "1000");
        Account to = account("SB2", "200");
        when(accounts.findByAccountNumber("SB1")).thenReturn(Optional.of(from));
        when(accounts.findByAccountNumber("SB2")).thenReturn(Optional.of(to));

        service.transfer("SB1", "SB2", new BigDecimal("300"));

        assertThat(from.getBalance()).isEqualByComparingTo("700");
        assertThat(to.getBalance()).isEqualByComparingTo("500");
        verify(transactions, times(2)).save(any(BankTransaction.class));
    }

    @Test
    void transferToSameAccountIsRejected() {
        assertThatThrownBy(() -> service.transfer("SB1", "SB1", new BigDecimal("50")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot transfer to the same account");
    }

    @Test
    void transferWithInsufficientBalanceChangesNothing() {
        Account from = account("SB1", "100");
        Account to = account("SB2", "200");
        when(accounts.findByAccountNumber("SB1")).thenReturn(Optional.of(from));
        when(accounts.findByAccountNumber("SB2")).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> service.transfer("SB1", "SB2", new BigDecimal("500")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient balance");

        assertThat(from.getBalance()).isEqualByComparingTo("100");
        assertThat(to.getBalance()).isEqualByComparingTo("200");
        verify(accounts, never()).save(any(Account.class));
    }
}