package com.quickbite.backend.A3_repo;

import com.quickbite.backend.model.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryPartnerRepo extends JpaRepository<DeliveryPartner, Integer> {
    DeliveryPartner findByUserUserId(Integer userId);
}
