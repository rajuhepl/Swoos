package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.sql.Timestamp;
@Getter
@Setter
//@Document(collection = "user")
@Entity
@Table(name="user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "og_password")
    private String ogPassword;

    @Column(name = "dob")
    private String dob;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "application_role_id")
    private MasterRole applicationRole;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "created_by")
    private int createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "updated_by")
    private int updatedBy;

}

