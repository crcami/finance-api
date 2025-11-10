package com.finance.api.record.persistence;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.finance.api.record.domain.RecordKind;
import com.finance.api.record.domain.RecordStatus;

/** Factory for JPA Specifications on RecordEntity. */
public final class RecordSpecifications {

  private RecordSpecifications() {}

  public static Specification<RecordEntity> belongsTo(UUID userId) {
    return (root, cq, cb) -> cb.equal(root.get("userId"), userId);
  }

  public static Specification<RecordEntity> dueDateBetween(LocalDate from, LocalDate to) {
    return (root, cq, cb) -> cb.between(root.get("dueDate"), from, to);
  }

  public static Specification<RecordEntity> monthEquals(YearMonth ym) {
    LocalDate from = ym.atDay(1);
    LocalDate to = ym.atEndOfMonth();
    return dueDateBetween(from, to);
  }

  public static Specification<RecordEntity> hasStatus(RecordStatus status) {
    return (root, cq, cb) -> cb.equal(root.get("status"), status);
  }

  public static Specification<RecordEntity> hasKind(RecordKind kind) {
    return (root, cq, cb) -> cb.equal(root.get("kind"), kind);
  }

  public static Specification<RecordEntity> hasCategory(UUID categoryId) {
    return (root, cq, cb) -> cb.equal(root.get("categoryId"), categoryId);
  }
}
