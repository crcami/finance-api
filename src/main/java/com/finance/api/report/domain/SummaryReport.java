package com.finance.api.report.domain;

import java.math.BigDecimal;

public record SummaryReport(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance
) { }
