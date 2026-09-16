package com.storesegment.storesegmentapi.controller;

import com.storesegment.storesegmentapi.dto.StoreSegmentRequest;
import com.storesegment.storesegmentapi.entity.StoreSegment;
import com.storesegment.storesegmentapi.service.StoreSegmentService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StoreSegmentController {

    private final StoreSegmentService storeSegmentService;

    public StoreSegmentController(StoreSegmentService storeSegmentService) {
        this.storeSegmentService = storeSegmentService;
    }

    // 1. LIST
    @GetMapping("/store-segment/list")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Page<StoreSegment> result =
                storeSegmentService.list(search, status, page, limit);

        // Convert entities to contract response format
        List<Map<String, Object>> segments =
                result.getContent()
                        .stream()
                        .map(segment -> {

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put(
                                    "store_segment_group_id",
                                    segment.getId()
                            );

                            item.put(
                                    "store_segment_group_name",
                                    segment.getTitle()
                            );

                            item.put(
                                    "type",
                                    segment.getStoreSegmentType()
                            );

                            item.put(
                                    "status",
                                    segment.getStatus()
                            );

                            return item;
                        })
                        .toList();

        Map<String, Object> pagination =
                new LinkedHashMap<>();

        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put(
                "total_records",
                result.getTotalElements()
        );
        pagination.put(
                "total_page",
                result.getTotalPages()
        );

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "store_segment_group",
                segments
        );

        data.put(
                "pagination",
                pagination
        );

        return ResponseEntity.ok(
                response(
                        "/api/store-segment/list",
                        data
                )
        );
    }

    // 2. CREATE
    @PostMapping("/store-segment/create")
    public ResponseEntity<?> create(
            @RequestBody StoreSegmentRequest request) {

        StoreSegment created = storeSegmentService.create(request);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("store_segment_group_id", created.getId());
        data.put(
                "message",
                "Store segment group created successfully"
        );

        return ResponseEntity.ok(
                response("/api/store-segment/create", data)
        );
    }

    // 3. UPDATE
    @PostMapping("/store-segment/update")
    public ResponseEntity<?> update(
            @RequestBody StoreSegmentRequest request) {

        StoreSegment updated = storeSegmentService.update(request);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("store_segment_group_id", updated.getId());
        data.put(
                "message",
                "Store segment group updated successfully"
        );

        return ResponseEntity.ok(
                response("/api/store-segment/update", data)
        );
    }

    // 4. DUPLICATE
    @PostMapping("/store-segment/duplicate")
    public ResponseEntity<?> duplicate(
            @RequestBody Map<String, Long> request) {

        Long id = request.get("store_segment_group_id");

        if (id == null) {
            throw new RuntimeException(
                    "store_segment_group_id is required"
            );
        }

        StoreSegment duplicated =
                storeSegmentService.duplicate(id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(
                "store_segment_group_id",
                duplicated.getId()
        );
        data.put(
                "message",
                "Store segment group duplicated successfully"
        );

        return ResponseEntity.ok(
                response("/api/store-segment/duplicate", data)
        );
    }

    // 5. DETAILS
    @GetMapping("/store-segment-group/details/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {

        StoreSegment segment =
                storeSegmentService.getDetails(id);

        Map<String, Object> data = new LinkedHashMap<>();

        data.put(
                "store_segment_group_id",
                segment.getId()
        );

        data.put(
                "title",
                segment.getTitle()
        );

        data.put(
                "store_segment_group_type",
                segment.getStoreSegmentType()
        );

        // MANUAL SEGMENT
        if ("MANUAL".equalsIgnoreCase(
                segment.getStoreSegmentType())) {

            data.put(
                    "stores",
                    storeSegmentService.getStoreIds(segment.getId())
            );

            data.put("sql_query", null);
        }

        // AUTOMATED SEGMENT
        else {

            data.put("stores", null);

            data.put(
                    "sql_query",
                    segment.getSqlQuery()
            );
        }

        data.put(
                "status",
                segment.getStatus()
        );

        return ResponseEntity.ok(
                response(
                        "/api/store-segment-group/details/" + id,
                        data
                )
        );
    }

    // COMMON RESPONSE FORMAT
    private Map<String, Object> response(
            String endpoint,
            Object data) {

        Map<String, Object> meta = new LinkedHashMap<>();

        meta.put(
                "request_id",
                "req_" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );
        meta.put("timestamp", Instant.now().toString());
        meta.put("version", "v1");
        meta.put("end_point", endpoint);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("meta", meta);
        response.put("data", data);

        return response;
    }
}