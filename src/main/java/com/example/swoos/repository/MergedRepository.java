package com.example.swoos.repository;

import com.example.swoos.dto.MergedModelProjection;
import com.example.swoos.dto.PlatformCount;
import com.example.swoos.model.MergedModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
public interface MergedRepository extends JpaRepository<MergedModel,Long> {

    @Query(value = "SELECT * FROM merged_model m WHERE CAST(REPLACE(m.swooscontribution, ' %', '') AS DECIMAL(10, 2)) > :value " +
            "AND DATE(m.create_at) = CURDATE() AND m.history_flag = false ", nativeQuery = true)
    Page<MergedModel> findAllBySWOOSContributionGreaterThanAndCreatedAtToday(@Param("value") String value, Pageable pageable);
    @Query(value = "SELECT * FROM merged_model m WHERE CAST(REPLACE(m.swooscontribution, ' %', '') AS DECIMAL(10, 2)) < :value " +
            "AND DATE(m.create_at) = CURDATE() AND m.history_flag = false", nativeQuery = true)
    Page<MergedModel> findAllBySWOOSContributionLesserThanAndCreatedAtToday(@Param("value") String value, Pageable pageable);

    @Query("SELECT m FROM MergedModel m WHERE m.historyFlag = false ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> findAllOrderByValueLossDesc();
    @Query("SELECT m FROM MergedModel m WHERE m.createAt BETWEEN :fromDate AND :toDate AND m.historyFlag = false ORDER BY CAST(m.ValueLoss AS double) DESC")
    Page<MergedModel> findAllOrderByValueLossDescPageable(@Param("fromDate")Timestamp fromDate,@Param("toDate")Timestamp toDate,Pageable pageable);

    @Query("SELECT m FROM MergedModel m WHERE FUNCTION('DATE', m.createAt) = FUNCTION('CURDATE') AND m.historyFlag = false ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> getAllTodayMergedData();
    @Query("SELECT m FROM MergedModel m WHERE m.historyFlag = true AND (m.reason = 'Discontinued' OR m.reason = 'Location not align')")
    List<MergedModel> findAllDiscontinuedOrLocationNotAlign();

    @Query("SELECT m FROM MergedModel m WHERE  m.historyFlag = true ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> getAllHistoryTrue();
    @Query("SELECT m FROM MergedModel m WHERE m.updatedAt BETWEEN :fromDate AND :toDate AND m.historyFlag = true AND m.reason NOT IN ('DisContinued') ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> getAllHistoryTrue(@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);

    @Query(value = "SELECT location, SUM(value_loss) FROM merged_model GROUP BY location", nativeQuery = true)
    List<Object[]> findLocationWiseSalesLoss();


    @Query("SELECT m.SWOOSContribution as SWOOSContribution," +
            "   m.ValueLoss as valueLoss ,m.Pname as platform, m.Revenue as revenue, m.reason as reason,m.daySales as daySales," +
            " m.monthlySales as monthlySales,m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad " +
            "  FROM MergedModel m WHERE m.Platform IN :platforms  AND m.historyFlag = true AND m.reason NOT IN ('DisContinued', 'Dispute', 'Location not align')")
    List<MergedModelProjection> findAllByPlatformsNative(@Param("platforms") List<String> platforms);
    @Query("SELECT m.Pname, m.Asin,m.Platform,m.createAt FROM MergedModel m WHERE m.Platform IN :platforms")
    List<Object[]> findAllByPlatformsNativeProducts(@Param("platforms") List<String> platforms);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution,m.Pname as platform ," +
            "  m.ValueLoss as valueLoss, m.Revenue as revenue,m.daySales as daySales, m.reason as reason," +
            "m.monthlySales as monthlySales, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad" +
            " FROM MergedModel m WHERE m.Platform = :platform AND m.historyFlag = true AND m.reason NOT IN ('DisContinued', 'Dispute', 'Location not align')")
    List<MergedModelProjection> findAllByPlatform(@Param("platform") String platform);

    @Query("SELECT m.Pname, m.Asin,m.Platform,m.createAt FROM MergedModel m WHERE m.Platform = :platform")
    List<Object[]> findAllByPlatformProduct(@Param("platform") String platform);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution,m.Pname as platform," +
            "   m.ValueLoss as valueLoss, m.Revenue as revenue,m.daySales as daySales, m.reason as reason," +
            " m.monthlySales as monthlySales, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad" +
            "  FROM MergedModel m WHERE m.Asin = :productId ")
    List<MergedModelProjection> findByIdProduct(String productId);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution," +
            "   m.ValueLoss as valueLoss, m.Revenue as revenue, m.Pname as platform,m.daySales as daySales, m.reason as reason," +
            "  m.Pune as pune,m.monthlySales as monthlySales, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad" +
            "  FROM MergedModel m WHERE m.Asin = :productId AND m.createAt BETWEEN :fromDate AND :toDate")
    List<MergedModelProjection> findByIdProductAndDate(@Param("productId") String productId, @Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution, " +
            "m.ValueLoss as valueLoss,m.Pname as platform, " +
            "m.Revenue as revenue, " +
            "m.reason as reason, " +
            "m.Pune as pune, " +
            "m.other as other, " +
            "m.Patna as patna, " +
            "m.Mumbai as mumbai, " +
            "m.Indore as indore,m.daySales as daySales, " +
            "m.Hyderabad as hyderabad, " +
            "m.Delhi as delhi, " +
            "m.Chennai as chennai, " +
            "m.Calcutta as calcutta, " +
            "m.Bangalore as bangalore, " +
            "m.monthlySales as monthlySales, "+
            "m.Ahmedabad as ahmedabad " +
            "FROM MergedModel m " +
            "WHERE m.historyFlag = true AND " +
            "m.reason NOT IN ('DisContinued', 'Dispute', 'Location not align')")
    List<MergedModelProjection> getAll();

    @Query("SELECT m.Pname, m.Asin ,m.Platform,m.createAt FROM MergedModel m")
    List<Object[]> getAllProducts();
    @Query("SELECT m.Platform, m.mergedId FROM MergedModel m")
    List<Object[]> getAllMergedModel();
//If Date is Present
@Query("SELECT m.SWOOSContribution as SWOOSContribution,m.Pname as platform, m.monthlySales as monthlySales, m.daySales as daySales, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad " +
        "FROM MergedModel m " +
        "WHERE m.Platform IN :platforms AND m.createAt BETWEEN :fromDate AND :toDate AND m.historyFlag = true AND m.reason NOT IN ('DisContinued', 'Dispute', 'Location not align')")
List<MergedModelProjection> findAllByPlatformsNativeDate(@Param("platforms") List<String> platforms, @Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);
    @Query("SELECT  m.SWOOSContribution as SWOOSContribution,m.Pname as platform,m.monthlySales as monthlySales,  m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason,m.daySales as daySales, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad FROM MergedModel m " +
            "WHERE m.Platform = :platform AND m.createAt BETWEEN :fromDate AND :toDate AND m.historyFlag = true AND m.reason NOT IN ('DisContinued', 'Dispute', 'Location not align')")
    List<MergedModelProjection> findAllByPlatformDate(@Param("platform") String platform,@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.SWOOSContribution AS SWOOSContribution, " +
            "m.ValueLoss AS valueLoss, " +
            "m.Revenue AS revenue, " +
            "m.reason AS reason, " +
            "m.Pune AS pune,m.Pname as platform, " +
            "m.other AS other, " +
            "m.Patna AS patna, " +
            "m.Mumbai AS mumbai,m.daySales as daySales, " +
            "m.Indore AS indore, " +
            "m.Hyderabad AS hyderabad, " +
            "m.Delhi AS delhi, " +
            "m.monthlySales as monthlySales, "+
            "m.Chennai AS chennai, " +
            "m.Calcutta AS calcutta, " +
            "m.Bangalore AS bangalore, " +
            "m.Ahmedabad AS ahmedabad " +
            "FROM MergedModel m " +
            "WHERE m.createAt BETWEEN :fromDate AND :toDate " +
            "AND m.historyFlag = true " +
            "AND m.reason NOT IN ('DisContinued', 'Dispute', 'Location not align')")
    List<MergedModelProjection> getAllPlat(@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate);

    @Query("SELECT m.Pname, m.Asin,m.Platform,m.createAt  FROM MergedModel m WHERE m.Platform = :platform AND m.createAt BETWEEN :fromDate AND :toDate")
    List<Object[]> findAllByPlatformDateProducts(@Param("platform") String platform,@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.Pname, m.Asin, m.Platform,m.createAt FROM MergedModel m WHERE m.createAt BETWEEN :fromDate AND :toDate")
    List<Object[]> getAllPlatformProductsDate(@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.Pname, m.Asin, m.Platform,m.createAt FROM MergedModel m WHERE m.Platform IN :platforms AND m.createAt BETWEEN :fromDate AND :toDate")
    List<Object[]> findAllByPlatformsNativeDateProduct(@Param("platforms") List<String> platforms, @Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.Platform AS platform, " +
            "       SUM(CASE WHEN m.Ahmedabad = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Bangalore = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Chennai = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Delhi = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Hyderabad = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Indore = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Calcutta = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Mumbai = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Nagpur = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Patna = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.other = 'OUT-OF-STOCK' THEN 1 ELSE 0 END + " +
            "           CASE WHEN m.Pune = 'OUT-OF-STOCK' THEN 1 ELSE 0 END) AS outOfStockCount " +
            "FROM MergedModel m " +
            "GROUP BY m.Platform")
    List<PlatformCount> countDataByPlatform();


    //Product List Search
    @Query("SELECT m.Pname, m.Asin, m.Platform, m.createAt FROM MergedModel m WHERE m.Platform = :platform AND m.createAt BETWEEN :fromDate AND :toDate AND m.Pname LIKE %:search%")
    List<Object[]> findAllByPlatformDateProductsSearch(@Param("platform") String platform, @Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate, @Param("search") String search);

    @Query("SELECT m.Pname, m.Asin, m.Platform, m.createAt FROM MergedModel m WHERE m.createAt BETWEEN :fromDate AND :toDate AND m.Pname LIKE %:search%")
    List<Object[]> getAllPlatformProductsDateSearch(@Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate, @Param("search") String search);

    @Query("SELECT m.Pname, m.Asin, m.Platform, m.createAt FROM MergedModel m WHERE m.Platform IN :platforms AND m.createAt BETWEEN :fromDate AND :toDate AND m.Pname LIKE %:search%")
    List<Object[]> findAllByPlatformsNativeDateProductSearch(@Param("platforms") List<String> platforms, @Param("fromDate") Timestamp fromDate, @Param("toDate") Timestamp toDate, @Param("search") String search);

    @Query("SELECT m.Pname, m.Asin, m.Platform, m.createAt FROM MergedModel m WHERE m.Platform = :platform AND m.Pname LIKE %:search%")
    List<Object[]> findAllByPlatformProduct(@Param("platform") String platform, @Param("search") String search);

    @Query("SELECT m.Pname, m.Asin, m.Platform, m.createAt FROM MergedModel m WHERE m.Platform IN :platforms AND m.Pname LIKE %:search%")
    List<Object[]> findAllByPlatformsNativeProducts(@Param("platforms") List<String> platforms, @Param("search") String search);

    @Query("SELECT m.Pname, m.Asin, m.Platform, m.createAt FROM MergedModel m WHERE m.Pname LIKE %:search%")
    List<Object[]> getAllProducts(@Param("search") String search);
}