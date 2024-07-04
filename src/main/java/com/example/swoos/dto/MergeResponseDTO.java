package com.example.swoos.dto;

import lombok.Data;

@Data
public class MergeResponseDTO {
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
    private double SwoosPercentage;
    private String ValueLoss;
    private String SWOOSContribution;
    private double totalValueLoss;
    private String other ="1";
    private String reason;
    private String remarks;
    private String lastDayReason;
}
