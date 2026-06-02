package com.quickbite.backend.A3_repo;

import com.quickbite.backend.model.AssignmentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentStateRepo extends JpaRepository<AssignmentState, Integer> {
}
