package com.finance.api.report.domain;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ForecastItem(
    YearMonth month,
    BigDecimal projectedIncome,
    BigDecimal projectedExpense,
    BigDecimal projectedBalance
) { }
