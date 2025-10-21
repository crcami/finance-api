package com.finance.api.report.domain;

import java.math.BigDecimal;
import java.util.List;

public record CashflowReport(
    List<CashflowItem> items,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance
) { }
