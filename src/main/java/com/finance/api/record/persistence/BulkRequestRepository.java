package com.finance.api.record.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkRequestRepository extends JpaRepository<BulkRequestEntity, UUID> {
  Optional<BulkRequestEntity> findByUserIdAndRequestId(UUID userId, String requestId);
}
