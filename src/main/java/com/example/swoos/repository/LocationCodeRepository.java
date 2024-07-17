package com.example.swoos.repository;

import com.example.swoos.model.LocationWithStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationCodeRepository extends JpaRepository<LocationWithStatusCode,Long> {
}
