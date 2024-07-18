package com.example.swoos.repository;

import com.example.swoos.model.DataTable;
import com.example.swoos.projection.DataListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataTableRepository extends JpaRepository<DataTable,Long> {
  /*  @Query("SELECT e.city as city , e.Location as location FROM DataTable e")
    List<DataListProjection> getAllDataList();
*/
    @Query(value = "SELECT * FROM data WHERE DATE(triggeredon) = CURDATE()", nativeQuery = true)
    List<DataTable> findByTriggeredOnToday();
    @Query(value = "SELECT e.city as city , e.Location as location FROM data e WHERE DATE(triggeredon) = CURDATE()", nativeQuery = true)
    List<DataListProjection> findByTriggeredOnTodayProjection();
}
