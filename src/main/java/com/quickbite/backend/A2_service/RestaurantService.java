package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.RestaurantRepo;
import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.AlreadyExistsException;
import com.quickbite.backend.custom_exception.InvalidInputException;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.restaurant_DTO.RestaurantProfileDTO;
import com.quickbite.backend.dto.restaurant_DTO.RestaurantResponseDTO;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Restaurant;
import com.quickbite.backend.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional
public class RestaurantService {

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private UserRepo userRepo;

    public String createRestaurantProfile(RestaurantProfileDTO restaurantProfile) {

        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        AppUser user = userRepo.findByUserId(userDetails.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        boolean isRestaurantExists = restaurantRepo.existsByUserUserId(user.getUserId());

        if (isRestaurantExists) {
            throw new AlreadyExistsException("Restaurant profile is already created for this restaurant owner");
        }

        Restaurant restaurant = new Restaurant();

        restaurant.setUser(user);
        restaurant.setName(restaurantProfile.getName().toLowerCase().trim());
        restaurant.setActive(true);
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
            byte[] logo = restaurantProfile.getLogo().getBytes();
            System.out.println("length of logo is : " + logo.length);
            if (logo.length == 0) {
                throw new InvalidInputException("Logo cannot be empty");
            }
            restaurant.setLogo(logo);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in RestaurantService setLogo() : " + e);
            throw new RuntimeException(e);
        }
        restaurant.setFSSAILicenseNumber(restaurantProfile.getFSSAILicenseNumber());

        try {
            restaurantRepo.save(restaurant);
            return "Restaurant profile created successfully";
        } catch (DataIntegrityViolationException e) {
            log.error("error in RestaurantService createRestaurantProfile() is : " + e);
            throw new DataIntegrityViolationException("Fassai license number is already taken,. and must be unique");
        } catch (Exception e) {
            log.error("error in RestaurantService createRestaurantProfile() is : " + e);
            throw new RuntimeException("error occurred");
        }
    }


    public List<RestaurantResponseDTO> getAllRestaurants() {
        List<Restaurant> listOfRestaurants = restaurantRepo.findAll();
        List<Restaurant> listOfActiveRestaurants = listOfRestaurants.stream().filter(r -> r.getActive() == true).toList();

        if (listOfActiveRestaurants.isEmpty()) {
            throw new ResourceNotFoundException("Restaurants not added yet...");
        }

        return listOfActiveRestaurants.stream()
                .map((restaurant) -> {
                    RestaurantResponseDTO res = new RestaurantResponseDTO();

                    res.setRestaurantId(restaurant.getRestaurantId());
                    res.setName(restaurant.getName());
                    res.setDescription(restaurant.getDescription());
                    res.setCuisineType(restaurant.getCuisineType());

                    res.setStreetAddress(restaurant.getStreetAddress());
                    res.setCity(restaurant.getCity());
                    res.setState(restaurant.getState());

                    res.setOpeningTime(restaurant.getOpeningTime());
                    res.setClosingTime(restaurant.getClosingTime());
                    res.setAvgPreparationTime(restaurant.getAvgPreparationTime());
                    res.setDeliveryTime(restaurant.getDeliveryTime());

                    res.setLogo(restaurant.getLogo());
                    res.setRating(restaurant.getRating());

                    return res;
                }).toList();
    }
}
