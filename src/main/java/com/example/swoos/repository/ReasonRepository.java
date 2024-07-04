package com.example.swoos.repository;


import com.example.swoos.model.Reason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReasonRepository extends JpaRepository<Reason, Long> {
    List<Reason> findByRowIdOrderByCreatedDateDesc(int rowId);
}


