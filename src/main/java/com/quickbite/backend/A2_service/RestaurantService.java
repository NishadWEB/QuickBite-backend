package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.RestaurantRepo;
import com.quickbite.backend.dto.restaurant_DTO.RestaurantProfileDTO;
import com.quickbite.backend.model.Restaurant;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepo restaurantRepo;

    public String createRestaurantProfile( RestaurantProfileDTO restaurantProfile) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantProfile.getName());
        restaurant.setDescription(restaurantProfile.getDescription());
        restaurant.setCuisineType(restaurantProfile.getCuisineType());
        restaurant.setStreetAddress(restaurantProfile.getStreetAddress());
        restaurant.setCity(restaurantProfile.getCity());
        restaurant.setState(restaurantProfile.getState());
        restaurant.setPinCode(restaurantProfile.getPinCode());

        restaurant.setOpeningTime(restaurantProfile.getOpeningTime());
        restaurant.setClosingTime(restaurantProfile.getClosingTime());
        restaurant.setAvgPreparationTime(restaurantProfile.getAvgPreparationTime());
        restaurant.setDeliveryTime(restaurantProfile.getDeliveryTime());

        try {
            restaurant.setLogo(restaurantProfile.getLogo().getBytes());
        } catch (Exception e) {
            log.error("Error in RestaurantService setLogo() : " + e);
            throw new RuntimeException(e);
        }
        restaurant.setFSSAILicenseNumber(restaurantProfile.getFSSAILicenseNumber());

        if (restaurantProfile.getRating() != null) {
            restaurant.setRating(restaurantProfile.getRating());
        }

        try {
            restaurantRepo.save(restaurant);
            return "Restaurant profile created successfully";
        } catch (Exception e) {
            log.error("error in RestaurantService createRestaurantProfile() is : " + e);
            throw new RuntimeException("error occurred");
        }
    }
}
