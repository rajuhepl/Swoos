package com.example.swoos.service;

import com.example.swoos.model.PlatformAndValueloss;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.Map;

public interface MergeExcelAndCSVService {
    PageResponse<Object> getMergedModel(int pageSize, int pageNo, LocalDate fromDate, String field,
                                        String searchTerm);

    SuccessResponse<Object> readHistoryTrue(HttpServletResponse response);

    Map<String, Map<String, String>> locations(long id);

    PlatformAndValueloss platformAndValueloss();

    SuccessResponse<Object> MergedModel();
}
