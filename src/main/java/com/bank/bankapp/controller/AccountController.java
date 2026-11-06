package com.bank.bankapp.controller;

import com.bank.bankapp.dto.AmountRequest;
import com.bank.bankapp.dto.CreateAccountRequest;
import com.bank.bankapp.dto.TransferRequest;
import com.bank.bankapp.exception.ForbiddenException;
import com.bank.bankapp.model.Account;
import com.bank.bankapp.model.BankTransaction;
import com.bank.bankapp.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    // Account tana dena leda admin aa ani check chestundi
    private void checkAccess(String number, Authentication auth) {
        Account account = service.get(number);
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !auth.getName().equals(account.getOwnerUsername())) {
            throw new ForbiddenException("You do not have access to this account");
        }
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@Valid @RequestBody CreateAccountRequest req, Authentication auth) {
        return service.create(req.holderName(), req.initialDeposit(), auth.getName());
    }

    @GetMapping("/accounts")
    public List<Account> myAccounts(Authentication auth) {
        return service.listByOwner(auth.getName());
    }

    @GetMapping("/accounts/{number}")
    public Account get(@PathVariable String number, Authentication auth) {
        checkAccess(number, auth);
        return service.get(number);
    }

    @PostMapping("/accounts/{number}/deposit")
    public Account deposit(@PathVariable String number, @Valid @RequestBody AmountRequest req,
                           Authentication auth) {
        checkAccess(number, auth);
        return service.deposit(number, req.amount());
    }

    @PostMapping("/accounts/{number}/withdraw")
    public Account withdraw(@PathVariable String number, @Valid @RequestBody AmountRequest req,
                            Authentication auth) {
        checkAccess(number, auth);
        return service.withdraw(number, req.amount());
    }

    @PostMapping("/transfers")
    public Map<String, String> transfer(@Valid @RequestBody TransferRequest req, Authentication auth) {
        checkAccess(req.fromAccount(), auth); // pampe account tana dey ayi undali
        service.transfer(req.fromAccount(), req.toAccount(), req.amount());
        return Map.of("message", "Transfer successful");
    }

    @GetMapping("/accounts/{number}/transactions")
    public List<BankTransaction> history(@PathVariable String number, Authentication auth) {
        checkAccess(number, auth);
        return service.history(number);
    }
}