package com.example.swoos.repository;

import com.example.swoos.dto.DataListProjection;
import com.example.swoos.model.ExcelModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExcelRepository extends JpaRepository<ExcelModel,Long> {
  /*  @Query("SELECT e.city as city , e.Location as location FROM ExcelModel e")
    List<DataListProjection> getAllDataList();
*/
    @Query(value = "SELECT * FROM data WHERE DATE(triggeredon) = CURDATE()", nativeQuery = true)
    List<ExcelModel> findByTriggeredOnToday();
    @Query(value = "SELECT e.city as city , e.Location as location FROM data e WHERE DATE(triggeredon) = CURDATE()", nativeQuery = true)
    List<DataListProjection> findByTriggeredOnTodayProjection();
}
