package com.finance.api.report.domain;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CashflowItem(
    YearMonth month,
    BigDecimal income,
    BigDecimal expense,
    BigDecimal balance
) { }
