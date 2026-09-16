package com.storesegment.storesegmentapi.repository;

import com.storesegment.storesegmentapi.entity.StoreSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoreSegmentRepository
        extends JpaRepository<StoreSegment, Long>,
        JpaSpecificationExecutor<StoreSegment> {
}