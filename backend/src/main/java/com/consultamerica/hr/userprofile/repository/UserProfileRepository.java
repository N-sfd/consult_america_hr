package com.consultamerica.hr.userprofile.repository;

import com.consultamerica.hr.userprofile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmailIgnoreCase(String email);
}
