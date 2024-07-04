package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name ="platform")
public class Platform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "value_loss")
    private double valueLoss;

    @Column(name = "percentage")
    private double percentage;

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

    @Column(name = "out_of_stock_count")
    private int outOfStockCount;
}
