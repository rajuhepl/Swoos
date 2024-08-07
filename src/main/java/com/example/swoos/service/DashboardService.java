package com.example.swoos.service;

import com.example.swoos.response.SuccessResponse;

import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {
    SuccessResponse<Object> getDashboardCalculation(String platform, String channel, long productId, LocalDate from,
                                                    LocalDate to);

    SuccessResponse<Object> getProductList(String platform, String channel, LocalDate fromDate, LocalDate toDate,String search);

    Map<String, Long> getPlatformSukCount();


}
