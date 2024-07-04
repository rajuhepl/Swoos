package com.example.swoos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name ="merged_model")
public class MergedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mergedId;
    private String Platform;
    private String Asin;
    private String Pname;
    private String InternalDivision;
    private String brand;
    private String Category;
    private String SubCategory;
    private String Ahmedabad;
    private String Bangalore;
    private String Chennai;
    private String Delhi;
    private String Hyderabad;
    private String Indore;
    private String Calcutta;
    private String Mumbai;
    private String Nagpur;
    private String Patna;
    private String Pune;
    private String AhmedabadPercentage="-";
    private String BangalorePercentage="-";
    private String ChennaiPercentage="-";
    private String DelhiPercentage="-";
    private String HyderabadPercentage="-";
    private String IndorePercentage="-";
    private String CalcuttaPercentage="-";
    private String MumbaiPercentage="-";
    private String NagpurPercentage="-";
    private String PatnaPercentage="-";
    private String PunePercentage="-";
    private String NA="-";
    private String Revenue;
    private String monthlySales;
    private String daySales;
    @Digits(integer = 10,fraction = 2)
    private BigDecimal SwoosPercentage;
    private String ValueLoss;
    private String SWOOSContribution;
    @Digits(integer = 10,fraction = 2)
    private BigDecimal totalValueLoss;
    private String other;
//    @OneToOne(cascade = CascadeType.ALL)
//    private DropDownModel dropDownModel;
    //@OneToOne(cascade = CascadeType.ALL)
    private String remarks;
    //@OneToOne(cascade = CascadeType.ALL)
    private String reason;
    private LocalDateTime day;
    private String lastDayReason;
    private String city;
    private Boolean isSubmited;
    private boolean deletedFlag;
    @CreationTimestamp
    private Timestamp createAt;
    @UpdateTimestamp
    private Timestamp updatedAt;
    private String location;
    private LocalDateTime date;
//    @Column(name = "history_flag")
    private boolean historyFlag;
}

