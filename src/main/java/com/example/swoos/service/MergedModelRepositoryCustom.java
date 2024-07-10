package com.example.swoos.service;

import com.example.swoos.model.MergedModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
public interface MergedModelRepositoryCustom {
    Page<MergedModel> findAllOrderByValueLossDescPageable(
            String field,
            String searchTerm,
            Pageable pageable);
}
