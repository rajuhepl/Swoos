package com.example.swoos.repository;


import com.example.swoos.model.MasterRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterRoleRepository extends JpaRepository<MasterRole,Long> {

    MasterRole findByRoleName(String admin);
}

