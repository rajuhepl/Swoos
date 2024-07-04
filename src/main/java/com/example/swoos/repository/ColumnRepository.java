package com.example.swoos.repository;

import com.example.swoos.model.User;
import com.example.swoos.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColumnRepository extends JpaRepository<UserProfile,Long> {
    Optional<UserProfile> findByUser(User userEntity);
}
