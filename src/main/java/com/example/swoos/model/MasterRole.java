package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "master_role")
public class MasterRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "role_name")
    private String roleName;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "deleted_flag")
    private Boolean deletedFlag;
    @Column(name = "created_by")
    private int createdBy;
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Column(name = "updated_by")
    private int updatedBy;
    @Column(name = "updated_at")
    private Timestamp updatedAt;

}

