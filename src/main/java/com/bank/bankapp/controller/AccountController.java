package com.bank.bankapp.controller;

import com.bank.bankapp.dto.AmountRequest;
import com.bank.bankapp.dto.CreateAccountRequest;
import com.bank.bankapp.dto.TransferRequest;
import com.bank.bankapp.model.Account;
import com.bank.bankapp.model.BankTransaction;
import com.bank.bankapp.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@Valid @RequestBody CreateAccountRequest req) {
        return service.create(req.holderName(), req.initialDeposit());
    }

    @GetMapping("/accounts/{number}")
    public Account get(@PathVariable String number) {
        return service.get(number);
    }

    @PostMapping("/accounts/{number}/deposit")
    public Account deposit(@PathVariable String number, @Valid @RequestBody AmountRequest req) {
        return service.deposit(number, req.amount());
    }

    @PostMapping("/accounts/{number}/withdraw")
    public Account withdraw(@PathVariable String number, @Valid @RequestBody AmountRequest req) {
        return service.withdraw(number, req.amount());
    }

    @PostMapping("/transfers")
    public Map<String, String> transfer(@Valid @RequestBody TransferRequest req) {
        service.transfer(req.fromAccount(), req.toAccount(), req.amount());
        return Map.of("message", "Transfer successful");
    }

    @GetMapping("/accounts/{number}/transactions")
    public List<BankTransaction> history(@PathVariable String number) {
        return service.history(number);
    }
}