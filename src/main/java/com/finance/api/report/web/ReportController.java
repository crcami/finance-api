package com.finance.api.report.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Reports", description = "Financial reports for the current user")
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
            @Parameter(description = "Start date (defaults to 90 days ago)",
                    schema = @Schema(type = "string", format = "date"), example = "2025-07-25")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (defaults to today)",
                    schema = @Schema(type = "string", format = "date"), example = "2025-10-23")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filter by category IDs (repeat param: ?categoryIds=...&categoryIds=...)",
                    array = @ArraySchema(schema = @Schema(format = "uuid")))
            @RequestParam(required = false) List<UUID> categoryIds,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        var out = reports.summary(userId, defFrom(from), defTo(to), categoryIds);
        return ApiResponse.ok(out);
    }

    @Operation(summary = "Monthly cashflow within a period")
    @GetMapping("/cashflow")
    public ApiResponse<CashflowReport> cashflow(
            @Parameter(description = "Start date (defaults to 90 days ago)",
                    schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (defaults to today)",
                    schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filter by category IDs (repeat param)",
                    array = @ArraySchema(schema = @Schema(format = "uuid")))
            @RequestParam(required = false) List<UUID> categoryIds,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        var out = reports.cashflow(userId, defFrom(from), defTo(to), categoryIds);
        return ApiResponse.ok(out);
    }

    @Operation(summary = "Totals by category within a period (pageable)")
    @GetMapping("/by-category")
    public ApiResponse<PageResponse<CategoryReportItem>> byCategory(
            @Parameter(description = "Start date (defaults to 90 days ago)",
                    schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (defaults to today)",
                    schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filter by category IDs (repeat param)",
                    array = @ArraySchema(schema = @Schema(format = "uuid")))
            @RequestParam(required = false) List<UUID> categoryIds,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        var page = reports.byCategory(userId, defFrom(from), defTo(to), categoryIds, pageable);
        return ApiResponse.ok(page);
    }

    @Operation(summary = "Naive forecast using 3-month moving average")
    @GetMapping("/forecast")
    public ApiResponse<List<ForecastItem>> forecast(
            @Parameter(description = "Start date (defaults to 90 days ago)",
                    schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (defaults to today)",
                    schema = @Schema(type = "string", format = "date"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filter by category IDs (repeat param)",
                    array = @ArraySchema(schema = @Schema(format = "uuid")))
            @RequestParam(required = false) List<UUID> categoryIds,
            @Parameter(description = "How many months to forecast ahead",
                    schema = @Schema(minimum = "1", maximum = "24", defaultValue = "3"),
                    example = "3")
            @RequestParam(defaultValue = "3") int monthsAhead,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        var out = reports.forecast(userId, defFrom(from), defTo(to), categoryIds, monthsAhead);
        return ApiResponse.ok(out);
    }
}
