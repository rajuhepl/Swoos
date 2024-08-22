package com.example.swoos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReasonLevelDto {
    private String name;
    private int count;
    private DashboardCalcDto table;
}
