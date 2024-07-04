package com.example.swoos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name ="reason")
public class Reason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "row_id")
    private int rowId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "is_submitted")
    private Boolean isSubmited;

    @Column(name = "deleted_flag")
    private boolean deletedFlag;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public Reason() {}

    public Reason(int rowId, String reason) {
        this.rowId = rowId;
        this.reason = reason;
        this.createdDate = LocalDateTime.now();
    }



}


