package com.finance.api.record.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecordRepository extends JpaRepository<RecordEntity, UUID>,
        JpaSpecificationExecutor<RecordEntity> {

    Optional<RecordEntity> findByIdAndUserId(UUID id, UUID userId);

    long deleteByUserIdAndIdIn(UUID userId, Iterable<UUID> ids);
}
