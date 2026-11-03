package com.bank.bankapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String holderName,
        @NotNull @DecimalMin("0.00") BigDecimal initialDeposit) {}