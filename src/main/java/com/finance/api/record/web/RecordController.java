package com.finance.api.record.web;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.common.api.ApiResponse;
import com.finance.api.record.application.RecordService;
import com.finance.api.record.domain.BulkResult;
import com.finance.api.record.domain.RecordKind;
import com.finance.api.record.domain.RecordRequest;
import com.finance.api.record.domain.RecordResponse;
import com.finance.api.record.domain.RecordStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/records")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Records", description = "Income/expense records management")
public class RecordController {

    private static final Logger log = LoggerFactory.getLogger(RecordController.class);
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    private static UUID resolveUserId(Authentication auth) {
        try { return UUID.fromString(auth.getName()); } catch (Exception ignore) {}
        Object p = auth.getPrincipal();
        if (p instanceof UUID u) return u;
        if (p instanceof String s) return UUID.fromString(s);
        throw new IllegalStateException("Unsupported principal type: " + (p == null ? "null" : p.getClass()));
    }

  
    @Operation(
        summary = "List records (paged) with month or date range filters",
        description = "Filter by month (yyyy-MM) or by startDate/endDate; optional filters: status, kind, categoryId."
    )
    @GetMapping
    public ApiResponse<Page<RecordResponse>> list(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(
                name = "month",
                description = "Year-month formatted as yyyy-MM (takes precedence over start/end)",
                schema = @Schema(pattern = "^[0-9]{4}-[0-9]{2}$")
            )
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) RecordKind kind,
            @RequestParam(required = false) UUID categoryId,
            @ParameterObject @PageableDefault(size = 50, sort = "dueDate") Pageable pageable
    ) {
        UUID userId = resolveUserId(auth);
        Optional<YearMonth> ym = Optional.ofNullable(month).filter(s -> !s.isBlank()).map(s -> YearMonth.parse(s, YM));
        Page<RecordResponse> page = service.list(userId,
                Optional.ofNullable(startDate),
                Optional.ofNullable(endDate),
                ym,
                Optional.ofNullable(status),
                Optional.ofNullable(kind),
                Optional.ofNullable(categoryId),
                pageable);
        return ApiResponse.ok(page);
    }

    @Operation(summary = "Bulk create records (idempotent by X-Request-Id)")
    @PostMapping("/bulk")
    public ApiResponse<BulkResult> bulkCreate(
            @Valid @RequestBody List<@Valid RecordRequest> items,
            @Parameter(name = "X-Request-Id", description = "Idempotency key to safely retry the same request", in = ParameterIn.HEADER)
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = resolveUserId(auth);
        log.debug("bulkCreate userId={}, count={}, reqId={}", userId, items == null ? 0 : items.size(), requestId);
        var result = service.bulkCreate(userId, items, requestId);
        return ApiResponse.ok("Bulk processed", result);
    }

    @Operation(summary = "Get record by id")
    @GetMapping("/{id}")
    public ApiResponse<RecordResponse> get(@PathVariable UUID id, @Parameter(hidden = true) Authentication auth) {
        UUID userId = resolveUserId(auth);
        return ApiResponse.ok(service.get(userId, id));
    }

    @Operation(summary = "Update record by id")
    @PutMapping("/{id}")
    public ApiResponse<RecordResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RecordRequest in,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = resolveUserId(auth);
        return ApiResponse.ok("Updated", service.update(userId, id, in));
    }

    @Operation(summary = "Delete many records (body is array of UUIDs)")
    @DeleteMapping
    public ApiResponse<java.util.Map<String, Long>> deleteMany(
            @RequestBody List<UUID> ids,
            @Parameter(hidden = true) Authentication auth
    ) {
        UUID userId = resolveUserId(auth);
        long deleted = service.deleteMany(userId, ids);
        return ApiResponse.ok("Deleted", java.util.Map.of("deleted", deleted));
    }

    @Operation(summary = "Confirm record (paid/received) depending on kind")
    @PatchMapping("/{id}/confirm")
    public ApiResponse<RecordResponse> confirm(@PathVariable UUID id, @Parameter(hidden = true) Authentication auth) {
        UUID userId = resolveUserId(auth);
        return ApiResponse.ok("Confirmed", service.confirm(userId, id));
    }
}
