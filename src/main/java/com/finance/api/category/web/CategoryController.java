package com.finance.api.category.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.category.application.CategoryService;
import com.finance.api.category.domain.CategoryPatchRequest;
import com.finance.api.category.domain.CategoryRequest;
import com.finance.api.category.domain.CategoryResponse;
import com.finance.api.common.api.ApiResponse;
import com.finance.api.common.api.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
@SecurityRequirement(name = "bearer-jwt")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new category for the current user")
    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest in,
            Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok("Created", service.create(userId, in));
    }

    @Operation(summary = "List categories of the current user (filterable and pageable)")
    @GetMapping
    public ApiResponse<PageResponse<CategoryResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean archived,
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            Authentication auth) {

        UUID userId = (UUID) auth.getPrincipal();
        Page<CategoryResponse> page = service.list(userId, q, archived, pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }

    @Operation(summary = "Get a category by id")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok(service.get(userId, id));
    }

    @Operation(summary = "Update a category")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable UUID id,
            @Valid @RequestBody CategoryRequest in,
            Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok("Updated", service.update(userId, id, in));
    }

    @Operation(summary = "Archive or unarchive a category")
    @PatchMapping("/{id}")
    public ApiResponse<CategoryResponse> patch(@PathVariable UUID id,
            @Valid @RequestBody CategoryPatchRequest in,
            Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.ok("Patched", service.patch(userId, id, in));
    }

    @Operation(summary = "Delete a category")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        service.delete(userId, id);
        return ApiResponse.ok("Deleted", null);
    }
}
