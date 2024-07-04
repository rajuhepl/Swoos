package com.example.swoos.repository;

import com.example.swoos.model.DropDownModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DropDownRepository extends JpaRepository<DropDownModel, Long> {


    DropDownModel findByDescription(String description);

}

