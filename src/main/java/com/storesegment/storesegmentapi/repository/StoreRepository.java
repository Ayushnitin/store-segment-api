package com.storesegment.storesegmentapi.repository;

import com.storesegment.storesegmentapi.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByStoreSegmentId(Long storeSegmentId);
}