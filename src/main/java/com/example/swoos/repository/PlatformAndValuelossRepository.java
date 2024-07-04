package com.example.swoos.repository;

import com.example.swoos.model.PlatformAndValueloss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface PlatformAndValuelossRepository extends JpaRepository<PlatformAndValueloss,Long> {


}
