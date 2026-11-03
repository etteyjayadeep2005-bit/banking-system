package com.bank.bankapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AmountRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {}