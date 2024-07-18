package com.example.swoos.controller;

import com.example.swoos.dto.MergeRequestDTO;
import com.example.swoos.dto.MergedModelDto;
import com.example.swoos.service.MergeService;
import com.example.swoos.model.MergedModel;
import com.example.swoos.dto.PlatformOFSCount;
import com.example.swoos.repository.MergedRepository;
import com.example.swoos.response.PageResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class MergeController {
    @Autowired
    private MergeService mergeService;
    @Autowired
    private MergedRepository mergedRepository;

    @GetMapping("/mergeModel")
    public PageResponse<Object> mergedModel(@RequestParam int pageSize,
                                            @RequestParam int pageNo,
                                            @RequestParam LocalDate fromDate,
                                            @RequestParam(required = false) String field,
                                            @RequestParam(required = false)String searchString,
                                            @RequestParam(required = false) String value ,
                                            @RequestParam boolean greaterThan){
        if(value == null|| value.isEmpty()){
            return mergeService.getMergedModel(pageSize,pageNo,fromDate,field,searchString);
        }else{
            return mergeService.swoosFilter(value,greaterThan,pageNo,pageSize);
        }
    }

    @GetMapping("/historyTrue")
    public ResponseEntity<List<MergedModelDto>> historyTrue(HttpServletResponse response){
        return ResponseEntity.ok(mergeService.readHistoryTrue(response));
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateModel(@RequestBody List<MergeRequestDTO> mergeRequestDTO ){
        return  ResponseEntity.ok(mergeService.update(mergeRequestDTO));
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<MergedModel> getById(@PathVariable Long id) {
        Optional<MergedModel> mergedModel = mergedRepository.findById(id);
        return mergedModel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/platform")
    public ResponseEntity<PlatformOFSCount> getPlatform(){
        return ResponseEntity.ok(mergeService.platformAndValueloss());
    }
}