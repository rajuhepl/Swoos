package com.example.swoos.service;

import com.example.swoos.dto.MergeRequestDTO;
import com.example.swoos.dto.MergedModelDto;
import com.example.swoos.dto.PlatformOFSCount;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MergeService {
    PageResponse<Object> getMergedModel(int pageSize, int pageNo, LocalDate fromDate, String field,
                                        String searchTerm);

    List<MergedModelDto> readHistoryTrue(HttpServletResponse response);

    Map<String, Map<String, String>> locations(long id);

    PlatformOFSCount platformAndValueloss();

    SuccessResponse<Object> MergedModel();

    PageResponse<Object> swoosFilter(String value, boolean greaterThan,int pageNo,int pageSize);

    String update(List<MergeRequestDTO> mergeRequestDTO);
}
