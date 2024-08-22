package com.example.swoos.service;

import com.example.swoos.dto.LocationLevelDTO;
import com.example.swoos.dto.ReasonLevelDto;
import com.example.swoos.response.SuccessResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    SuccessResponse<Object> getDashboardCalculation(String platform, String channel, String productId, LocalDate from,
                                                    LocalDate to);

    SuccessResponse<Object> getProductList(String platform, String channel, LocalDate fromDate, LocalDate toDate,String search);

    Map<String, Long> getPlatformSukCount();


    List<ReasonLevelDto> getReasonLevel(String platform, String channel, String productId, LocalDate fromDate, LocalDate toDate);

    List<ReasonLevelDto> getPlatformLevel(String platform, String channel, String productId, LocalDate fromDate, LocalDate toDate);

    List<LocationLevelDTO> getLocationLevel(String platform, String channel, String productId, LocalDate fromDate, LocalDate toDate);
}
