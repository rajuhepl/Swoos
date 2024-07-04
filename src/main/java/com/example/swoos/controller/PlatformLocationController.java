//package com.example.swoos.Controller;
//
//import com.example.swoos.Request.ValueLossRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api")
//public class PlatformLocationController {
//
//    @Autowired
//    private PlatformLocationService platformLocationService;
//
//    @PostMapping("/calculateValueLoss")
//    public void calculateValueLoss(@RequestBody ValueLossRequest valueLossRequest) {
//        platformLocationService.calculateAndSaveValueLoss(
//                valueLossRequest.getDistinctPlatforms(),
//                valueLossRequest.getDistinctLocations(),
//                valueLossRequest.getMergedList(),
//                valueLossRequest.getOutOfStockCounts(),
//                valueLossRequest.getPlatformAndValueLoss(),
//                valueLossRequest.getNational()
//        );
//    }
//}
//
