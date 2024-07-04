package com.example.swoos.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardCalcDto {
    private String swoosContribution;
    private String valueLoss;
    private String swoosLoss;
    private Map<String,Long> reasonLevelCount;
    private Map<String,Double> locationLevelCount;
}
