package com.finance.api.report.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.common.api.PageResponse;
import com.finance.api.report.domain.CashflowItem;
import com.finance.api.report.domain.CashflowReport;
import com.finance.api.report.domain.CategoryReportItem;
import com.finance.api.report.domain.ForecastItem;
import com.finance.api.report.domain.SummaryReport;
import com.finance.api.report.persistence.ReportQueries;

@Service
public class ReportService {

    private final ReportQueries queries;

    public ReportService(ReportQueries queries) {
        this.queries = queries;
    }

    @Transactional(readOnly = true)
    public SummaryReport summary(UUID userId, LocalDate from, LocalDate to, List<UUID> categoryIds) {
        var row = queries.querySummary(userId, from, to, categoryIds);
        BigDecimal income = row.income();
        BigDecimal expense = row.expense();
        return new SummaryReport(income, expense, income.subtract(expense));
    }

    @Transactional(readOnly = true)
    public CashflowReport cashflow(UUID userId, LocalDate from, LocalDate to, List<UUID> categoryIds) {
        var rows = queries.queryCashflow(userId, from, to, categoryIds);
        var items = rows.stream()
                .map(r -> new CashflowItem(r.month(), r.income(), r.expense(), r.income().subtract(r.expense())))
                .toList();

        BigDecimal totalIncome = items.stream().map(CashflowItem::income).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = items.stream().map(CashflowItem::expense).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CashflowReport(items, totalIncome, totalExpense, totalIncome.subtract(totalExpense));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryReportItem> byCategory(UUID userId, LocalDate from, LocalDate to,
            List<UUID> categoryIds, Pageable pageable) {
        var rows = queries.queryByCategory(userId, from, to, categoryIds);
        var items = rows.stream()
                .map(r -> new CategoryReportItem(
                r.id(), r.name(), r.income(), r.expense(), r.income().subtract(r.expense())))
                .toList();

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int start = Math.min(page * size, items.size());
        int end = Math.min(start + size, items.size());
        List<CategoryReportItem> slice = items.subList(start, end);
        int totalPages = (int) Math.ceil(items.size() / (double) size);
        return new PageResponse<>(slice, page, size, items.size(), totalPages);
    }

    @Transactional(readOnly = true)
    public List<ForecastItem> forecast(UUID userId, LocalDate from, LocalDate to,
            List<UUID> categoryIds, int monthsAhead) {
        var rows = queries.queryCashflow(userId, from, to, categoryIds);

        Map<YearMonth, BigDecimal[]> map = rows.stream().collect(Collectors.toMap(
                r -> r.month(),
                r -> new BigDecimal[]{r.income(), r.expense()},
                (a, b) -> a,
                TreeMap::new
        ));

        int window = 3;
        YearMonth last = map.isEmpty() ? YearMonth.now() : map.keySet().stream().reduce((a, b) -> b).orElse(YearMonth.now());
        List<ForecastItem> out = new ArrayList<>();

        for (int i = 1; i <= monthsAhead; i++) {
            last = last.plusMonths(1);

            BigDecimal avgIncome = movingAverage(map.values().stream().map(v -> v[0]).toList(), window);
            BigDecimal avgExpense = movingAverage(map.values().stream().map(v -> v[1]).toList(), window);
            var item = new ForecastItem(last, avgIncome, avgExpense, avgIncome.subtract(avgExpense));
            out.add(item);
            map.put(last, new BigDecimal[]{avgIncome, avgExpense});
        }
        return out;
    }

    private static BigDecimal movingAverage(List<BigDecimal> values, int window) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int n = Math.min(values.size(), window);
        return values.subList(values.size() - n, values.size())
                .stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(n), java.math.RoundingMode.HALF_UP);
    }
}
