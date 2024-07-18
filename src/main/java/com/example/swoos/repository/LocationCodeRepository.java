package com.example.swoos.repository;

import com.example.swoos.model.LocationPincode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationCodeRepository extends JpaRepository<LocationPincode,Long> {
}
