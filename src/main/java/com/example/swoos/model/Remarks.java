package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@Table(name="remarks")
public class Remarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   // @UserProfile(name = "id")
    private Long remarkID;

   // @UserProfile(name = "Business")
    private String remarks;
    private Boolean isSubmited;
    private boolean deletedFlag;
    @CreationTimestamp
    private Timestamp createAt;
    @UpdateTimestamp
    private Timestamp updatedAt;
}
