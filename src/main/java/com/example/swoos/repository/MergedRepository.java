package com.example.swoos.repository;

import com.example.swoos.dto.MergedModelProjection;
import com.example.swoos.model.MergedModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
//@Repository
public interface MergedRepository extends JpaRepository<MergedModel,Long> {

//    @Query("SELECT m FROM MergedModel m ORDER BY CAST(m.ValueLoss AS double) DESC where history=false")
//    List<MergedModel> findAllOrderByValueLossDesc();

    @Query("SELECT m FROM MergedModel m WHERE m.historyFlag = false ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> findAllOrderByValueLossDesc();
    @Query("SELECT m FROM MergedModel m WHERE m.createAt BETWEEN :fromDate AND :toDate AND m.historyFlag = false ORDER BY CAST(m.ValueLoss AS double) DESC")
    Page<MergedModel> findAllOrderByValueLossDescPageable(@Param("fromDate")Timestamp fromDate,@Param("toDate")Timestamp toDate,Pageable pageable);

    @Query("SELECT m FROM MergedModel m WHERE m.historyFlag = true AND (m.reason = 'Discontinued' OR m.reason = 'Location not align')")
    List<MergedModel> findAllDiscontinuedOrLocationNotAlign();

    @Query("SELECT m FROM MergedModel m WHERE  m.historyFlag = true ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> getAllHistoryTrue();
    @Query("SELECT m FROM MergedModel m WHERE m.updatedAt BETWEEN :fromDate AND :toDate AND m.historyFlag = true ORDER BY CAST(m.ValueLoss AS double) DESC")
    List<MergedModel> getAllHistoryTrue(@Param("fromDate")Timestamp fromDate,@Param("toDate")Timestamp toDate);
    //    @Query(value = "SELECT location, SUM(loss) FROM SalesLossData GROUP BY location", nativeQuery = true)
//    List<Object[]> findLocationWiseSalesLoss();
@Query(value = "SELECT location, SUM(value_loss) FROM merged_model GROUP BY location", nativeQuery = true)
List<Object[]> findLocationWiseSalesLoss();


//    @Query(value = "SELECT SWOOSContribution,totalValueLoss,ValueLoss,Revenue FROM merged_model WHERE platform IN (:platforms)", nativeQuery = true)
//    List<MergedModel> findAllByPlatformsNative(@Param("platforms") List<String> platforms);
//    @Query(value = "SELECT SWOOSContribution,totalValueLoss,ValueLoss,Revenue FROM merged_model WHERE platform = :platform", nativeQuery = true)
//    List<MergedModel> findAllByPlatform(@Param("platform") String platform);

    // If Date is not present
    @Query("SELECT m.SWOOSContribution as SWOOSContribution," +
            "  m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason," +
            " m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad " +
            "  FROM MergedModel m WHERE m.Platform IN :platforms")
    List<MergedModelProjection> findAllByPlatformsNative(@Param("platforms") List<String> platforms);
    @Query("SELECT m.Pname,m.mergedId FROM MergedModel m WHERE m.Platform IN :platforms")
    List<Object[]> findAllByPlatformsNativeProducts(@Param("platforms") List<String> platforms);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution," +
            " m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason," +
            " m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad" +
            " FROM MergedModel m WHERE m.Platform = :platform")
    List<MergedModelProjection> findAllByPlatform(@Param("platform") String platform);

    @Query("SELECT m.Pname,m.mergedId FROM MergedModel m WHERE m.Platform = :platform")
    List<Object[]> findAllByPlatformProduct(@Param("platform") String platform);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution," +
            "  m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason," +
            "  m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad" +
            "  FROM MergedModel m WHERE m.mergedId = :productId")
    List<MergedModelProjection> findByIdProduct(long productId);
    @Query("SELECT m.SWOOSContribution as SWOOSContribution, m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad" +
            " FROM MergedModel m")
    List<MergedModelProjection> getAll();
    @Query("SELECT m.Pname,m.mergedId FROM MergedModel m")
    List<Object[]> getAllProducts();
    @Query("SELECT m.Platform, m.mergedId FROM MergedModel m")
    List<Object[]> getAllMergedModel();
//If Date is Present
    @Query("SELECT  m.SWOOSContribution as SWOOSContribution, m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad FROM MergedModel m WHERE m.Platform IN :platforms AND m.createAt BETWEEN :fromDate AND :toDate")
    List<MergedModelProjection> findAllByPlatformsNativeDate(@Param("platforms") List<String> platforms, @Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT  m.SWOOSContribution as SWOOSContribution, m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad FROM MergedModel m WHERE m.Platform = :platform AND m.createAt BETWEEN :fromDate AND :toDate")
    List<MergedModelProjection> findAllByPlatformDate(@Param("platform") String platform,@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT  m.SWOOSContribution as SWOOSContribution, m.totalValueLoss as totalValueLoss, m.ValueLoss as valueLoss, m.Revenue as revenue, m.reason as reason, m.Pune as pune, m.other as other, m.Patna as patna, m.Mumbai as mumbai, m.Indore as indore, m.Hyderabad as hyderabad, m.Delhi as delhi, m.Chennai as chennai, m.Calcutta as calcutta, m.Bangalore as bangalore, m.Ahmedabad as ahmedabad FROM MergedModel m WHERE m.createAt BETWEEN :fromDate AND :toDate")
    List<MergedModelProjection> getAllPlat(@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.Pname,m.mergedId  FROM MergedModel m WHERE m.Platform = :platform AND m.createAt BETWEEN :fromDate AND :toDate")
    List<Object[]> findAllByPlatformDateProducts(@Param("platform") String platform,@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.Pname,m.mergedId FROM MergedModel m WHERE m.createAt BETWEEN :fromDate AND :toDate")
    List<Object[]> getAllPlatformProductsDate(@Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
    @Query("SELECT m.Pname,m.mergedId FROM MergedModel m WHERE m.Platform IN :platforms AND m.createAt BETWEEN :fromDate AND :toDate")
    List<Object[]> findAllByPlatformsNativeDateProduct(@Param("platforms") List<String> platforms, @Param("fromDate") Timestamp fromDate, @Param("toDate")Timestamp toDate);
}
