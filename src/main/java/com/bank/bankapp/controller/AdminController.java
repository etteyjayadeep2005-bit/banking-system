package com.bank.bankapp.controller;

import com.bank.bankapp.model.Account;
import com.bank.bankapp.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AccountService service;

    public AdminController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/accounts")
    public List<Account> allAccounts() {
        return service.listAll();
    }
}