package com.finance.api.report.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryReportItem(
    UUID categoryId,
    String name,
    BigDecimal income,
    BigDecimal expense,
    BigDecimal net
) { }
