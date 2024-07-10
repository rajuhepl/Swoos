package com.example.swoos.controller;

import com.example.swoos.dto.MergeRequestDTO;
import com.example.swoos.service.MergeExcelAndCSVService;
import com.example.swoos.service.MergeFileService;
import com.example.swoos.service.SalesLossService;
import com.example.swoos.model.MergedModel;
import com.example.swoos.model.PlatformAndValueloss;
import com.example.swoos.repository.MergedRepository;
import com.example.swoos.response.PageResponse;
import com.example.swoos.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
//@RequestMapping("/api")
public class MergeController {
    @Autowired
    MergeExcelAndCSVService mergeExcelAndCSVService;
    @Autowired
    MergeFileService mergeFileService;
    @Autowired
    MergedRepository mergedRepository;

    @Autowired
    private SalesLossService salesLossService;

    @GetMapping("/mergeModel")
    public PageResponse<Object> mergedModel(@RequestParam int pageSize,
                                            @RequestParam int pageNo,
                                            @RequestParam LocalDate fromDate,
                                            @RequestParam(required = false) String field,
                                            @RequestParam(required = false)String searchString,
                                            @RequestParam(required = false) String value ,
                                            @RequestParam boolean greaterThan){
        if(value == null|| value.isEmpty()){
            return mergeExcelAndCSVService.getMergedModel(pageSize,pageNo,fromDate,field,searchString);
        }else{
            return mergeExcelAndCSVService.swoosFilter(value,greaterThan,pageNo,pageSize);
        }
    }

    @GetMapping("/historyTrue")
    public SuccessResponse<Object> historyTrue(HttpServletResponse response){
        return mergeExcelAndCSVService.readHistoryTrue(response);
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("csvFile") MultipartFile csvFile,
                                              @RequestParam("excelFile") MultipartFile excelFile,HttpServletResponse response) {
        return mergeFileService.readCSVData(csvFile,excelFile,response);
    }
    @PostMapping("/update")
    public String updateModel(@RequestBody List<MergeRequestDTO> mergeRequestDTO ){
        return  mergeFileService.update(mergeRequestDTO );
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<MergedModel> getById(@PathVariable Long id) {
        Optional<MergedModel> mergedModel = mergedRepository.findById(id);
        return mergedModel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/location")
    public Map<String,Map<String,String>> getLocations(@RequestParam long id){
        return mergeExcelAndCSVService.locations(id);
    }

    @GetMapping("/platform")
    public PlatformAndValueloss getPlatform(){
        return mergeExcelAndCSVService.platformAndValueloss();
    }

/*    @GetMapping("/swoosfilter")
    public PageResponse<Object> getSwoosFilter(@RequestParam String value ,
                                               @RequestParam boolean greaterThan,
                                               @RequestParam int pageNo,
                                               @RequestParam int pageSize){
        return mergeExcelAndCSVService.swoosFilter(value,greaterThan,pageNo,pageSize);
    }*/
}
