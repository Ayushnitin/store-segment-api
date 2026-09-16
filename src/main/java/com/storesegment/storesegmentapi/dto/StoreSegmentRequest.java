package com.storesegment.storesegmentapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StoreSegmentRequest {

    @JsonProperty("store_segment_id")
    private Long storeSegmentId;

    private String title;

    @JsonProperty("store_segment_type")
    private String storeSegmentType;

    private List<Long> stores;

    @JsonProperty("sql_query")
    private String sqlQuery;

    private String status;

    public Long getStoreSegmentId() {
        return storeSegmentId;
    }

    public void setStoreSegmentId(Long storeSegmentId) {
        this.storeSegmentId = storeSegmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStoreSegmentType() {
        return storeSegmentType;
    }

    public void setStoreSegmentType(String storeSegmentType) {
        this.storeSegmentType = storeSegmentType;
    }

    public List<Long> getStores() {
        return stores;
    }

    public void setStores(List<Long> stores) {
        this.stores = stores;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}