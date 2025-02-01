package com.example.swoos.model;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
//@Table(name = "data",schema = "swoos",catalog = "citpl_sales")
@Table(name ="data")
public class ExcelModel {
    @Id
   // @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long excelId;

    @Column(name = "platform")
    private String Platform;

    @Column(name = "asin")
    private String Asin	;

    @Column(name = "pname")
    private String Pname;

    @Column(name = "brand")
    private String Brand;

    @Column(name = "location")
    private  String Location;

    @Column(name = "city")
    private String City;

    @Column(name = "status_text")
    private String	statusText;

    @Column(name = "status")
    private String status;

    @Column(name = "triggeredon")
    private LocalDateTime triggeredOn;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "product_url")
    private String productUrl;

    @Column(name = "pin_code")
    private Long pinCode;

    @Column(name = "region")
    private String region;

    @Column(name = "status_num")
    private Long statusNum;

    @Column(name = "sp")
    private Long sp;

    @Column(name = "seller")
    private String seller;

    @Column(name = "mrp")
    private Long mrp;

    @Column(name = "discount")
    private Float discount;

}
