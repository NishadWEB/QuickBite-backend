package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.RestaurantRepo;
import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.AlreadyExistsException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.restaurant_DTO.RestaurantProfileDTO;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Restaurant;
import com.quickbite.backend.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private UserRepo userRepo;

    public String createRestaurantProfile( RestaurantProfileDTO restaurantProfile) {

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        AppUser user = userRepo.findByUserId(userDetails.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        boolean isRestaurantExists = restaurantRepo.existsByUserUserId(user.getUserId());

        System.out.println("restaurant is fetched");

        if(isRestaurantExists){
            throw new AlreadyExistsException("Restaurant profile is created for this restaurant owner");
        }

        Restaurant restaurant = new Restaurant();

        restaurant.setUser(user);
        restaurant.setName(restaurantProfile.getName().toLowerCase().trim());
        restaurant.setDescription(restaurantProfile.getDescription().toLowerCase().trim());
        restaurant.setCuisineType(restaurantProfile.getCuisineType().toLowerCase().trim());
        restaurant.setStreetAddress(restaurantProfile.getStreetAddress().toLowerCase().trim());
        restaurant.setCity(restaurantProfile.getCity().toLowerCase().trim());
        restaurant.setState(restaurantProfile.getState().toLowerCase().trim());
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

        try {
            restaurantRepo.save(restaurant);
            return "Restaurant profile created successfully";
        } catch (Exception e) {
            log.error("error in RestaurantService createRestaurantProfile() is : " + e);
            throw new RuntimeException("error occurred");
        }
    }
}
