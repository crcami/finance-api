package com.finance.api.record.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.common.api.ApiResponse;
import com.finance.api.record.application.RecordService;
import com.finance.api.record.domain.BulkResult;
import com.finance.api.record.domain.RecordRequest;
import com.finance.api.record.domain.RecordResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/records")
@SecurityRequirement(name = "bearer-jwt")
public class RecordController {

    private final RecordService service;

    public RecordController(RecordService service) {
        this.service = service;
    }

    @Operation(summary = "Bulk create records (idempotent by X-Request-Id)")
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkResult>> bulkCreate(
            @Valid @RequestBody List<RecordRequest> items,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            Authentication auth) {

        UUID userId = (UUID) auth.getPrincipal();
        var result = service.bulkCreate(userId, items, requestId);
        return ResponseEntity.ok(ApiResponse.ok("Bulk processed", result));
    }

    @Operation(summary = "Get record by id")
    @GetMapping("/{id}")
    public ApiResponse<RecordResponse> get(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok(service.get(userId, id));
    }

    @Operation(summary = "Update record by id")
    @PutMapping("/{id}")
    public ApiResponse<RecordResponse> update(@PathVariable UUID id,
            @Valid @RequestBody RecordRequest in,
            Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok("Updated", service.update(userId, id, in));
    }

    @Operation(summary = "Delete many records (body is array of UUIDs)")
    @DeleteMapping
    public ApiResponse<?> deleteMany(@RequestBody List<UUID> ids, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        long deleted = service.deleteMany(userId, ids);
        return ApiResponse.ok("Deleted", java.util.Map.of("deleted", deleted));
    }

    @Operation(summary = "Confirm record (paid/received) depending on kind")
    @PatchMapping("/{id}/confirm")
    public ApiResponse<RecordResponse> confirm(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok("Confirmed", service.confirm(userId, id));
    }
}
