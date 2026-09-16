package com.storesegment.storesegmentapi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "store_segments")
public class StoreSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "store_segment_type", nullable = false)
    private String storeSegmentType;

    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(nullable = false)
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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