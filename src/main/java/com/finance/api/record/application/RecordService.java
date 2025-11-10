package com.finance.api.record.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.common.exception.NotFoundException;
import com.finance.api.record.domain.BulkResult;
import com.finance.api.record.domain.RecordKind;
import com.finance.api.record.domain.RecordRequest;
import com.finance.api.record.domain.RecordResponse;
import com.finance.api.record.domain.RecordStatus;
import com.finance.api.record.persistence.BulkRequestEntity;
import com.finance.api.record.persistence.BulkRequestRepository;
import com.finance.api.record.persistence.RecordEntity;
import com.finance.api.record.persistence.RecordRepository;
import com.finance.api.record.persistence.RecordSpecifications;


@Service
public class RecordService {

  private final RecordRepository repo;
  private final BulkRequestRepository bulkRepo;

  public RecordService(RecordRepository repo, BulkRequestRepository bulkRepo) {
    this.repo = repo;
    this.bulkRepo = bulkRepo;
  }

  @Transactional(readOnly = true)
  public Page<RecordResponse> list(
      UUID userId,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate,
      Optional<YearMonth> month,
      Optional<RecordStatus> status,
      Optional<RecordKind> kind,
      Optional<UUID> categoryId,
      Pageable pageable) {

    Specification<RecordEntity> spec = Specification.where(RecordSpecifications.belongsTo(userId));

    if (month.isPresent()) {
      spec = spec.and(RecordSpecifications.monthEquals(month.get()));
    } else if (startDate.isPresent() || endDate.isPresent()) {
      LocalDate from = startDate.orElse(LocalDate.MIN);
      LocalDate to = endDate.orElse(LocalDate.MAX);
      spec = spec.and(RecordSpecifications.dueDateBetween(from, to));
    }

    if (status.isPresent())    spec = spec.and(RecordSpecifications.hasStatus(status.get()));
    if (kind.isPresent())      spec = spec.and(RecordSpecifications.hasKind(kind.get()));
    if (categoryId.isPresent()) spec = spec.and(RecordSpecifications.hasCategory(categoryId.get()));

    Page<RecordEntity> page = repo.findAll(spec, pageable);
    return page.map(RecordService::toResponse);
  }

  @Transactional(readOnly = true)
  public RecordResponse get(UUID userId, UUID id) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Record not found"));
    return toResponse(e);
  }

  @Transactional
  public RecordResponse update(UUID userId, UUID id, RecordRequest in) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Record not found"));
    e.setKind(in.kind());
    e.setStatus(in.status());
    e.setAmount(in.amount());
    e.setDueDate(in.dueDate());
    e.setCategoryId(in.categoryId());
    e.setDescription(in.description());
    return toResponse(repo.save(e));
  }

  @Transactional
  public long deleteMany(UUID userId, List<UUID> ids) {
    if (ids == null || ids.isEmpty()) return 0;
    return repo.deleteByUserIdAndIdIn(userId, ids);
  }

  @Transactional
  public BulkResult bulkCreate(UUID userId, List<RecordRequest> items, String requestId) {
    if (requestId != null && !requestId.isBlank()) {
      var existing = bulkRepo.findByUserIdAndRequestId(userId, requestId).orElse(null);
      if (existing != null) return new BulkResult(existing.getCreatedCount(), existing.getFailedCount());
    }

    int created = 0, failed = 0;
    var batch = new ArrayList<RecordEntity>();

    for (var in : items) {
      try {
        var e = new RecordEntity();
        e.setUserId(userId);
        e.setCategoryId(in.categoryId());
        e.setKind(in.kind());
        e.setStatus(in.status());
        e.setAmount(in.amount());
        e.setDueDate(in.dueDate());
        e.setDescription(in.description());
        batch.add(e);
        created++;
      } catch (Exception ex) {
        failed++;
      }
    }

    if (!batch.isEmpty()) repo.saveAll(batch);

    if (requestId != null && !requestId.isBlank()) {
      var br = new BulkRequestEntity();
      br.setUserId(userId);
      br.setRequestId(requestId);
      br.setCreatedCount(created);
      br.setFailedCount(failed);
      br.setProcessedAt(Instant.now());
      bulkRepo.save(br);
    }

    return new BulkResult(created, failed);
  }

  @Transactional
  public RecordResponse confirm(UUID userId, UUID id) {
    var e = repo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Record not found"));
    switch (e.getKind()) {
      case EXPENSE -> e.setStatus(RecordStatus.PAID);
      case INCOME  -> e.setStatus(RecordStatus.RECEIVED);
    }
    e.setPaidAt(Instant.now());
    return toResponse(repo.save(e));
  }

  private static RecordResponse toResponse(RecordEntity e) {
    return new RecordResponse(
        e.getId(), e.getCategoryId(), e.getKind(), e.getStatus(),
        e.getAmount(), e.getDueDate(), e.getPaidAt(), e.getDescription());
  }
}
