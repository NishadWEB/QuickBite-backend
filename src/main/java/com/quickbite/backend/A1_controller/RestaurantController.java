package com.quickbite.backend.A1_controller;

import com.quickbite.backend.dto.restaurant_DTO.RestaurantProfileDTO;
import com.quickbite.backend.A2_service.RestaurantService;
import com.quickbite.backend.model.Restaurant;
import jakarta.validation.Valid;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    // create restaurant profile
    @PostMapping("/profile")
    public ResponseEntity<String> createRestaurantProfile(@Valid @ModelAttribute RestaurantProfileDTO restaurantProfile){
        String res = restaurantService.createRestaurantProfile(restaurantProfile);
        return ResponseEntity.status(201).body(res);
    }
}
