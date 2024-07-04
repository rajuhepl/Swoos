package com.example.swoos.repository;

import com.example.swoos.model.CSVModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CsvRepository extends JpaRepository<CSVModel,Long> {

    @Query(value = "SELECT * FROM ecom_offtake WHERE DATE_FORMAT(triggeredon, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')", nativeQuery = true)
    List<CSVModel> findAllDataInCurrentMonth();

    @Query(value = "SELECT * FROM ecom_offtake WHERE DATE_FORMAT(triggeredon, '%Y-%m') = :month", nativeQuery = true)
    List<CSVModel> findAllDataForMonth(@Param("month") String month);

    @Query(value = "SELECT DATE_FORMAT(MAX(triggeredon), '%Y-%m') FROM ecom_offtake", nativeQuery = true)
    String findLastLoadedMonth();

    @Query(value = "SELECT * FROM ecom_offtake WHERE DATE_FORMAT(triggeredon, '%Y-%m') = DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m')", nativeQuery = true)
    List<CSVModel> findAllDataInPreviousMonth();
}
