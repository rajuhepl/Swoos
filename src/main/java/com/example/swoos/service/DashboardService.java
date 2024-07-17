package com.example.swoos.service;

import com.example.swoos.dto.DashboardCalcDto;
import com.example.swoos.dto.ProductDto;
import com.example.swoos.response.SuccessResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardCalcDto getDashboardCalculation(String platform, String channel, long productId, LocalDate from,
                                             LocalDate to);

    List<ProductDto> getProductList(String platform, String channel, LocalDate fromDate, LocalDate toDate);

    Map<String, Long> getPlatformSukCount();


}
