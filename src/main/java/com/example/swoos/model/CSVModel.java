package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
//@Table(name = "ecom_offtake",schema = "swoos",catalog = "citpl_sales")
@Table(name ="ecom_offtake")
public class CSVModel {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
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
//    @Column(name ="contribution")
//    private String Contribution;

}
