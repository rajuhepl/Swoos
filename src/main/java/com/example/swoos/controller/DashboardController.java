package com.example.swoos.controller;
import com.example.swoos.dto.LocationLevelDTO;
import com.example.swoos.dto.ReasonLevelDto;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@RestController
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;
    @GetMapping("/dashboard")
    public SuccessResponse<Object> getDashboardCalculation(@RequestParam(required = false) String platform,
                                                           @RequestParam(required = false) String channel,
                                                           @RequestParam(required = false) String productId,
                                                           @RequestParam(required = false) LocalDate fromDate,
                                                           @RequestParam(required = false) LocalDate toDate){
        return dashboardService.getDashboardCalculation(platform,channel,productId,fromDate,toDate);
    }
    @GetMapping("/reasonlevel")
    public List<ReasonLevelDto> getReasonLevel(@RequestParam(required = false) String platform,
                                               @RequestParam(required = false) String channel,
                                               @RequestParam(required = false) String productId,
                                               @RequestParam(required = false) LocalDate fromDate,
                                               @RequestParam(required = false) LocalDate toDate){
        return dashboardService.getReasonLevel(platform,channel,productId,fromDate,toDate);
    }
    @GetMapping("/locationlevel")
    public List<LocationLevelDTO> getLocationLevel(@RequestParam(required = false) String platform,
                                                   @RequestParam(required = false) String channel,
                                                   @RequestParam(required = false) String productId,
                                                   @RequestParam(required = false) LocalDate fromDate,
                                                   @RequestParam(required = false) LocalDate toDate){
        return dashboardService.getLocationLevel(platform,channel,productId,fromDate,toDate);
    }
    @GetMapping("/platformlevel")
    public List<ReasonLevelDto> getPlatformLevel(@RequestParam(required = false) String platform,
                                                 @RequestParam(required = false) String channel,
                                                 @RequestParam(required = false) String productId,
                                                 @RequestParam(required = false) LocalDate fromDate,
                                                 @RequestParam(required = false) LocalDate toDate){
        return dashboardService.getPlatformLevel(platform,channel,productId,fromDate,toDate);
    }
    @GetMapping("/productList")
    public SuccessResponse<Object> getProductList(@RequestParam(required = false) String platform,
                                                  @RequestParam(required = false) String channel,
                                                  @RequestParam(required = false) LocalDate fromDate,
                                                  @RequestParam(required = false) LocalDate toDate,
                                                  @RequestParam(required = false) String search){
        return dashboardService.getProductList(platform,channel,fromDate,toDate,search);

    }
    @GetMapping("/sukCount")
    public Map<String,Long> getSukCountPlatforms() {
    return dashboardService.getPlatformSukCount();
    }
}
