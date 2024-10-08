package com.example.swoos.configure;

import com.example.swoos.service.serviceimpl.MergeExcelAndCSVServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class Scheduler {
    @Autowired
    private MergeExcelAndCSVServiceImpl mergeExcelAndCSVService;
    @Scheduled(cron = "00 30 10 * * *",zone = "Asia/Kolkata")
//    @PostConstruct
    public void dataLoad() {
        mergeExcelAndCSVService.readDataFromFile();
    }
}
