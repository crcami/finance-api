package com.finance.api.report.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.common.api.ApiResponse;
import com.finance.api.common.api.PageResponse;
import com.finance.api.report.application.ReportService;
import com.finance.api.report.domain.CashflowReport;
import com.finance.api.report.domain.CategoryReportItem;
import com.finance.api.report.domain.ForecastItem;
import com.finance.api.report.domain.SummaryReport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "bearer-jwt")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    private static LocalDate defFrom(LocalDate from) {
        return from != null ? from : LocalDate.now().minusDays(90);
    }

    private static LocalDate defTo(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }

    @Operation(summary = "Summary (income, expense, balance) for a period")
    @GetMapping("/summary")
    public ApiResponse<SummaryReport> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<UUID> categoryIds,
            Authentication auth) {

        UUID userId = (UUID) auth.getPrincipal();
        var out = reports.summary(userId, defFrom(from), defTo(to), categoryIds);
        return ApiResponse.ok(out);
    }

    @Operation(summary = "Monthly cashflow within a period")
    @GetMapping("/cashflow")
    public ApiResponse<CashflowReport> cashflow(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<UUID> categoryIds,
            Authentication auth) {

        UUID userId = (UUID) auth.getPrincipal();
        var out = reports.cashflow(userId, defFrom(from), defTo(to), categoryIds);
        return ApiResponse.ok(out);
    }

    @Operation(summary = "Totals by category within a period (pageable)")
    @GetMapping("/by-category")
    public ApiResponse<PageResponse<CategoryReportItem>> byCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<UUID> categoryIds,
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            Authentication auth) {

        UUID userId = (UUID) auth.getPrincipal();
        var page = reports.byCategory(userId, defFrom(from), defTo(to), categoryIds, pageable);
        return ApiResponse.ok(page);
    }

    @Operation(summary = "Naive forecast using 3-month moving average")
    @GetMapping("/forecast")
    public ApiResponse<List<ForecastItem>> forecast(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<UUID> categoryIds,
            @RequestParam(defaultValue = "3") int monthsAhead,
            Authentication auth) {

        UUID userId = (UUID) auth.getPrincipal();
        var out = reports.forecast(userId, defFrom(from), defTo(to), categoryIds, monthsAhead);
        return ApiResponse.ok(out);
    }
}
