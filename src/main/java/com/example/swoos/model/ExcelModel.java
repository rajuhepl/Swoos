package com.example.swoos.model;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name ="data")
public class ExcelModel {
    @Id
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

}
