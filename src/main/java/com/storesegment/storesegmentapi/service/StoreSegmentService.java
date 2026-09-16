package com.storesegment.storesegmentapi.service;

import com.storesegment.storesegmentapi.dto.StoreSegmentRequest;
import com.storesegment.storesegmentapi.entity.Store;
import com.storesegment.storesegmentapi.entity.StoreSegment;
import com.storesegment.storesegmentapi.repository.StoreRepository;
import com.storesegment.storesegmentapi.repository.StoreSegmentRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreSegmentService {

    private final StoreSegmentRepository storeSegmentRepository;
    private final StoreRepository storeRepository;

    public StoreSegmentService(
            StoreSegmentRepository storeSegmentRepository,
            StoreRepository storeRepository) {

        this.storeSegmentRepository = storeSegmentRepository;
        this.storeRepository = storeRepository;
    }

    // =========================================================
    // 1. CREATE STORE SEGMENT
    // =========================================================

    @Transactional
    public StoreSegment create(StoreSegmentRequest request) {

        validateRequest(request);

        StoreSegment segment = new StoreSegment();

        segment.setTitle(request.getTitle().trim());
        segment.setStoreSegmentType(
                request.getStoreSegmentType().toUpperCase()
        );
        segment.setStatus(
                request.getStatus().toUpperCase()
        );

        // AUTOMATED segment uses SQL query
        if ("AUTOMATED".equalsIgnoreCase(
                request.getStoreSegmentType())) {

            segment.setSqlQuery(request.getSqlQuery());

        } else {

            // MANUAL segment does not need SQL
            segment.setSqlQuery(null);
        }

        StoreSegment savedSegment =
                storeSegmentRepository.save(segment);

        // MANUAL segment -> assign selected stores
        if ("MANUAL".equalsIgnoreCase(
                request.getStoreSegmentType())) {

            assignStores(
                    savedSegment,
                    request.getStores()
            );
        }

        return savedSegment;
    }


    // =========================================================
    // 2. UPDATE STORE SEGMENT
    // =========================================================

    @Transactional
    public StoreSegment update(StoreSegmentRequest request) {

        if (request.getStoreSegmentId() == null) {

            throw new IllegalArgumentException(
                    "store_segment_id is required"
            );
        }

        validateRequest(request);

        StoreSegment segment =
                storeSegmentRepository
                        .findById(request.getStoreSegmentId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Store segment not found"
                                )
                        );

        /*
         * Remove old store assignments.
         *
         * Required when:
         *
         * MANUAL -> MANUAL
         * MANUAL -> AUTOMATED
         */
        clearExistingStoreAssignments(
                segment.getId()
        );

        segment.setTitle(
                request.getTitle().trim()
        );

        segment.setStoreSegmentType(
                request.getStoreSegmentType().toUpperCase()
        );

        segment.setStatus(
                request.getStatus().toUpperCase()
        );

        // AUTOMATED
        if ("AUTOMATED".equalsIgnoreCase(
                request.getStoreSegmentType())) {

            segment.setSqlQuery(
                    request.getSqlQuery()
            );

        } else {

            // MANUAL
            segment.setSqlQuery(null);
        }

        StoreSegment updatedSegment =
                storeSegmentRepository.save(segment);

        // Assign new stores if MANUAL
        if ("MANUAL".equalsIgnoreCase(
                request.getStoreSegmentType())) {

            assignStores(
                    updatedSegment,
                    request.getStores()
            );
        }

        return updatedSegment;
    }


    // =========================================================
    // 3. DUPLICATE STORE SEGMENT
    // =========================================================

    @Transactional
    public StoreSegment duplicate(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "store_segment_group_id is required"
            );
        }

        StoreSegment original =
                storeSegmentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Store segment not found"
                                )
                        );

        StoreSegment duplicate =
                new StoreSegment();

        duplicate.setTitle(
                original.getTitle() + " Copy"
        );

        duplicate.setStoreSegmentType(
                original.getStoreSegmentType()
        );

        duplicate.setSqlQuery(
                original.getSqlQuery()
        );

        duplicate.setStatus(
                original.getStatus()
        );

        /*
         * NOTE:
         *
         * With only the two assumed tables:
         *
         * store
         * store_segments
         *
         * a store has one store_segment_id.
         *
         * Therefore a MANUAL segment's stores cannot safely
         * be copied to another segment without moving them
         * away from the original segment.
         *
         * The segment definition itself is duplicated.
         */

        return storeSegmentRepository.save(
                duplicate
        );
    }


    // =========================================================
    // 4. GET STORE SEGMENT DETAILS
    // =========================================================

    public StoreSegment getDetails(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "store_segment_group_id is required"
            );
        }

        return storeSegmentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Store segment not found"
                        )
                );
    }


    // =========================================================
    // 5. GET STORE IDS FOR DETAILS API
    // =========================================================

    public List<Long> getStoreIds(Long segmentId) {

        if (segmentId == null) {

            throw new IllegalArgumentException(
                    "store_segment_group_id is required"
            );
        }

        return storeRepository
                .findByStoreSegmentId(segmentId)
                .stream()
                .map(Store::getId)
                .toList();
    }


    // =========================================================
    // 6. LIST STORE SEGMENTS
    // Search + Status + Pagination
    // =========================================================

    public Page<StoreSegment> list(
            String search,
            String status,
            int page,
            int limit) {

        // PAGE VALIDATION
        if (page < 1) {

            throw new IllegalArgumentException(
                    "page must be greater than 0"
            );
        }

        // LIMIT VALIDATION
        if (limit < 1) {

            throw new IllegalArgumentException(
                    "limit must be greater than 0"
            );
        }

        /*
         * Start with:
         *
         * WHERE 1 = 1
         */

        Specification<StoreSegment> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();


        // =====================================================
        // SEARCH BY TITLE
        // =====================================================

        if (search != null &&
                !search.isBlank()) {

            String searchValue =
                    "%" +
                            search
                                    .trim()
                                    .toLowerCase()
                            + "%";

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(
                                                    root.get("title")
                                            ),
                                            searchValue
                                    )
                    );
        }


        // =====================================================
        // FILTER BY STATUS
        // =====================================================

        if (status != null &&
                !status.isBlank()) {

            if (!status.equalsIgnoreCase("ACTIVE")
                    &&
                    !status.equalsIgnoreCase("INACTIVE")) {

                throw new IllegalArgumentException(
                        "status must be ACTIVE or INACTIVE"
                );
            }

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.equal(
                                            criteriaBuilder.upper(
                                                    root.get("status")
                                            ),
                                            status.toUpperCase()
                                    )
                    );
        }


        // API uses page numbers starting from 1.
        // Spring Data starts from 0.

        Pageable pageable =
                PageRequest.of(
                        page - 1,
                        limit
                );


        return storeSegmentRepository
                .findAll(
                        specification,
                        pageable
                );
    }


    // =========================================================
    // 7. VALIDATE CREATE / UPDATE REQUEST
    // =========================================================

    private void validateRequest(
            StoreSegmentRequest request) {

        // REQUEST BODY
        if (request == null) {

            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }


        // =====================================================
        // TITLE
        // =====================================================

        if (request.getTitle() == null
                ||
                request.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "title is required"
            );
        }


        // =====================================================
        // STORE SEGMENT TYPE
        // =====================================================

        if (request.getStoreSegmentType() == null
                ||
                request.getStoreSegmentType().isBlank()) {

            throw new IllegalArgumentException(
                    "store_segment_type is required"
            );
        }


        if (!request.getStoreSegmentType()
                .equalsIgnoreCase("MANUAL")
                &&
                !request.getStoreSegmentType()
                        .equalsIgnoreCase("AUTOMATED")) {

            throw new IllegalArgumentException(
                    "store_segment_type must be MANUAL or AUTOMATED"
            );
        }


        // =====================================================
        // STATUS
        // =====================================================

        if (request.getStatus() == null
                ||
                request.getStatus().isBlank()) {

            throw new IllegalArgumentException(
                    "status is required"
            );
        }


        if (!request.getStatus()
                .equalsIgnoreCase("ACTIVE")
                &&
                !request.getStatus()
                        .equalsIgnoreCase("INACTIVE")) {

            throw new IllegalArgumentException(
                    "status must be ACTIVE or INACTIVE"
            );
        }


        // =====================================================
        // MANUAL SEGMENT
        // =====================================================

        if ("MANUAL".equalsIgnoreCase(
                request.getStoreSegmentType())) {

            if (request.getStores() == null
                    ||
                    request.getStores().isEmpty()) {

                throw new IllegalArgumentException(
                        "stores are required for MANUAL store segment"
                );
            }
        }


        // =====================================================
        // AUTOMATED SEGMENT
        // =====================================================

        if ("AUTOMATED".equalsIgnoreCase(
                request.getStoreSegmentType())) {

            if (request.getSqlQuery() == null
                    ||
                    request.getSqlQuery().isBlank()) {

                throw new IllegalArgumentException(
                        "sql_query is required for AUTOMATED store segment"
                );
            }
        }
    }


    // =========================================================
    // 8. ASSIGN STORES TO MANUAL SEGMENT
    // =========================================================

    private void assignStores(
            StoreSegment segment,
            List<Long> storeIds) {

        if (storeIds == null ||
                storeIds.isEmpty()) {

            return;
        }


        List<Store> stores =
                storeRepository.findAllById(
                        storeIds
                );


        /*
         * Example:
         *
         * Requested:
         * [1,2,3,4]
         *
         * Database found:
         * [1,2,3]
         *
         * Then one store ID does not exist.
         */

        if (stores.size() != storeIds.size()) {

            throw new IllegalArgumentException(
                    "One or more store IDs are invalid"
            );
        }


        for (Store store : stores) {

            store.setStoreSegment(
                    segment
            );
        }


        storeRepository.saveAll(
                stores
        );
    }


    // =========================================================
    // 9. CLEAR PREVIOUS STORE ASSIGNMENTS
    // =========================================================

    private void clearExistingStoreAssignments(
            Long segmentId) {

        List<Store> existingStores =
                storeRepository
                        .findByStoreSegmentId(
                                segmentId
                        );


        if (existingStores.isEmpty()) {
            return;
        }


        for (Store store : existingStores) {

            store.setStoreSegment(
                    null
            );
        }


        storeRepository.saveAll(
                existingStores
        );
    }
}