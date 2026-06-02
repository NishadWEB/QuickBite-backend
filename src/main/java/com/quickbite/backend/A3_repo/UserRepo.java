package com.quickbite.backend.A3_repo;

import com.quickbite.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<AppUser, Integer> {
    AppUser findByEmail(String email);

    AppUser findByUserId(Integer userId);

    boolean existsByEmail(String email);

}
