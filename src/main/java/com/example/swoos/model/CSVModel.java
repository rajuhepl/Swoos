package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name ="ecom_offtake")
public class CSVModel {
    @Id
    @Column(name = "id")
    private Long csvId;

    @Column(name = "bu")
    private String Business;

    @Column(name = "platform")
    private String Platform;

    @Column(name = "sku_code")
    private String MainSKUCode;

    @Column(name = "division")
    private String InternalDivision;

    @Column(name = "brand")
    private String Brand;

    @Column(name = "category")
    private String Category;

    @Column(name = "sub_category")
    private String SubCategory;

    @Column(name = "revenue")
    private String Revenue;

    @Column(name = "triggeredon")
    private LocalDateTime triggeredOn;

}
