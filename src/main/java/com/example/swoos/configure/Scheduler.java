package com.example.swoos.configure;

import com.example.swoos.model.MasterRole;
import com.example.swoos.repository.MasterRoleRepository;
import com.example.swoos.repository.UserRepository;
import com.example.swoos.service.serviceimpl.MergeServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class Scheduler {
    @Autowired
    private MergeServiceImpl mergeExcelAndCSVService;
    @Scheduled(cron = "00 30 10 * * *",zone = "Asia/Kolkata")
//    @PostConstruct
    public void dataLoad() {
        mergeExcelAndCSVService.readDataFromFile();
    }

    @Autowired
    private MasterRoleRepository masterRoleRepository;
    @Autowired
    private UserRepository userRepository;

}
