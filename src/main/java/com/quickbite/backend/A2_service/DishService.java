package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.DishRepo;
import com.quickbite.backend.A3_repo.RestaurantRepo;
import com.quickbite.backend.A3_repo.UserRepo;
import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.dto.restaurant_DTO.DishDTO;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Dish;
import com.quickbite.backend.model.Restaurant;
import com.quickbite.backend.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DishService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private DishRepo dishRepo;

    @Transactional
    public String addDish(DishDTO dish) {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        AppUser user = userRepo.findByUserId(userDetails.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        Integer userId = user.getUserId();

        Restaurant restaurant = restaurantRepo.findByUserUserId(userId);

        if(restaurant == null){
            throw new ResourceNotFoundException("Restaurant not found for this Restaurant owner");
        }
        Dish dish1 = new Dish();
        dish1.setRestaurant(restaurant); // restaurant id
        dish1.setName(dish.getName().toLowerCase().trim());
        dish1.setPrice(dish.getPrice());

        dish1.setDescription(dish.getDescription().toLowerCase().trim());

        try {
            dish1.setImage(dish.getImage().getBytes());
        } catch (Exception e) {
            log.error("Error in DishService while setting the dish image : " + e);
            throw new RuntimeException(e);
        }

        try {
            dishRepo.save(dish1);
            return "successfully added the new dish";
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException("Dish with same name already exists.");
        }
        catch (Exception e) {
            log.error("error in DishService while adding new dish to db : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
