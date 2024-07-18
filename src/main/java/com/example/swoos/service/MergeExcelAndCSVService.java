package com.example.swoos.service;

import com.example.swoos.model.MergedModel;
import com.example.swoos.model.PlatformAndValueloss;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MergeExcelAndCSVService {
    PageResponse<Object> getMergedModel(int pageSize, int pageNo, LocalDate fromDate, String field,
                                        String searchTerm);

    SuccessResponse<Object> readHistoryTrue(HttpServletResponse response);

    Map<String, Map<String, String>> locations(long id);

    PlatformAndValueloss platformAndValueloss();


    PageResponse<Object> swoosFilter(String value, boolean greaterThan,int pageNo,int pageSize);
}
