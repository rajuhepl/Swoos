package com.example.swoos.controller;

import com.example.swoos.dto.DashboardCalcDto;
import com.example.swoos.dto.ProductDto;
import com.example.swoos.response.SuccessResponse;
import com.example.swoos.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<DashboardCalcDto> getDashboardCalculation(@RequestParam(required = false) String platform,
                                                                    @RequestParam(required = false) String channel,
                                                                    @RequestParam long productId, @RequestParam(required = false) LocalDate fromDate,
                                                                    @RequestParam(required = false) LocalDate toDate){
        return ResponseEntity.ok(dashboardService.getDashboardCalculation(platform,channel,productId,fromDate,toDate));

    }
    @GetMapping("/productList")
    public ResponseEntity<List<ProductDto>> getProductList(@RequestParam(required = false) String platform,
                                                          @RequestParam(required = false) String channel,
                                                          @RequestParam(required = false) LocalDate fromDate,
                                                          @RequestParam(required = false) LocalDate toDate){
        return ResponseEntity.ok(dashboardService.getProductList(platform,channel,fromDate,toDate));

    }
    @GetMapping("/sukCount")
    public ResponseEntity<Map<String,Long>> getSukCountPlatforms() {
    return ResponseEntity.ok(dashboardService.getPlatformSukCount());
    }

}
