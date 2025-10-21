package com.finance.api.category.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.category.domain.CategoryPatchRequest;
import com.finance.api.category.domain.CategoryRequest;
import com.finance.api.category.domain.CategoryResponse;
import com.finance.api.category.persistence.CategoryEntity;
import com.finance.api.category.persistence.CategoryRepository;
import com.finance.api.common.exception.BadRequestException;
import com.finance.api.common.exception.NotFoundException;
import com.finance.api.common.util.SpecificationUtils;

@Service
public class CategoryService {

  private final CategoryRepository repo;

  public CategoryService(CategoryRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public CategoryResponse create(UUID userId, CategoryRequest in) {
    if (repo.existsByUserIdAndNameIgnoreCase(userId, in.name())) {
      throw new BadRequestException("Category name already exists");
    }
    var e = new CategoryEntity();
    e.setUserId(userId);
    e.setName(in.name());
    e.setColor(blankToNull(in.color()));
    e.setArchived(false);
    return toResponse(repo.save(e));
  }

  @Transactional(readOnly = true)
  public Page<CategoryResponse> list(UUID userId, String q, Boolean archived, Pageable pageable) {
    Specification<CategoryEntity> spec = SpecificationUtils.and(
        SpecificationUtils.ownedBy("userId", userId),
        SpecificationUtils.likeInsensitiveIfNotBlank("name", q),
        SpecificationUtils.equalIfNotNull("archived", archived)
    );
    return repo.findAll(spec, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public CategoryResponse get(UUID userId, UUID id) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Category not found"));
    return toResponse(e);
  }

  @Transactional
  public CategoryResponse update(UUID userId, UUID id, CategoryRequest in) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Category not found"));

    if (!e.getName().equalsIgnoreCase(in.name())
        && repo.existsByUserIdAndNameIgnoreCase(userId, in.name())) {
      throw new BadRequestException("Category name already exists");
    }

    e.setName(in.name());
    e.setColor(blankToNull(in.color()));
    return toResponse(repo.save(e));
  }

  @Transactional
  public CategoryResponse patch(UUID userId, UUID id, CategoryPatchRequest in) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Category not found"));
    e.setArchived(Boolean.TRUE.equals(in.archived()));
    return toResponse(repo.save(e));
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Category not found"));
    repo.delete(e);
  }


  private CategoryResponse toResponse(CategoryEntity e) {
    return new CategoryResponse(e.getId(), e.getName(), e.getColor(), e.isArchived());
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
